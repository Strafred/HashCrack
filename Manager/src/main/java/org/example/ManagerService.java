package org.example;

import com.mongodb.client.FindIterable;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.bson.Document;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class ManagerService {
    CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();
    ManagerRepository managerRepository;
    RequestRepository requestRepository;
    ConnectionFactory connectionFactory;

    @Autowired
    public ManagerService(ManagerRepository managerRepository, RequestRepository requestRepository, ConnectionFactory connectionFactory) {
        this.alphabet.getSymbols().addAll(List.of("abcdefghijklmnopqrstuvwxyz0123456789".split("")));
        this.managerRepository = managerRepository;
        this.requestRepository = requestRepository;
        this.connectionFactory = connectionFactory;
    }

    public void saveJob(String requestId, Job job) {
        managerRepository.insertJob(requestId, job);
    }

    public Job getStatus(String requestId) {
        return managerRepository.getJob(requestId);
    }

    public void confirmJob(String requestId, List<String> words, int partNumber) {
        managerRepository.makeJobPartDone(requestId, words, partNumber);
    }

    public Marshaller createMarshaller() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CrackHashManagerRequest.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        return marshaller;
    }

    public String generateUniqueID() {
        return UUID.randomUUID().toString();
    }

    public String createCrackHashRequestXml(int partCount, int partNumber, String hash, int maxLength, String uniqueID, Marshaller marshaller) throws JAXBException {
        CrackHashManagerRequest requestData = new CrackHashManagerRequest();
        requestData.setPartCount(partCount);
        requestData.setPartNumber(partNumber);
        requestData.setRequestId(uniqueID);
        requestData.setHash(hash);
        requestData.setMaxLength(maxLength);
        requestData.setAlphabet(alphabet);

        StringWriter writer = new StringWriter();
        marshaller.marshal(requestData, writer);
        String xml = writer.toString();
        System.out.println(xml);

        return xml;
    }

    public CrackHashWorkerResponse deserializeWorkerXmlResponse(String xmlResponse) {
        JAXBContext context;
        CrackHashWorkerResponse workerResponseData = null;
        try {
            context = JAXBContext.newInstance(CrackHashWorkerResponse.class);
            workerResponseData = (CrackHashWorkerResponse) context.createUnmarshaller().unmarshal(new java.io.StringReader(xmlResponse));
        } catch (JAXBException e) {
            System.err.println("Error while deserializing xml: " + e.getMessage());
        }
        return workerResponseData;
    }

    public void handleResponse(String xmlResponse) {
        var workerResponseData = deserializeWorkerXmlResponse(xmlResponse);

        System.out.println("Words:");
        System.out.println(workerResponseData.getAnswers().getWords());
        confirmJob(workerResponseData.getRequestId(), workerResponseData.getAnswers().getWords(), workerResponseData.getPartNumber());
    }

    public String createXmlRequest(int workerNumber, int i, String hash, int maxLength, String requestId) {
        String xml = "";
        try {
            var marshaller = createMarshaller();
            xml = createCrackHashRequestXml(workerNumber, i, hash, maxLength, requestId, marshaller);
        } catch (JAXBException e) {
            System.err.println("Something wrong with auto-generated class from XSD schema");
            System.err.println("JAXBException: " + e.getMessage());
        }
        return xml;
    }

    private void sendXmlToRabbitMQ(String xml, Channel channel) throws IOException {
        channel.exchangeDeclare("crack_hash", "direct", true);
        channel.basicPublish("crack_hash", "for_workers", MessageProperties.PERSISTENT_TEXT_PLAIN, xml.getBytes());
        System.out.println(" [x] Sent '" + xml + "'");
    }

    public void handleXmlToRabbitMQ(String xml) {
        try (var connection = connectionFactory.createConnection(); Channel channel = connection.createChannel(false)) {
            sendXmlToRabbitMQ(xml, channel);
        } catch (AmqpException e) {
            System.err.println("Can't create connection to RabbitMQ from manager" + e.getMessage());
            requestRepository.saveRequest(xml);
        } catch (IOException | TimeoutException e) {
            System.err.println("Can't publish message to exchange from manager" + e.getMessage());
            requestRepository.saveRequest(xml);
        }
    }

    @Scheduled(fixedRate = 15000)
    public void scheduledTask() {
        FindIterable<Document> tasks = requestRepository.findAll();
        tasks.forEach(task -> {
            try (var connection = connectionFactory.createConnection(); Channel channel = connection.createChannel(false)) {
                sendXmlToRabbitMQ(task.getString("xmlRequest"), channel);
                requestRepository.deleteRequest(task);
            } catch (AmqpException e) {
                System.err.println("Can't create connection to RabbitMQ from manager" + e.getMessage());
            } catch (IOException | TimeoutException e) {
                System.err.println("Can't publish message to exchange from manager" + e.getMessage());
            }
        });
    }
}

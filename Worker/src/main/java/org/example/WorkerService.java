package org.example;

import com.rabbitmq.client.MessageProperties;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics3.Generator;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class WorkerService {
    ConnectionFactory connectionFactory;

    @Autowired
    public WorkerService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Marshaller createMarshaller() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CrackHashWorkerResponse.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        return marshaller;
    }

    public List<String> generateWords(int partNumber, int partCount, int maxLength, List<String> alphabet, String targetHash) {
        List<String> words = new ArrayList<>();
        for (int length = 1; length <= maxLength; length++) {
            var combinationsIterator = Generator.permutation(alphabet)
                    .withRepetitions(length)
                    .iterator();
            int count = 0;
            while (combinationsIterator.hasNext()) {
                var lettersCombination = combinationsIterator.next();
                count++;
                if (count % partCount == partNumber) {
                    var word = String.join("", lettersCombination);
                    var hash = DigestUtils.md5Hex(word);
                    if (hash.equals(targetHash)) {
                        System.out.println(word);
                        words.add(word);
                    }
                }
                if (count % partCount == 0) {
                    count = 0;
                }
            }
        }
        return words;
    }

    public String createCrackHashResponseXml(List<String> words, CrackHashManagerRequest requestData, Marshaller marshaller) throws JAXBException {
        CrackHashWorkerResponse responseData = new CrackHashWorkerResponse();
        responseData.setRequestId(requestData.getRequestId());
        responseData.setPartNumber(requestData.getPartNumber());
        var answers = new CrackHashWorkerResponse.Answers();
        answers.getWords().addAll(words);
        responseData.setAnswers(answers);

        StringWriter writer = new StringWriter();
        marshaller.marshal(responseData, writer);
        String xml = writer.toString();
        System.out.println(xml);

        return xml;
    }

    public String createResponse(List<String> words, CrackHashManagerRequest requestData) {
        String answerXml = "";
        try {
            var marshaller = createMarshaller();
            answerXml = createCrackHashResponseXml(words, requestData, marshaller);
        } catch (JAXBException e) {
            System.err.println("Something wrong with auto-generated class from XSD schema");
            System.err.println("JAXBException: " + e.getMessage());
        }
        return answerXml;
    }

    public void sendResponseToManager(String answerXml) {
        try (var connection = connectionFactory.createConnection(); var channel = connection.createChannel(false)) {
            channel.exchangeDeclare("crack_hash", "direct", true);
            channel.basicPublish("crack_hash", "for_manager", MessageProperties.PERSISTENT_TEXT_PLAIN, answerXml.getBytes());
            System.out.println(" [x] Sent '" + answerXml + "'");
        } catch (AmqpException e) {
            System.err.println("Can't create connection to RabbitMQ server" + e.getMessage());
        } catch (IOException | TimeoutException e) {
            System.err.println("Can't publish message to manager" + e.getMessage());
        }
    }
}

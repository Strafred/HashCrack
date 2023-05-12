package org.example;

import com.mongodb.client.FindIterable;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

import org.bson.Document;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping("/manager")
public class ManagerController {
    private static final int TIMEOUT = 15000;
    private static final int WORKERS_NUMBER = Integer.parseInt(System.getenv("WORKERS_NUMBER"));
    private final ManagerService managerService;
    private final ConnectionFactory connectionFactory;
    private final RequestRepository requestRepository;

    @Autowired
    public ManagerController(ManagerService managerService, ConnectionFactory connectionFactory, RequestRepository requestRepository) {
        this.managerService = managerService;
        this.connectionFactory = connectionFactory;
        this.requestRepository = requestRepository;
    }

    @RabbitListener(queues = "manager_queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message) {
        String xmlResponse = new String(message.getBody());

        System.out.println("Received response from worker");
        System.out.println(xmlResponse);

        JAXBContext context;
        CrackHashWorkerResponse workerResponseData = null;
        try {
            context = JAXBContext.newInstance(CrackHashWorkerResponse.class);
            workerResponseData = (CrackHashWorkerResponse) context.createUnmarshaller().unmarshal(new java.io.StringReader(xmlResponse));
        } catch (JAXBException e) {
            System.err.println("JAXBException: " + e.getMessage());
        }

        System.out.println("Words:");
        assert workerResponseData != null;
        System.out.println(workerResponseData.getAnswers().getWords());
        managerService.confirmJob(workerResponseData.getRequestId(), workerResponseData.getAnswers().getWords(), workerResponseData.getPartNumber());
    }

    @GetMapping("/api/hash/status")
    public Job getJobStatus(@RequestParam String requestId) {
        return managerService.getStatus(requestId);
    }

    @PostMapping("/api/hash/crack")
    @ResponseBody
    public String crackHash(@RequestBody HashInfo hashInfo) throws IOException, TimeoutException {
        String requestId = managerService.generateUniqueID();
        managerService.saveJob(requestId, new Job(Status.IN_PROGRESS, new ArrayList<>(), WORKERS_NUMBER));

        for (int i = 0; i < WORKERS_NUMBER; i++) {
            String xml = "";
            try {
                var marshaller = managerService.createMarshaller();
                xml = managerService.createCrackHashRequestXml(WORKERS_NUMBER, i, hashInfo.getHash(), hashInfo.getMaxLength(), requestId, marshaller);
            } catch (JAXBException e) {
                System.err.println("Something wrong with auto-generated class from XSD schema");
                System.err.println("JAXBException: " + e.getMessage());
            }

            try (var connection = connectionFactory.createConnection();
                 Channel channel = connection.createChannel(false)) {
                try {
                    channel.exchangeDeclare("crack_hash", "direct", true);
                    channel.basicPublish("crack_hash", "for_workers", MessageProperties.PERSISTENT_TEXT_PLAIN, xml.getBytes());
                    System.out.println(" [x] Sent '" + xml + "'");
                } catch (IOException e) {
                    System.err.println("Can't publish message to exchange from manager" + e.getMessage());
                    requestRepository.saveRequest(xml);
                }
            } catch (UnknownHostException e) {
                System.err.println("Can't create connection to RabbitMQ from manager" + e.getMessage());
                requestRepository.saveRequest(xml);
            }
        }
        return requestId;
    }

    @Scheduled(fixedRate = 15000)
    public void scheduledTask() {
        FindIterable<Document> tasks = requestRepository.findAll();
        tasks.forEach(task -> {
            try (var connection = connectionFactory.createConnection();
                 Channel channel = connection.createChannel(false)) {
                try {
                    channel.exchangeDeclare("crack_hash", "direct", true);
                    channel.basicPublish("crack_hash", "for_workers", MessageProperties.PERSISTENT_TEXT_PLAIN, task.getString("xmlRequest").getBytes());
                    System.out.println(" [x] Sent '" + task.getString("xmlRequest") + "'");
                    requestRepository.deleteRequest(task);
                } catch (IOException | AmqpException e) {
                    System.err.println("Can't publish message to exchange from manager" + e.getMessage());
                }
            } catch (IOException | TimeoutException e) {
                System.err.println("Can't create connection to RabbitMQ from manager" + e.getMessage());
            }
        });
    }
}

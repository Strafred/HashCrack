package org.example;

import com.rabbitmq.client.MessageProperties;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

import java.io.IOException;
import java.util.List;

@Component
public class Receiver {

    final WorkerService workerService;
    final ConnectionFactory connectionFactory;

    @Autowired
    public Receiver(WorkerService workerService, ConnectionFactory connectionFactory) {
        this.workerService = workerService;
        this.connectionFactory = connectionFactory;
    }

    @RabbitListener(queues = "workers_queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message) throws Exception {
        String xml = new String(message.getBody());

        JAXBContext context = JAXBContext.newInstance(CrackHashManagerRequest.class);
        CrackHashManagerRequest requestData = (CrackHashManagerRequest) context.createUnmarshaller().unmarshal(new java.io.StringReader(xml));

        System.out.println(requestData.getRequestId());
        System.out.println(requestData.getHash());
        System.out.println(requestData.getMaxLength());
        System.out.println(requestData.getAlphabet().getSymbols());

        List<String> words = workerService.generateWords(
                requestData.getPartNumber(),
                requestData.getPartCount(),
                requestData.getMaxLength(),
                requestData.getAlphabet().getSymbols(),
                requestData.getHash());

        String answerXml = "";
        try {
            var marshaller = workerService.createMarshaller();
            answerXml = workerService.createCrackHashResponseXml(words, requestData, marshaller);
        } catch (JAXBException e) {
            System.err.println("Something wrong with auto-generated class from XSD schema");
            System.err.println("JAXBException: " + e.getMessage());
        }

        try (var connection = connectionFactory.createConnection();
             var channel = connection.createChannel(false)) {
            try {
                channel.exchangeDeclare("crack_hash", "direct", true);
                channel.basicPublish("crack_hash", "for_manager", MessageProperties.PERSISTENT_TEXT_PLAIN, answerXml.getBytes());
                System.out.println(" [x] Sent '" + xml + "'");
            } catch (IOException e) {
                System.err.println("Can't publish message to manager" + e.getMessage());
            }
        }
    }
}

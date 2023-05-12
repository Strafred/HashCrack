//package org.example;
//
//import com.rabbitmq.client.ConnectionFactory;
//import jakarta.xml.bind.JAXBContext;
//import jakarta.xml.bind.JAXBException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.TimeoutException;
//
//@RestController
//@RequestMapping("/worker")
//public class WorkerController {
//    private final WorkerService workerService;
//    ConnectionFactory connectionFactory;
//
//    @Autowired
//    public WorkerController(WorkerService workerService) throws IOException, TimeoutException {
//        this.workerService = workerService;
//        connectionFactory = new ConnectionFactory();
//        connectionFactory.setHost("rabbitmq");
//        connectionFactory.setUsername("user");
//        connectionFactory.setPassword("password");
//
////        Connection connection = connectionFactory.newConnection();
////        Channel channel = connection.createChannel();
//
////        channel.exchangeDeclare("crack_hash", "direct");
////        channel.queueDeclare("workers_queue", true, false, false, null);
////        channel.queueBind("workers_queue", "crack_hash", "for_workers");
////
////        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
////            String message = new String(delivery.getBody(), "UTF-8");
////            System.out.println(" [x] Received '" +
////                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
////            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
////        };
////        channel.basicConsume("workers_queue", false, deliverCallback, consumerTag -> {});
//    }
//
//    @PostMapping("/internal/api/worker/hash/crack/task")
//    public String crackHashTask(@RequestBody String xml) throws JAXBException {
//
//    }
//}

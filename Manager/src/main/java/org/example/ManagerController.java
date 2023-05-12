package org.example;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/manager")
public class ManagerController {
    private static final int WORKERS_NUMBER = Integer.parseInt(System.getenv("WORKERS_NUMBER"));
    private final ManagerService managerService;

    @Autowired
    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @RabbitListener(queues = "manager_queue", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message) {
        try {
            String xmlResponse = new String(message.getBody());
            System.out.println("Received response from worker");
            System.out.println(xmlResponse);
            managerService.handleResponse(xmlResponse);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    @GetMapping("/api/hash/status")
    public Job getJobStatus(@RequestParam String requestId) {
        return managerService.getStatus(requestId);
    }

    @PostMapping("/api/hash/crack")
    @ResponseBody
    public String crackHash(@RequestBody HashInfo hashInfo) {
        String requestId = managerService.generateUniqueID();
        managerService.saveJob(requestId, new Job(Status.IN_PROGRESS, new ArrayList<>(), WORKERS_NUMBER));

        for (int i = 0; i < WORKERS_NUMBER; i++) {
            String xml = managerService.createXmlRequest(WORKERS_NUMBER, i, hashInfo.getHash(), hashInfo.getMaxLength(), requestId);
            managerService.handleXmlToRabbitMQ(xml);
        }
        return requestId;
    }
}

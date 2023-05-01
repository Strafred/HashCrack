package org.example;

import jakarta.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.scheduler.Schedulers;


@RestController
@RequestMapping("/manager")
public class ManagerController {
    private final ManagerService managerService;
    private ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    @Autowired
    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/api/hash/status")
    public Job getJobStatus(@RequestParam String requestId) {
        return jobs.get(requestId);
    }

    @PostMapping("/api/hash/crack")
    @ResponseBody
    public String crackHash(@RequestBody HashInfo hashInfo) {
        String xml = "";
        String requestId = managerService.generateUniqueID();
        try {
            var marshaller = managerService.createMarshaller();
            xml = managerService.createCrackHashRequestXml(hashInfo.getHash(), hashInfo.getMaxLength(), requestId, marshaller);
        } catch (JAXBException e) {
            System.err.println("Something wrong with auto-generated classes from XSD schema!");
            System.err.println("JAXBException: " + e.getMessage());
        }

        jobs.put(requestId, new Job(Status.IN_PROGRESS, new String[0]));

        WebClient client = WebClient.create("http://localhost:8081/worker/internal/api/manager/hash/crack/request");
        client.patch()
                .header("Content-Type", "text/xml")
                .bodyValue(xml)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .doFinally((data) -> {
                    System.err.println("rofl " + data);
                    if (jobs.get(requestId).getStatus() == Status.IN_PROGRESS) {
                        jobs.put(requestId, new Job(Status.ERROR, new String[0]));
                    }
                })
                .subscribeOn(Schedulers.single()) // ???
                .subscribe(response -> {
                    System.err.println(response);
                    if (response.equals("OK")) {
                        jobs.put(requestId, new Job(Status.READY, new String[0]));
                    }
                });

        return requestId;
    }
}
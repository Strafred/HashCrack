package org.example;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

import java.time.Duration;
import java.util.ArrayList;


@RestController
@RequestMapping("/manager")
public class ManagerController {
    private static final int TIMEOUT = 15000;
    private final ManagerService managerService;

    @Autowired
    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/api/hash/status")
    public Job getJobStatus(@RequestParam String requestId) {
        return managerService.getStatus(requestId);
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
            System.err.println("Something wrong with auto-generated class from XSD schema");
            System.err.println("JAXBException: " + e.getMessage());
        }

        managerService.saveJob(requestId, new Job(Status.IN_PROGRESS, new ArrayList<>()));

        WebClient client = WebClient.create("http://hashcrack-worker-1:8081/worker/internal/api/worker/hash/crack/task");
        client.post()
                .header("Content-Type", "text/xml")
                .bodyValue(xml)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(TIMEOUT))
                .subscribe(
                        xmlResponse -> {
                            System.out.println("Response from worker:");
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
                            System.out.println(workerResponseData.getAnswers().getWords());
                            managerService.saveJob(requestId, new Job(Status.READY, workerResponseData.getAnswers().getWords()));
                        },
                        error -> {
                            System.err.println(error.getMessage());
                            managerService.saveJob(requestId, new Job(Status.ERROR, new ArrayList<>()));
                        }
                );

        return requestId;
    }
}
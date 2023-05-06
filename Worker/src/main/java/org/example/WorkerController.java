package org.example;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/worker")
public class WorkerController {
    private final WorkerService workerService;

    @Autowired
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping("/internal/api/worker/hash/crack/task")
    public String crackHashTask(@RequestBody String xml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CrackHashManagerRequest.class);
        CrackHashManagerRequest requestData = (CrackHashManagerRequest) context.createUnmarshaller().unmarshal(new java.io.StringReader(xml));

        System.out.println(requestData.getRequestId());
        System.out.println(requestData.getHash());
        System.out.println(requestData.getMaxLength());
        System.out.println(requestData.getAlphabet().getSymbols());

        List<String> words = workerService.generateWords(requestData.getMaxLength(), requestData.getAlphabet().getSymbols(), requestData.getHash());

        String answerXml = "";
        try {
            var marshaller = workerService.createMarshaller();
            answerXml = workerService.createCrackHashResponseXml(words, requestData.getRequestId(), marshaller);
        } catch (JAXBException e) {
            System.err.println("Something wrong with auto-generated class from XSD schema");
            System.err.println("JAXBException: " + e.getMessage());
        }

        return answerXml;
    }
}

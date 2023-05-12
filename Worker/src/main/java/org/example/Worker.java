package org.example;

import jakarta.xml.bind.JAXBContext;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

import java.util.List;

@Component
public class Worker {
    final WorkerService workerService;

    @Autowired
    public Worker(WorkerService workerService) {
        this.workerService = workerService;
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

        String answerXml = workerService.createResponse(words, requestData);
        workerService.sendResponseToManager(answerXml);
    }
}

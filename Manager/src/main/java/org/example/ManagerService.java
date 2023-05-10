package org.example;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

@Service
public class ManagerService {
    CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();
    ManagerRepository managerRepository;

    @Autowired
    public ManagerService(ManagerRepository managerRepository) {
        this.alphabet.getSymbols().addAll(List.of("abcdefghijklmnopqrstuvwxyz0123456789".split("")));
        this.managerRepository = managerRepository;
    }

    public void saveJob(String requestId, Job job) {
        managerRepository.insertJob(requestId, job);
    }

    public Job getStatus(String requestId) {
        return managerRepository.getJob(requestId);
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

    public void confirmJob(String requestId, List<String> words, int partNumber) {
        managerRepository.makeJobPartDone(requestId, words, partNumber);
    }
}

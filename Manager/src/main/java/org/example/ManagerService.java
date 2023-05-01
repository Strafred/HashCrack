package org.example;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

@Service
public class ManagerService {
    CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();

    public ManagerService() {
        alphabet.getSymbols().addAll(List.of("abcdefghijklmnopqrstuvwxyz0123456789".split("")));
        System.out.println("Alphabet is: " + alphabet.getSymbols());
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

    public String createCrackHashRequestXml(String hash, int maxLength, String uniqueID, Marshaller marshaller) throws JAXBException {
        CrackHashManagerRequest requestData = new CrackHashManagerRequest();
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
}

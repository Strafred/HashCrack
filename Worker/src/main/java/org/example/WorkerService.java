package org.example;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkerService {
    public Marshaller createMarshaller() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CrackHashWorkerResponse.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        return marshaller;
    }

    public List<String> generateWords(int partNumber, int partCount, int maxLength, List<String> alphabet, String targetHash) {
        List<String> words = new ArrayList<>();

        for (int length = 1; length <= maxLength; length++) {
            var combinationsIterator = Generator.permutation(alphabet)
                    .withRepetitions(length)
                    .iterator();

            int count = 0;

            while (combinationsIterator.hasNext()) {
                var lettersCombination = combinationsIterator.next();
                count++;

                if (count % partCount == partNumber) {
                    var word = String.join("", lettersCombination);
                    var hash = DigestUtils.md5Hex(word);
                    if (hash.equals(targetHash)) {
                        System.out.println(word);
                        words.add(word);
                    }
                }

                if (count % partCount == 0) {
                    count = 0;
                }
            }
        }
        return words;
    }

    public String createCrackHashResponseXml(List<String> words, CrackHashManagerRequest requestData, Marshaller marshaller) throws JAXBException {
        CrackHashWorkerResponse responseData = new CrackHashWorkerResponse();
        responseData.setRequestId(requestData.getRequestId());
        responseData.setPartNumber(requestData.getPartNumber());
        var answers = new CrackHashWorkerResponse.Answers();
        answers.getWords().addAll(words);
        responseData.setAnswers(answers);

        StringWriter writer = new StringWriter();
        marshaller.marshal(responseData, writer);
        String xml = writer.toString();
        System.out.println(xml);

        return xml;
    }
}

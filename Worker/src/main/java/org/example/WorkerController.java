package org.example;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

@RestController
@RequestMapping("/worker")
public class WorkerController {
    @PatchMapping("/internal/api/manager/hash/crack/request")
    public String greeting(@RequestBody String xml) throws JAXBException, InterruptedException {
        System.out.println("WorkerController.greeting!!!!!!!!");
        JAXBContext context = JAXBContext.newInstance(CrackHashManagerRequest.class);
        CrackHashManagerRequest requestData = (CrackHashManagerRequest) context.createUnmarshaller().unmarshal(new java.io.StringReader(xml));

        Thread.sleep(10000);

//        String target = "e2fc714c4727ee9395f324cd2e7f331f";
//        int length = 4;
//        String[] alphabet = "abcdefghijklmnopqrstuvwxyz".split("");
//
//        ICombinatoricsVector<String> vector = createVector(alphabet);
//        Generator<String> generator = createPermutationWithRepetitionGenerator(vector, length);
//        for (ICombinatoricsVector<String> letters : generator) {
//            var word = String.join("", letters.getVector());
//            var hash = DigestUtils.md5Hex(word);
//            if (hash.equals(target)) {
//                System.out.println(word);
//                break;
//            }
//        }

        return "OK";
    }
}

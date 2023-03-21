package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("/manager")
public class ManagerController {
    @GetMapping("/id")
    public String greeting() throws IOException {
        var workersNumber = System.getenv("WORKERS_NUMBER");
        for (int i = 1; i <= Integer.parseInt(workersNumber); i++) {
            var workerUrl = new URL("http://hashcrack-worker-" + i + ":8080/worker/id");
            System.out.println(workerUrl);

            var connection = (HttpURLConnection) workerUrl.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response);
        }
        return "done";
    }

//    @GetMapping("/workers")
//    public void getWorkers() {
//
//    }
}
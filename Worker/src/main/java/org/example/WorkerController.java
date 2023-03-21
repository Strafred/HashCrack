package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worker")
public class WorkerController {
    @GetMapping("/id")
    public long greeting() {
        System.out.println("WorkerController.greeting!!!!!!!!");
        return new Worker().getId();
    }
}

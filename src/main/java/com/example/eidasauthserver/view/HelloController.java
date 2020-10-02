package com.example.eidasauthserver.view;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @RequestMapping("/")
    public String getMessage() {
        return "Hello world!";
    }

    int addition(int x, int y) {
        return x + y;
    }

    int multiplication(int x, int y) {
        return x * y;
    }
}

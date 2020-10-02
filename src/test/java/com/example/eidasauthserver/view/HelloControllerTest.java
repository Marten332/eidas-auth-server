package com.example.eidasauthserver.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HelloControllerTest {

    HelloController hc;

    @BeforeEach
    void setUp() {
        this.hc = new HelloController();
    }

    @Test
    void getMessage() {
        assertEquals("Hello world!", hc.getMessage());
    }

    @Test
    void calculation() {
        assertEquals(9, hc.addition(5,4));
    }

    @Test
    void multiplication() {
        assertEquals(20, hc.multiplication(5,4));
    }
}

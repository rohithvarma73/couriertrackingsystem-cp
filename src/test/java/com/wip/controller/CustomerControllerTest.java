package com.wip.controller;

import com.wip.couriertrackingsystem.CouriertrackingsystemApplication;
import com.wip.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = CouriertrackingsystemApplication.class)
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private CustomerController customerController;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void contextLoads() {
        assertNotNull(customerController);
    }
}
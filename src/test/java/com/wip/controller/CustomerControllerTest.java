package com.wip.controller;

import com.wip.couriertrackingsystem.CouriertrackingsystemApplication;
import com.wip.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = CouriertrackingsystemApplication.class)
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private CustomerController customerController;

    @MockBean
    private CustomerService customerService;

    @Test
    void contextLoads() {
    }
}
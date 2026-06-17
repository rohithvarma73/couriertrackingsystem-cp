package com.wip.controller;

import com.wip.couriertrackingsystem.CouriertrackingsystemApplication;
import com.wip.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = CouriertrackingsystemApplication.class)
@ActiveProfiles("test")
class ShipmentControllerTest {

    @Autowired
    private ShipmentController shipmentController;

    @MockitoBean
    private ShipmentService shipmentService;

    @Test
    void contextLoads() {
        assertNotNull(shipmentController);
    }
}
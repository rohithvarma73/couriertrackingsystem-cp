package com.wip.controller;

import com.wip.couriertrackingsystem.CouriertrackingsystemApplication;
import com.wip.service.TrackingUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = CouriertrackingsystemApplication.class)
@ActiveProfiles("test")
class TrackingUpdateControllerTest {

    @Autowired
    private TrackingUpdateController trackingUpdateController;

    @MockitoBean
    private TrackingUpdateService trackingUpdateService;

    @Test
    void contextLoads() {
        assertNotNull(trackingUpdateController);
    }
}
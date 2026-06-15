package com.wip.controller;

import com.wip.couriertrackingsystem.CouriertrackingsystemApplication;
import com.wip.service.TrackingUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = CouriertrackingsystemApplication.class)
@ActiveProfiles("test")
class TrackingUpdateControllerTest {

    @Autowired
    private TrackingUpdateController trackingUpdateController;

    @MockBean
    private TrackingUpdateService trackingUpdateService;

    @Test
    void contextLoads() {
    }
}
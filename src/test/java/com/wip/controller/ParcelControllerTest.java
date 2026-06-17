package com.wip.controller;

import com.wip.couriertrackingsystem.CouriertrackingsystemApplication;
import com.wip.service.ParcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = CouriertrackingsystemApplication.class)
@ActiveProfiles("test")
class ParcelControllerTest {

    @Autowired
    private ParcelController parcelController;

    @MockitoBean
    private ParcelService parcelService;

    @Test
    void contextLoads() {
        assertNotNull(parcelController);
    }
}
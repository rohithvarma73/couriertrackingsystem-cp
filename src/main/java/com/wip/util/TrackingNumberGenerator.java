package com.wip.util;

import java.util.UUID;

public final class TrackingNumberGenerator {

    private TrackingNumberGenerator() {
    }

    public static String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
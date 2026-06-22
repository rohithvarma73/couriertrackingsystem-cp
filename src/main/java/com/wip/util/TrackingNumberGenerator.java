package com.wip.util;

import java.util.UUID;

/**
 * Utility class for generating unique parcel tracking numbers.
 *
 * <p>This class is a stateless utility that cannot be instantiated. It provides
 * a single static factory method to produce tracking numbers in the standardised
 * format {@code TRK-XXXXXXXX}, where the suffix is an uppercase 8-character
 * hexadecimal segment derived from a randomly generated {@link UUID}. The
 * resulting identifiers are sufficiently unique for practical use within the
 * Courier Tracking System and can be assigned to new parcels at the time of
 * creation.</p>
 *
 * <p>Example output: {@code TRK-A1B2C3D4}</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public final class TrackingNumberGenerator {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * <p>All functionality is exposed via static methods; creating instances
     * of this class serves no purpose and is therefore disallowed.</p>
     */
    private TrackingNumberGenerator() {
    }

    /**
     * Generates a unique tracking number for a courier parcel.
     *
     * <p>The tracking number is produced by taking the first 8 characters of a
     * randomly generated {@link UUID} string (after stripping the hyphens used
     * by {@link UUID#toString()}), converting them to uppercase, and prepending
     * the {@code "TRK-"} prefix. This yields a compact, human-readable identifier
     * such as {@code TRK-3F8A12BC}.</p>
     *
     * <p><strong>Format:</strong> {@code TRK-{8-char-hex}}</p>
     *
     * @return a non-null, non-empty tracking number string in the format
     *         {@code TRK-XXXXXXXX}, guaranteed to be unique with very high
     *         probability due to the UUID entropy
     */
    public static String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
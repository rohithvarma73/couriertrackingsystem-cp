package com.wip.exception;

/**
 * Exception thrown when a requested resource cannot be found in the system.
 *
 * <p>This is an unchecked exception that extends {@link RuntimeException} and is used
 * throughout the service and controller layers to signal that a specific entity
 * (e.g., a customer, parcel, or shipment) does not exist for the given identifier.
 * It is handled centrally by {@code GlobalExceptionHandler} for REST endpoints
 * (returning HTTP 404) and by {@code UiExceptionHandler} for Thymeleaf UI controllers
 * (rendering the {@code error/not-found} view).</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

	/** Serial version UID for safe serialization of this exception class. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code ResourceNotFoundException} with the specified detail message.
	 *
	 * <p>The message should clearly identify the resource type and the identifier that
	 * was used in the lookup, for example:
	 * {@code "Customer not found with id: 42"}.</p>
	 *
	 * @param message a human-readable description of the missing resource;
	 *                propagated to the HTTP response body or UI error page
	 */
	public ResourceNotFoundException(String message) {
        super(message);
    }
}

package com.wip.exception;

/**
 * ResourceNotFoundException Component.
 * 
 * Handles operations and data related to ResourceNotFoundException.
 */
public class ResourceNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}

package com.example.priceservice.domain.exception;

public final class AmbiguousPriceSelectionException extends RuntimeException {

    public AmbiguousPriceSelectionException(int priority) {
        super("Multiple applicable prices have the same highest priority: " + priority);
    }
}

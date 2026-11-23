package com.example.priceservice.application.exception;

public final class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException() {
        super("No applicable price was found for the requested product, brand, and date");
    }
}

package com.nedap.go.gui;

import java.io.Serial;

public class InvalidCoordinateException extends Exception {

    @Serial
    private static final long serialVersionUID = -3201761568174113313L;

    public InvalidCoordinateException(String message) {
        super(message);
    }
}

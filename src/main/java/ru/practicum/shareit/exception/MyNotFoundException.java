package ru.practicum.shareit.exception;

public class MyNotFoundException extends RuntimeException {

    public MyNotFoundException(String message) {
        super(message);
    }
}

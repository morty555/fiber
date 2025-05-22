package com.example.srp.exception;

public class AccountExistedException extends BaseException{
    public AccountExistedException() {
    }

    public AccountExistedException(String msg) {
        super(msg);
    }
}

package com.example.srp.constant;

import com.example.srp.exception.BaseException;

public class AccountLockedException extends BaseException {
    public AccountLockedException() {
    }

    public AccountLockedException(String msg) {
        super(msg);
    }
}

package com.fpt.exception;

import org.springframework.security.authentication.DisabledException;

public class AccountBannedException extends DisabledException {
    public AccountBannedException() {
        super("Tài khoản của bạn đã bị khóa");
    }
}
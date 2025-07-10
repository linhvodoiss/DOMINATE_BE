package com.fpt.exception;

import org.springframework.security.authentication.DisabledException;

public class AccountNotActivatedException extends DisabledException {
    public AccountNotActivatedException() {
        super("Tài khoản của bạn chưa được kích hoạt");
    }
}
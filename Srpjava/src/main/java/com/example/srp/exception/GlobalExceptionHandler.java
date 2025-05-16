package com.example.srp.exception;

import com.example.srp.constant.AccountLockedException;
import com.example.srp.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(AccountNotFoundException.class)
    public Result handleAccountNotFound(AccountNotFoundException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(PasswordErrorException.class)
    public Result handlePasswordError(PasswordErrorException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public Result handleAccountLocked(AccountLockedException ex) {
        return Result.error(ex.getMessage());
    }

    // 其他通用异常
    @ExceptionHandler(Exception.class)
    public Result handleGenericException(Exception ex) {
        ex.printStackTrace(); // 开发阶段建议保留日志
        return Result.error("服务器发生错误：" + ex.getMessage());
    }
}

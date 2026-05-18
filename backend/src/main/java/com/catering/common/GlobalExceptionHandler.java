package com.catering.common;

import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValid(Exception e) {
        return Result.fail(1001, "参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception e) {
        e.printStackTrace();
        return Result.fail(3001, "服务器内部错误: " + e.getMessage());
    }
}

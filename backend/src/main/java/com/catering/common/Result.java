package com.catering.common;

import lombok.Data;

@Data
public class Result<T> {
    private int status;
    private String message;
    private boolean success;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.status = 0;
        r.message = "";
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int status, String message) {
        Result<T> r = new Result<>();
        r.status = status;
        r.message = message;
        r.success = false;
        r.data = null;
        return r;
    }
}

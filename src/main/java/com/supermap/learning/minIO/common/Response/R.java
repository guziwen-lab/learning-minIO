package com.supermap.learning.minIO.common.Response;

import com.supermap.learning.minIO.common.enumeration.BizCodeEnum;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * @author lty
 */
@Data
public class R<T> {

    /**
     * 0 为处理成功
     */
    private Integer code;

    private String msg;

    private T data;

    public R() {
    }

    public R(int code) {
        this(code, null, null);
    }

    public R(int code, String msg) {
        this(code, msg, null);
    }

    public R(int code, T data) {
        this(code, null, data);
    }

    public R(@Nullable Integer code, @Nullable String msg, @Nullable T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> error() {
        return new R<>(500);
    }

    public static <T> R<T> error(String message) {
        return new R<>(500, message);
    }

    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message);
    }

    public static <T> R<T> error(T data) {
        return new R<>(500, data);
    }

    public static <T> R<T> error(int code, T data) {
        return new R<>(code, data);
    }

    public static <T> R<T> error(int code, String message, T data) {
        return new R<>(code, message, data);
    }

    public static <T> R<T> error(BizCodeEnum bizCodeEnum) {
        return new R<>(bizCodeEnum.getCode(), bizCodeEnum.getMessage());
    }

    public static <T> R<T> error(BizCodeEnum bizCodeEnum, T data) {
        return new R<>(bizCodeEnum.getCode(), bizCodeEnum.getMessage(), data);
    }

    public static <T> R<T> ok() {
        return new R<>(0);
    }

    public static <T> R<T> ok(String message) {
        return ok(message, null);
    }

    public static <T> R<T> ok(T data) {
        return ok(null, data);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(0, message, data);
    }

    public boolean check() {
        return getCode() == 0;
    }
}

package com.supermap.learning.minIO.exception;

import com.supermap.learning.minIO.common.Response.R;
import com.supermap.learning.minIO.common.enumeration.BizCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author litianyi
 */
@Slf4j
@RestControllerAdvice(basePackages = {"com.supermap.unzip.controller"})
public class GlobalExceptionControllerAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public R<Map<String, String>> handleValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        result.getFieldErrors().forEach((item) -> errorMap.put(item.getField(), item.getDefaultMessage()));
        log.error("Validation failed for argument{}", errorMap);
        return R.error(BizCodeEnum.VALID_EXCEPTION, errorMap);
    }

    @ExceptionHandler(value = {Throwable.class})
    public R<Throwable> handleException(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
        return R.error(10000, throwable.getMessage());
    }

}

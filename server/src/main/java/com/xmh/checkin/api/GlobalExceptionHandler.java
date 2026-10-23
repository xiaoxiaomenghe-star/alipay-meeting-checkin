package com.xmh.checkin.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiResponse<Void>> business(BusinessException ex, HttpServletRequest request) {
    return ResponseEntity.status(ex.status())
        .body(new ApiResponse<>(ex.code(), ex.getMessage(), trace(request), null));
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    BindException.class,
    IllegalArgumentException.class
  })
  ResponseEntity<ApiResponse<Void>> invalid(Exception ex, HttpServletRequest request) {
    return ResponseEntity.badRequest()
        .body(new ApiResponse<>("INVALID_ARGUMENT", "请求参数不合法", trace(request), null));
  }

  @ExceptionHandler(DuplicateKeyException.class)
  ResponseEntity<ApiResponse<Void>> duplicate(
      DuplicateKeyException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiResponse<>("DUPLICATE_OPERATION", "请勿重复提交", trace(request), null));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> unknown(Exception ex, HttpServletRequest request) {
    return ResponseEntity.status(500)
        .body(new ApiResponse<>("INTERNAL_ERROR", "服务暂时不可用", trace(request), null));
  }

  private String trace(HttpServletRequest request) {
    return String.valueOf(request.getAttribute("traceId"));
  }
}

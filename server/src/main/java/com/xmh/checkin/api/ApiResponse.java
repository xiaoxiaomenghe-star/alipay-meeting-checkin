package com.xmh.checkin.api;

public record ApiResponse<T>(String code, String message, String traceId, T data) {
  public static <T> ApiResponse<T> ok(String traceId, T data) {
    return new ApiResponse<>("OK", "success", traceId, data);
  }
}

package top.emilejones.hhu.web.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理配置
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 IllegalArgumentException 异常，返回 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return buildResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理 IllegalStateException 异常，返回 409
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        return buildResponse(e.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * 处理 UnsupportedOperationException 异常，返回 501
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedOperationException(UnsupportedOperationException e) {
        return buildResponse(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * 处理 NullPointerException 异常，返回 500
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException e) {
        return buildResponse(e.getMessage() != null ? e.getMessage() : "Null Pointer Exception", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理所有其他异常，返回 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return buildResponse(e.getMessage() != null ? e.getMessage() : "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> buildResponse(String message, HttpStatus status) {
        Map<String, String> body = new HashMap<>();
        body.put("msg", message);
        return new ResponseEntity<>(body, status);
    }
}

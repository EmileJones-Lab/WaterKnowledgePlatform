package top.emilejones.hhu.web.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.emilejones.hhu.common.exception.*;
import top.emilejones.hhu.web.vo.FailureVO;

/**
 * 全局异常处理配置
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<FailureVO> handleBadRequestException(BadRequestException e) {
        return buildResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<FailureVO> handleNotFoundException(NotFoundException e) {
        return buildResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<FailureVO> handleConflictException(ConflictException e) {
        return buildResponse(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InternalAppException.class)
    public ResponseEntity<FailureVO> handleInternalAppException(InternalAppException e) {
        return buildResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotImplementedBusinessException.class)
    public ResponseEntity<FailureVO> handleNotImplementedBusinessException(NotImplementedBusinessException e) {
        return buildResponse(e, HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FailureVO> handleException(Exception e) {
        return buildResponse("系统异常，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<FailureVO> buildResponse(AppException exception, HttpStatus status) {
        return buildResponse(exception.getMessage(), status);
    }

    private ResponseEntity<FailureVO> buildResponse(String message, HttpStatus status) {
        FailureVO body = new FailureVO();
        body.setMsg(message);
        return new ResponseEntity<>(body, status);
    }
}

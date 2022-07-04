package my.demo.webservice.exception;

import my.demo.webservice.common.web.Code;
import my.demo.webservice.common.web.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException e) {
        log.warn(e.getMessage());
        return ResponseEntity.fail(e.getCodeValue(), e.getMessage());
    }

    @ExceptionHandler(ShiroException.class)
    public ResponseEntity<Object> handleShiroException(ShiroException e) {
        log.warn("shiro verify error:", e);
        return ResponseEntity.fail(Code.UNAUTHORIZED.getValue(), Code.UNAUTHORIZED.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRunTimeException(RuntimeException e) {
        e.printStackTrace();
        return ResponseEntity.fail(Code.INTERNAL_ERROR.getValue(), Code.INTERNAL_ERROR.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.warn("参数校验异常！");
        // 从异常对象中拿到ObjectError对象
        List<ObjectError> errors = e.getBindingResult().getAllErrors();
        // 然后提取错误提示信息进行返回
        return ResponseEntity.badArgument(formatError(errors));
    }

    private String formatError(List<ObjectError> errors) {
        StringBuilder builder = new StringBuilder();
        errors.forEach(error -> builder.append(error.getDefaultMessage()).append(";"));
        return builder.toString();
    }
}

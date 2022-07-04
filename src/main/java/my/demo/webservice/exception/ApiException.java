package my.demo.webservice.exception;


import my.demo.webservice.common.web.Code;

/**
 * API异常定义类
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Code code = Code.INTERNAL_ERROR;
    private int codeValue = code.getValue();

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(Code errCode, String message) {
        super(message);
        code = errCode;
        codeValue = errCode.getValue();
    }

    public ApiException(Code errCode) {
        super(errCode.getMessage());
        code = errCode;
        codeValue = errCode.getValue();
    }

    public ApiException(int codeValue, String message) {
        super(message);
        this.codeValue = codeValue;
    }

    public Code getCode() {
        return code;
    }

    public int getCodeValue() {
        return codeValue;
    }

}
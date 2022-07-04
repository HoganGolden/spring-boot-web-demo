package my.demo.webservice.common.web;

public enum Code {

    SUCCESS("正常", 200),
    NOT_FOUND("访问对象不存在", 404),
    INTERNAL_ERROR("内部处理错误", 500),
    RECORD_EXISTS("访问对象已存在", 610),
    UNAUTHORIZED("访问未授权", 601),
    INVALID_PARAM("参数错误", 603),
    PAYMENT_FAILURE("支付失败", 604),
    AUTHORIZATION_FAILURE("授权失败", 605),
    DATE_PARSE_ERROR("日期解析异常", 607),
    OUT_OF_STOCK("暂无库存", 608),
    STATUS_ERROR("状态异常", 609),
    PUSH_STREAM_ERROR("推流异常", 610),
    STOP_PUSH_STREAM_ERROR("停止推流异常", 611);;

    private String message;
    private Integer value;

    Code(String message, Integer value) {
        this.message = message;
        this.value = value;
    }

    public static Code fromValue(int value) {
        for (Code item : Code.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }

    public String getMessage() {
        return message;
    }

    public int getValue() {
        return value;
    }
}
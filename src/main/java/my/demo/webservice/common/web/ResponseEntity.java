package my.demo.webservice.common.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;


/**
 * 响应操作结果
 * <pre>
 *  {
 *      code： 错误码，
 *      msg：错误消息，
 *      data：  响应数据
 *  }
 * </pre>
 *
 * <p>
 * 错误码：
 * <ul>
 * <li> 0，成功；
 * <li> 4xx，前端错误，说明前端开发者需要重新了解后端接口使用规范：
 * <ul>
 * <li> 401，参数错误，即前端没有传递后端需要的参数；
 * <li> 402，参数值错误，即前端传递的参数值不符合后端接收范围。
 * </ul>
 * <li> 5xx，后端错误，除501外，说明后端开发者应该继续优化代码，尽量避免返回后端错误码：
 * <ul>
 * <li> 501，验证失败，即后端要求用户登录；
 * <li> 502，系统内部错误，即没有合适命名的后端内部错误；
 * <li> 503，业务不支持，即后端虽然定义了接口，但是还没有实现功能；
 * <li> 504，更新数据失效，即后端采用了乐观锁更新，而并发更新时存在数据更新失效；
 * <li> 505，更新数据失败，即后端数据库更新失败（正常情况应该更新成功）。
 * </ul>
 * <li> 6xx，App后端业务错误码，
 * <li> 7xx，管理后端业务错误码，
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseEntity<T> implements Serializable {

    private static final long serialVersionUID = 2946260266591132523L;

    private T data;

    private Integer code;
    private String msg;

    public ResponseEntity(T data, HttpStatus status) {
        this.data = data;
        this.code = status.value();
    }

    public ResponseEntity(Integer code, String msg, T data) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    public ResponseEntity(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> ResponseEntity<T> ok() {
        return new ResponseEntity<T>(Code.SUCCESS.getValue(), "成功");
    }

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<T>(Code.SUCCESS.getValue(), "成功", body);
    }

    public static <T> ResponseEntity<ResponseEntityPage<T>> okPage(Page<T> page) {
        return new ResponseEntity<ResponseEntityPage<T>>(Code.SUCCESS.getValue(), "成功", ResponseEntityPage.okPage(page));
    }

    public static <T> ResponseEntity<ResponseEntityPage<T>> okList(List<T> list) {
        return new ResponseEntity<ResponseEntityPage<T>>(Code.SUCCESS.getValue(), "成功", ResponseEntityPage.okList(list));
    }

    public static <T> ResponseEntity<T> fail() {
        return new ResponseEntity<T>(-1, "错误");
    }

    public static <T> ResponseEntity<T> fail(Integer code, String msg) {
        return new ResponseEntity<T>(code, msg);
    }

    public static <T> ResponseEntity<T> fail(Integer code, String msg, T data) {
        return new ResponseEntity<T>(code, msg, data);
    }

    public static <T> ResponseEntity<T> badArgument() {
        return badArgument("参数不对");
    }

    public static <T> ResponseEntity<T> badArgument(String msg) {
        return fail(401, msg);
    }

    public static <T> ResponseEntity<T> badArgumentValue() {
        return fail(402, "参数值不对");
    }

    public static <T> ResponseEntity<T> unlogin() {
        return fail(501, "请登录");
    }

    public static <T> ResponseEntity<T> serious() {
        return fail(502, "系统内部错误");
    }

    public static <T> ResponseEntity<T> unsupport() {
        return fail(503, "业务不支持");
    }

    public static <T> ResponseEntity<T> updatedDateExpired() {
        return fail(504, "更新数据已经失效");
    }

    public static <T> ResponseEntity<T> updatedDataFailed() {
        return fail(505, "更新数据失败");
    }

    public static <T> ResponseEntity<T> unauthz() {
        return fail(506, "无操作权限");
    }

    public static <T> ResponseEntity<T> badJwtToken() {
        return fail(Code.UNAUTHORIZED.getValue(), "API访问授权失败");
    }
}

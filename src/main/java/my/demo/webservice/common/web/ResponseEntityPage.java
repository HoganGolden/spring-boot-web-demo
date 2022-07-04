package my.demo.webservice.common.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Data
public class ResponseEntityPage<T> implements Serializable {

    private static final long serialVersionUID = 2946260266591132524L;

    private List<T> list;
    private Long total;
    private Integer page;
    private Integer limit;
    private Integer pages;


    protected static <T> ResponseEntityPage<T> okPage(Page<T> page) {
        return new ResponseEntityPage<T>(page.getContent(), page.getTotalElements(), page.getNumber(), page.getPageable().getPageSize(), page.getTotalPages());
    }

    protected static <T> ResponseEntityPage<T> okList(List<T> list) {
        return new ResponseEntityPage<T>(list, (long) list.size(), 1, list.size(), 1);
    }
}

package my.demo.webservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String user_name;
    private String real_name;
    private String avatar;
    private String user_type;
    private String role_name;
    private String user_id;
    private String nick_name;
    private String user_uid;
}

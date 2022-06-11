package Domain.UsersManagment.APIs.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserActivityInfoDTO {

    private LocalDateTime activityStart;
    private LocalDateTime activityEnd;
    private String schoolName;
    private String userCity;
    private String schoolCity;

}

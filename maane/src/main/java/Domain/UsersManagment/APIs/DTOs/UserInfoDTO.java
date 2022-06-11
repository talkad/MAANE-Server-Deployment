package Domain.UsersManagment.APIs.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@AllArgsConstructor
public class UserInfoDTO {

    private String lastName;
    private String firstName;
    private String city;
    private int workingDay;

}

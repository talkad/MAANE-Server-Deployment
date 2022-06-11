package Communication.DTOs;

import Domain.UsersManagment.UserStateEnum;
import Persistence.DbDtos.UserDBDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {
    private String username;
    private String workField;
    private String userToRegister;
    private String password;
    private UserStateEnum userStateEnum;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String city;
    private List<String> schools;

    public UserDTO(UserDBDTO userDBDTO) {
        username = userDBDTO.getUsername();
        workField = userDBDTO.getWorkField();
        userStateEnum = userDBDTO.getStateEnum();
        firstName = userDBDTO.getFirstName();
        lastName = userDBDTO.getLastName();
        email = userDBDTO.getEmail();
        phoneNumber = userDBDTO.getPhoneNumber();
        city = userDBDTO.getCity();
        schools = userDBDTO.getSchools();
    }


}

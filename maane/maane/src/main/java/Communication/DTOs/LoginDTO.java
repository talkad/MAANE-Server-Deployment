package Communication.DTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private String currUser;
    private String userToLogin;
    private String password;

    public String getCurrUser() {
        return currUser;
    }

    public String getUserToLogin() {
        return userToLogin;
    }

    public String getPassword() {
        return password;
    }

}
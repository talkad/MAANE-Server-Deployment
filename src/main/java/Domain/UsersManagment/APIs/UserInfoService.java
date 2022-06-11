package Domain.UsersManagment.APIs;

import Domain.CommonClasses.Response;
import Domain.UsersManagment.APIs.DTOs.UserActivityInfoDTO;
import Domain.UsersManagment.APIs.DTOs.UserInfoDTO;

import java.util.List;

public interface UserInfoService {

    Response<Boolean> canGenerateReport(String username);

    Response<UserInfoDTO> getUserInfo(String username);

    Response<List<UserActivityInfoDTO>> getUserActivities(String username, int year, int month);
}

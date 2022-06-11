package AcceptanceTesting.Tests;

import Communication.DTOs.UserDTO;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Response;
import Domain.UsersManagment.UserStateEnum;
import Persistence.DbDtos.SchoolDBDTO;
import Persistence.DbDtos.UserDBDTO;
import Persistence.UserQueries;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserManagementTests extends AcceptanceTests {

    private final String adminName = "admin";
    private final String supervisorName = "supervisor";
    private final String instructorName = "instructor";
    private final String schoolSymbol = "1";


    @Before
    public void setUp(){
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();
        super.setUp(true);
        userBridge.resetDB();
    }

    /**
     * admin create a supervisor in 'tech' workField
     * the supervisor assign instructor and coordinator to the same workField
     * the instructor update its info
     * the admin transfers supervision to the instructor assigned by the supervisor and the supervisor is removed
     */
    @Test
    public void legalSystemUseTest(){
        dataBridge.insertSchool(new SchoolDBDTO("1", "testing school", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 30));
        userBridge.login(adminName);
        userBridge.registerUserBySystemManager(adminName, new UserDTO(adminName, "tech", supervisorName, "1234abcd", UserStateEnum.SUPERVISOR,
                "sup", "visor", "a@gmail.com", "055-555-5555", "Beer Sheva", null), "irrelevant");
        userBridge.login(supervisorName);
        userBridge.registerUser(supervisorName, new UserDTO(supervisorName, "irrelevant", instructorName, "1234abcd", UserStateEnum.INSTRUCTOR,
                "ins", "tructor", "a@gmail.com", "055-555-5555", "Beer Sheva", null));
        Assert.assertTrue(UserQueries.getInstance().userExists(supervisorName));
        userBridge.assignSchoolToUser(supervisorName, instructorName, schoolSymbol);
        Assert.assertEquals("ins", userBridge.getAppointedUsers(supervisorName).getResult().get(0).getFirstName());

        dataBridge.assignCoordinator(supervisorName, "irrelevant", "coord", "inator", "a@gmail.com", "0555555555", schoolSymbol);
        userBridge.login(instructorName);
        userBridge.updateInfo(instructorName, "new_first_name", "new_last_name", "new@gmail.com", "0555555555", "Tel Aviv");
        Assert.assertEquals("new_first_name", userBridge.getUserInfo(instructorName).getResult().getFirstName());

        userBridge.transferSupervisionToExistingUser(adminName, supervisorName, instructorName);
        Assert.assertFalse(UserQueries.getInstance().userExists(supervisorName));
        Assert.assertTrue(UserQueries.getInstance().userExists(instructorName));
        Response<UserDBDTO> insUser = UserQueries.getInstance().getFullUser(instructorName);
        Assert.assertEquals(UserStateEnum.SUPERVISOR, insUser.getResult().getStateEnum());
        Assert.assertEquals("coord", userBridge.getCoordinator(instructorName, "irrelevant", schoolSymbol).getResult().getFirstName());
    }


    /**
     * admin create a supervisor in 'tech' workfield
     * create new goals and survey answers
     * generate a new workplan accordingly
     */
    @Test
    public void workPlanGeneratingTests(){
        // ...
    }



}

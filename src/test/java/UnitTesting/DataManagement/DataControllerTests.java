package UnitTesting.DataManagement;

import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Response;
import Domain.DataManagement.DataController;
import Domain.UsersManagment.UserController;
import Domain.UsersManagment.UserStateEnum;
import Domain.WorkPlan.GoalsManagement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataControllerTests {

    private DataController dataController = DataController.getInstance();
    

    @Before
    public void setup(){
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();

        UserController.getInstance().clearUsers();
        GoalsManagement.getInstance().clearGoals();
        dataController.clearSchools();
        dataController.addOneSchool();
    }

    @Test
    public void assignCoordinatorSuccess(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        String supervisorName = userController.registerUserBySystemManager(adminName, "sup1", "sup11111", UserStateEnum.SUPERVISOR, "", "tech", "", "", "email@gmail.com", "0555555555", "").getResult();
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "email@gmail.com", "0555555555", "");
        Response<Boolean> res = dataController.assignCoordinator(supervisorName, "irrelevant", "coordinator", "1", "email@gmail.com", "0555555555", "1");
        Assert.assertFalse(res.isFailure());
        Assert.assertEquals("coordinator", UserController.getInstance().getCoordinator("sup1", "tech", "1").getResult().getFirstName());
    }

    @Test
    public void assignTwoCoordinatorsToTheSameSchoolAndFieldFail(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        String supervisorName = userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "email@gmail.com", "0555555555", "").getResult();
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "email@gmail.com", "0555555555", "");
        Response<Boolean> res1 = dataController.assignCoordinator(supervisorName, "irrelevant", "coordinator", "1", "email@gmail.com", "0555555555", "1");
        Response<Boolean> res2 = dataController.assignCoordinator(supervisorName, "irrelevant", "coordinator2", "2", "email@gmail.com", "0555555555", "1");

        Assert.assertFalse(res1.isFailure());
        Assert.assertTrue(res2.isFailure());
        Assert.assertEquals("coordinator", userController.getCoordinator(supervisorName, "irrelevent", "1").getResult().getFirstName());
        Assert.assertNotEquals("coordinator2", userController.getCoordinator(supervisorName, "irrelevent", "1").getResult().getFirstName());
    }

    @Test
    public void removeCoordinatorSuccess(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        String supervisorName = userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "email@gmail.com", "0555555555", "").getResult();
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "email@gmail.com", "0555555555", "");
        Response<Boolean> res = dataController.assignCoordinator(supervisorName, "irrelevant", "coordinator", "1", "email@gmail.com", "0555555555", "1");
        Assert.assertFalse(res.isFailure());
        Assert.assertEquals("coordinator", UserController.getInstance().getCoordinator("sup1", "tech", "1").getResult().getFirstName());
        Response<Boolean> res2 = dataController.removeCoordinator(supervisorName, "irrelevant", "1");
        Assert.assertFalse(res2.isFailure());
        Assert.assertNull(UserController.getInstance().getCoordinator("sup1", "tech", "1").getResult());
    }

    @Test
    public void removeCoordinatorFail(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        String supervisorName = userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "email@gmail.com", "0555555555", "").getResult();
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "email@gmail.com", "0555555555", "");
        Response<Boolean> res = dataController.removeCoordinator(supervisorName, "irrelevant", "1");
        Assert.assertTrue(res.isFailure());
        Assert.assertNull(UserController.getInstance().getCoordinator("sup1", "tech", "1").getResult());
    }
}

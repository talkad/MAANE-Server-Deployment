package UnitTesting.UserManagement;

import Communication.DTOs.GoalDTO;
import Communication.DTOs.UserDTO;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Response;
import Domain.UsersManagment.UserController;
import Domain.UsersManagment.UserStateEnum;
import Domain.WorkPlan.GoalsManagement;
import Persistence.DbDtos.UserDBDTO;
import Persistence.UserQueries;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalTime;
import java.util.List;


public class UserControllerTest {

    private PasswordEncoder passwordEncoder;
    private UserController userController;
    private UserQueries userQueries;

    
    @Before
    public void setup(){
        this.passwordEncoder = new BCryptPasswordEncoder();
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();

        userController = UserController.getInstance();
        userController.clearUsers();
        GoalsManagement.getInstance().clearGoals();
        userQueries = UserQueries.getInstance();
    }

    @Test
    public void loginAsSystemManagerSuccess(){
        String adminName = userController.login("admin").getResult();
        Assert.assertTrue(userController.getConnectedUsers().containsKey(adminName));
        Assert.assertTrue(userQueries.userExists(adminName));
    }

    @Test
    public void assigningSupervisorSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.login("sup1");
        Assert.assertTrue(userController.getConnectedUsers().containsKey("sup1"));
        Assert.assertTrue(userQueries.userExists("sup1"));
    }

    @Test
    public void assigningInstructorBySupervisorSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        Assert.assertTrue(userQueries.userExists("ins1"));
        Assert.assertEquals("tech", userQueries.getFullUser("ins1").getResult().getWorkField());
    }

    @Test
    public void emailTest(){//todo add emails to fix it
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.sendCoordinatorEmails("sup1", "https://www.cs.bgu.ac.il/~comp211/Main");
    }

    @Test
    public void assigningInstructorByAdminSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.login("sup1");
        userController.registerUserBySystemManager(adminName, "ins1", "ins1", UserStateEnum.INSTRUCTOR, "sup1", "", "", "", "a@a.com", "0555555555", "");
        Assert.assertTrue(userQueries.userExists("ins1"));
        Assert.assertEquals("tech", userQueries.getFullUser("ins1").getResult().getWorkField());
        Assert.assertTrue(userQueries.getFullUser("sup1").getResult().getAppointments().contains("ins1"));
    }

    @Test
    public void assigningInstructorByAdminToAlreadyAppointedUserFail(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        Response<String> res = userController.registerUserBySystemManager(adminName, "ins1", "ins1", UserStateEnum.INSTRUCTOR, "sup1", "", "", "", "a@a.com", "05555555555", "");
        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void getAppointedUsers(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.registerUser("sup1", "gensup1", "gensup1", UserStateEnum.GENERAL_SUPERVISOR, "", "", "a@a.com", "0555555555", "");
        List<UserDTO> appointees = userController.getAppointedUsers("sup1").getResult();
        Assert.assertEquals(2, appointees.size());
    }

    @Test
    public void assigningSchoolsToInstructorSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.assignSchoolToUser("sup1", "ins1", "1");
        userController.assignSchoolToUser("sup1", "ins1", "2");
        Assert.assertEquals(2, userQueries.getFullUser("ins1").getResult().getSchools().size());
        Assert.assertTrue(userQueries.getFullUser("ins1").getResult().getSchools().contains("1"));
        Assert.assertTrue(userQueries.getFullUser("ins1").getResult().getSchools().contains("2"));
    }

    @Test
    public void removingSchoolsFromInstructorSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.assignSchoolToUser("sup1", "ins1", "1");
        userController.assignSchoolToUser("sup1", "ins1", "2");

        userController.removeSchoolFromUser("sup1", "ins1", "1");
        Assert.assertEquals(1, userQueries.getFullUser("ins1").getResult().getSchools().size());
        Assert.assertFalse(userQueries.getFullUser("ins1").getResult().getSchools().contains("1"));
        Assert.assertTrue(userQueries.getFullUser("ins1").getResult().getSchools().contains("2"));
    }

    @Test
    public void removeAssignedInstructorSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.assignSchoolToUser("sup1", "ins1", "1");
        userController.assignSchoolToUser("sup1", "ins1", "2");

        userQueries.getFullUser("sup1");
        userController.removeUser("sup1", "ins1");
        Assert.assertFalse(userQueries.userExists("ins1"));
        Assert.assertFalse(userQueries.getFullUser("sup1").getResult().getAppointments().contains("ins1"));
    }

    @Test
    public void changePasswordBySupervisorFail(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.changePasswordToUser(adminName, "sup1", "sup111", "sup11");
        Assert.assertFalse(passwordEncoder.matches("sup111", userQueries.getFullUser("sup1").getResult().getPassword()));
    }

    @Test
    public void changePasswordBySupervisorSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.changePasswordToUser(adminName, "sup1", "sup111", "sup111");
        Assert.assertTrue(passwordEncoder.matches("sup111", userQueries.getFullUser("sup1").getResult().getPassword()));//userController.getConnectedUsers().containsKey("ins1"));
    }

    @Test
    public void changePasswordSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.login("sup1");
        userController.changePassword("sup1","sup1", "1234", "1234");
        Assert.assertTrue(passwordEncoder.matches("1234", userQueries.getFullUser("sup1").getResult().getPassword()));//userController.getConnectedUsers().containsKey("ins1"));
    }

    @Test
    public void updateInfoSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.updateInfo("sup1", "1", "", "a@a.com", "0555555555", "");
        Assert.assertEquals("1", userQueries.getFullUser("sup1").getResult().getFirstName());
    }

    @Test
    public void changePasswordToInstructor() {
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.changePasswordToUser("sup1", "ins1", "ins111", "ins111");
        Assert.assertTrue(passwordEncoder.matches("ins111", userQueries.getFullUser("ins1").getResult().getPassword()));//userController.getConnectedUsers().containsKey("ins1"));
    }

    @Test
    public void assigningYeadimSuccess(){
        Integer year = 2022;
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        String supervisorName = userController.login("sup1").getResult();

        userController.addGoal(supervisorName, new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        userController.addGoal(supervisorName, new GoalDTO(2, "goal2", "goal2", 1, 1), year);
        userController.addGoal(supervisorName, new GoalDTO(3, "goal3", "goal3", 1, 1), year);

        Assert.assertEquals(3, userController.getGoals(supervisorName, year).getResult().size());
    }

    @Test
    public void removingGoalSuccess(){
        Integer year = 2022;//"תשפ\"ג";
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        String supervisorName = userController.login("sup1").getResult();

        userController.addGoal(supervisorName, new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        userController.addGoal(supervisorName, new GoalDTO(2, "goal2", "goal2", 1, 1), year);
        userController.addGoal(supervisorName, new GoalDTO(3, "goal3", "goal3", 1, 1), year);

        Assert.assertEquals(3, userController.getGoals(supervisorName, year).getResult().size());
        int goalToRemoveId = userController.getGoals(supervisorName, year).getResult().get(0).getGoalId();
        userController.removeGoal(supervisorName, year, goalToRemoveId);
        Assert.assertEquals(2, userController.getGoals(supervisorName, year).getResult().size());
    }

    @Test
    public void viewAllUsersSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("sup1");
        adminName = userController.login("admin").getResult();
        List<UserDTO> allUsers = userController.getAllUsers(adminName).getResult();
        Assert.assertEquals(allUsers.size(), userQueries.getUsers().size());
    }

    @Test
    public void assigningTwoSupervisorsToTheSameWorkFieldFail(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        Response<String> res = userController.registerUserBySystemManager(adminName, "sup2", "sup2", UserStateEnum.SUPERVISOR, "", "tech", "", "", "", "", "");
        Assert.assertTrue(res.isFailure());
        Assert.assertTrue(userQueries.userExists("sup1"));
        Assert.assertFalse(userQueries.userExists("sup2"));
    }

    @Test
    public void RemovingInstructorAssignedBySupervisorBySystemManagerSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("sup1");
        userController.login("admin");
        Response<Boolean> res = userController.removeUser(adminName, "ins1");
        Assert.assertFalse(res.isFailure());
        Assert.assertFalse(userQueries.userExists("ins1"));
        Assert.assertFalse(userQueries.getFullUser("sup1").getResult().getAppointments().contains("ins1"));
    }

    @Test
    public void transferSupervisionSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("sup1");
        userController.login(adminName);
        Response<Boolean> res = userController.transferSupervision(adminName, "sup1", "new_sup", "new_sup", "", "", "", "", "");
        Assert.assertFalse(res.isFailure());
        Assert.assertTrue(userQueries.userExists("new_sup"));
        Assert.assertFalse(userQueries.userExists("sup1"));
        Assert.assertTrue(userQueries.getFullUser("new_sup").getResult().getAppointments().contains("ins1"));
    }

    @Test
    public void transferSupervisionFail(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("sup1");
        userController.login(adminName);
        Response<Boolean> res = userController.transferSupervision(adminName, "sup1", "sup1", "new_sup", "", "", "", "", "");
        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void transferSupervisionToExistingUserSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("sup1");
        userController.login(adminName);
        Response<Boolean> res = userController.transferSupervisionToExistingUser(adminName, "sup1", "ins1");
        Assert.assertFalse(res.isFailure());
        Assert.assertTrue(userQueries.userExists("ins1"));
        Assert.assertFalse(userQueries.userExists("sup1"));
        Assert.assertTrue(userQueries.getFullUser("ins1").getResult().getAppointments().isEmpty());
        userController.logout(adminName);
        userController.login("ins1");
        userController.registerUser("ins1", "ins2", "ins2", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        Assert.assertTrue(userQueries.userExists("ins2"));
        Assert.assertTrue(userQueries.getFullUser("ins1").getResult().getAppointments().contains("ins2"));
    }


    @Test
    public void transferSupervisionTwiceSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        Response<Boolean> res = userController.transferSupervision(adminName, "sup1", "new_sup", "new_sup", "", "", "", "", "");
        Assert.assertFalse(res.isFailure());
        userController.logout("admin");
        userController.login("new_sup");
        userController.registerUser("new_sup", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("new_sup");
        userController.login("admin");
        Response<Boolean> res2 = userController.transferSupervisionToExistingUser(adminName, "new_sup", "ins1");
        Assert.assertFalse(res2.isFailure());
        Assert.assertTrue(userQueries.userExists("ins1"));
        Assert.assertFalse(userQueries.userExists("sup1"));
        Assert.assertFalse(userQueries.userExists("new_sup"));
        Assert.assertTrue(userQueries.getFullUser("ins1").getResult().getAppointments().isEmpty());
        userController.logout(adminName);
        userController.login("ins1");
        userController.registerUser("ins1", "ins2", "ins2", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        Assert.assertTrue(userQueries.userExists("ins2"));
        Assert.assertTrue(userQueries.getFullUser("ins1").getResult().getAppointments().contains("ins2"));
    }

    @Test
    public void allWorkFieldsSuccess() {
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.registerUserBySystemManager(adminName, "sup2", "sup2", UserStateEnum.SUPERVISOR, "", "English", "", "", "a@a.com", "0555555555", "");
        Response<List<String>> workFieldsRes = userController.allWorkFields(adminName);
        Assert.assertFalse(workFieldsRes.isFailure());
        Assert.assertEquals(2, workFieldsRes.getResult().size());
        Assert.assertTrue(workFieldsRes.getResult().contains("tech"));
        Assert.assertTrue(workFieldsRes.getResult().contains("English"));
    }

    @Test
    public void assigningWorkHoursSuccess(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("sup1");
        userController.login("ins1");
        userController.setWorkingTime("ins1", 1, LocalTime.of(9, 0).toString(), LocalTime.of(11, 0).toString(), LocalTime.of(11, 0).toString(), LocalTime.of(13, 0).toString());
        Response<UserDBDTO> userRes = userQueries.getWorkingTime("ins1");
        Assert.assertFalse(userRes.isFailure());
        Assert.assertEquals(1, userRes.getResult().getWorkDay());
        Assert.assertEquals(0, userRes.getResult().getAct1Start().compareTo(LocalTime.of(9, 0)));
    }

    @Test
    public void assigningWorkHoursFail(){
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
        userController.login("sup1");
        userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "");
        userController.logout("sup1");
        userController.login("ins1");
        Response<Boolean> setWorkTimeRes = userController.setWorkingTime("ins1", 1, LocalTime.of(9, 0).toString(), LocalTime.of(11, 0).toString(), LocalTime.of(10, 0).toString(), LocalTime.of(13, 0).toString());
        Response<UserDBDTO> userRes = userQueries.getWorkingTime("ins1");
        Assert.assertTrue(setWorkTimeRes.isFailure());
        Assert.assertEquals(0, userRes.getResult().getWorkDay());
        Assert.assertNotEquals(0, userRes.getResult().getAct1Start().compareTo(LocalTime.of(9, 0)));
    }
}
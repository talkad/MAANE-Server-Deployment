package UnitTesting.WorkPlan;

import Communication.DTOs.GoalDTO;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Pair;
import Domain.UsersManagment.UserController;
import Domain.UsersManagment.UserStateEnum;
import Domain.WorkPlan.AnnualScheduleGenerator;
import Domain.WorkPlan.GoalsManagement;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;

public class AnnualScheduleGeneratorTest {

    private UserController userController;

    @Before
    public void setup(){
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();

        userController = UserController.getInstance();
        userController.clearUsers();
        GoalsManagement.getInstance().clearGoals();//todo maybe call this from clearUsers

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "a@a.com", "0555555555", "");
        userController.logout(adminName);
    }

    @Test
    public void basicAlgorithmFunctionalitySuccess(){
        Integer year = 2022;
        String supervisorName = userController.login("sup1").getResult();

        userController.addGoal(supervisorName, new GoalDTO(1,"1", "desc", 1,5), year);
        userController.addGoal(supervisorName, new GoalDTO(2, "3", "desc", 1, 3), year);
        userController.addGoal(supervisorName, new GoalDTO(3, "2", "desc", 1,4), year);
        userController.addGoal(supervisorName, new GoalDTO(4, "4","desc", 1, 1), year);

        String instructorName = userController.registerUser("sup1", "ins1", "ins1", UserStateEnum.INSTRUCTOR, "", "", "a@a.com", "0555555555", "").getResult();

        userController.assignSchoolToUser(supervisorName, instructorName, "1");
        userController.assignSchoolToUser(supervisorName, instructorName, "2");

        List<Pair<String, List<Integer>>> schoolsAndFaults = new Vector<>();
        List<Integer> school1Faults = new Vector<>();
        school1Faults.add(1);
        school1Faults.add(3);
        school1Faults.add(4);
        school1Faults.add(2);

        List<Integer> school2Faults = new Vector<>();
        school2Faults.add(4);
        school2Faults.add(2);

        schoolsAndFaults.add(new Pair<>("1", school1Faults));
        schoolsAndFaults.add(new Pair<>("2", school2Faults));
        String workField = userController.getUser(supervisorName).getWorkField();
        AnnualScheduleGenerator.getInstance().algorithmMock(supervisorName, schoolsAndFaults, GoalsManagement.getInstance().getGoals(workField, year).getResult(), year);
        userController.login("ins1");
        userController.viewWorkPlan(instructorName, year, 9).getResult().printMe();
        userController.addActivity("ins1", LocalDateTime.of(2023, 1, 2, 8, 0).toString(), "1", 1, "idc", LocalDateTime.of(2023, 1, 1, 10, 0).toString());
        userController.viewWorkPlan(instructorName, 2023, 1).getResult().printMe();
    }
}

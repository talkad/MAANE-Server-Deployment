package AcceptanceTesting.Tests;

import Communication.DTOs.*;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.FaultDetector.Rules.*;
import Domain.UsersManagment.UserStateEnum;
import Persistence.DbDtos.SchoolDBDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static Domain.DataManagement.AnswerState.AnswerType.*;


public class SupervisorTests extends AcceptanceTests{

    private String supervisorName1;
    private String instructorName1;
    private String instructorName2;
    private SurveyDTO surveyDTO;
    private List<RuleRequestDTO> rulesDTO;
    private SurveyAnswersDTO answersDTO1, answersDTO2, answersDTO3, answersDTO4, answersDTO5, answersDTO6;

    @Before
    public void setUp() {
        super.setUp(true);
        userBridge.resetDB();
        String adminName = "admin";
        supervisorName1 = "supervisor1";
        instructorName1 = "instructor1";
        instructorName2 = "instructor2";
        List<String> ins1Schools = new Vector<>();
        ins1Schools.add("1");
        ins1Schools.add("2");
        ins1Schools.add("3");
        List<String> ins2Schools = new Vector<>();
        ins2Schools.add("4");
        ins2Schools.add("5");
        ins2Schools.add("6");
        userBridge.login(adminName);

        dataBridge.insertSchool(new SchoolDBDTO("1", "testing school", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 30));
        dataBridge.insertSchool(new SchoolDBDTO("2", "testing school2", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 31));
        dataBridge.insertSchool(new SchoolDBDTO("3", "testing school3", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 32));
        dataBridge.insertSchool(new SchoolDBDTO("4", "testing school4", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 33));
        dataBridge.insertSchool(new SchoolDBDTO("5", "testing school5", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 34));
        dataBridge.insertSchool(new SchoolDBDTO("6", "testing school6", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 35));

        userBridge.registerUserBySystemManager(adminName, new UserDTO(adminName, "science", supervisorName1, supervisorName1, UserStateEnum.SUPERVISOR,"Ronit", "Blisco", "ronit@gmail.com", "0501111111", "Tel Aviv", new Vector<>()), "");
        userBridge.registerUserBySystemManager(adminName, new UserDTO(adminName, "", instructorName1, instructorName1, UserStateEnum.INSTRUCTOR, "dan", "dani", "dan@gmail.com", "0501111111", "Tel Aviv", ins1Schools), supervisorName1);
        userBridge.registerUserBySystemManager(adminName, new UserDTO(adminName, "", instructorName2, instructorName2, UserStateEnum.INSTRUCTOR, "ben", "beni", "ben@gmail.com", "0501111111", "Tel Aviv", ins2Schools), supervisorName1);
        userBridge.logout("admin");
        userBridge.login(supervisorName1);

        surveyDTO = new SurveyDTO();

        List<String> questions = Arrays.asList("symbol" ,"is there research performed in the school?", "does the school provide at least 4 private hours a week?", "is there maintenance every week?", "number of students in class");
        List<List<String>> possibleAnswers = Arrays.asList(new LinkedList<>(), Arrays.asList("No", "Yes"), Arrays.asList("No", "Yes"), Arrays.asList("No", "Yes"), new LinkedList<>());
        List<AnswerType> questionTypes = Arrays.asList(OPEN_ANSWER, MULTIPLE_CHOICE, MULTIPLE_CHOICE, MULTIPLE_CHOICE, NUMERIC_ANSWER);

        rulesDTO = Arrays.asList(new RuleRequestDTO(1, new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.NONE, 1, List.of(1))),
                new RuleRequestDTO(2, new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.NONE, 2, List.of(1))),
                new RuleRequestDTO(3, new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.NONE, 3, List.of(1))),
                new RuleRequestDTO(4, new RuleDTO(null, RuleType.NUMERIC, Comparison.GREATER_THAN, 4, List.of(30)))
                );

        surveyDTO.setId("");
        surveyDTO.setTitle("testing survey");
        surveyDTO.setDescription("testing survey");
        surveyDTO.setQuestions(questions);
        surveyDTO.setTypes(questionTypes);
        surveyDTO.setAnswers(possibleAnswers);

        answersDTO1 = new SurveyAnswersDTO();
        List<String> answers1 = Arrays.asList("1", "0", "0", "0", "10");
        answersDTO1.setAnswers(answers1);
        answersDTO1.setTypes(questionTypes);
        answersDTO1.setId("");

        answersDTO2 = new SurveyAnswersDTO();
        List<String> answers2 = Arrays.asList("2", "1", "0", "1", "20");
        answersDTO2.setAnswers(answers2);
        answersDTO2.setTypes(questionTypes);
        answersDTO2.setId("");

        answersDTO3 = new SurveyAnswersDTO();
        List<String> answers3 = Arrays.asList("3", "1", "0", "1", "30");
        answersDTO3.setAnswers(answers3);
        answersDTO3.setTypes(questionTypes);
        answersDTO3.setId("");


        answersDTO4 = new SurveyAnswersDTO();
        List<String> answers4 = Arrays.asList("4", "0", "0", "1", "40");
        answersDTO4.setAnswers(answers4);
        answersDTO4.setTypes(questionTypes);
        answersDTO4.setId("");


        answersDTO5 = new SurveyAnswersDTO();
        List<String> answers5 = Arrays.asList("5", "1", "1", "1", "50");
        answersDTO5.setAnswers(answers5);
        answersDTO5.setTypes(questionTypes);
        answersDTO5.setId("");


        answersDTO6 = new SurveyAnswersDTO();
        List<String> answers6 = Arrays.asList("6", "1", "0", "1", "60");
        answersDTO6.setAnswers(answers6);
        answersDTO6.setTypes(questionTypes);
        answersDTO6.setId("");
    }

    @Test
    public void testSetUp() {
        Assert.assertFalse(userBridge.getUserRes(supervisorName1).isFailure());
        Assert.assertFalse(userBridge.getUserRes(instructorName1).isFailure());
        Assert.assertFalse(userBridge.getUserRes(instructorName2).isFailure());
        List<UserDTO> appointedUsers = userBridge.getAppointedUsers(supervisorName1).getResult();
        for (UserDTO u: appointedUsers) {
            Assert.assertTrue(u.getFirstName().equals("dan") || u.getFirstName().equals("ben"));
        }
    }

    @Test
    public void workPlanTest(){
        Integer year = 2022;
        userBridge.login(supervisorName1);
        dataBridge.assignCoordinator(supervisorName1, "irrelevant", "coor1", "dinator1", "a@gmail.com", "0555555555", "1");
        dataBridge.assignCoordinator(supervisorName1, "irrelevant", "coor2", "dinator2", "a@gmail.com", "0555555555", "2");
        dataBridge.assignCoordinator(supervisorName1, "irrelevant", "coor3", "dinator3", "a@gmail.com", "0555555555", "3");
        dataBridge.assignCoordinator(supervisorName1, "irrelevant", "coor4", "dinator4", "a@gmail.com", "0555555555", "4");
        dataBridge.assignCoordinator(supervisorName1, "irrelevant", "coor5", "dinator5", "a@gmail.com", "0555555555", "5");
        dataBridge.assignCoordinator(supervisorName1, "irrelevant", "coor6", "dinator6", "a@gmail.com", "0555555555", "6");

        userBridge.addGoal(supervisorName1, new GoalDTO(1, "research", "", 1, 1), year);
        userBridge.addGoal(supervisorName1, new GoalDTO(2, "private hours", "", 1, 2), year);
        userBridge.addGoal(supervisorName1, new GoalDTO(3, "maintenance", "", 1, 3), year);
        userBridge.addGoal(supervisorName1, new GoalDTO(4, "teaching equipment", "", 1, 4), year);

        Response<String> res = surveyBridge.createSurvey(supervisorName1, surveyDTO);
        surveyBridge.addRule(supervisorName1, res.getResult(), rulesDTO);
        surveyBridge.submitSurvey(supervisorName1, res.getResult());
        answersDTO1.setId(res.getResult());
        answersDTO2.setId(res.getResult());
        answersDTO3.setId(res.getResult());
        answersDTO4.setId(res.getResult());
        answersDTO5.setId(res.getResult());
        answersDTO6.setId(res.getResult());

        surveyBridge.addAnswers(answersDTO1);
        surveyBridge.addAnswers(answersDTO2);
        surveyBridge.addAnswers(answersDTO3);
        surveyBridge.addAnswers(answersDTO4);
        surveyBridge.addAnswers(answersDTO5);
        surveyBridge.addAnswers(answersDTO6);

        userBridge.login(instructorName1);
        userBridge.login(instructorName2);
        userBridge.assignSchoolToUser(supervisorName1, instructorName1, "1");
        userBridge.assignSchoolToUser(supervisorName1, instructorName1, "2");
        userBridge.assignSchoolToUser(supervisorName1, instructorName1, "3");

        userBridge.assignSchoolToUser(supervisorName1, instructorName2, "4");
        userBridge.assignSchoolToUser(supervisorName1, instructorName2, "5");
        userBridge.assignSchoolToUser(supervisorName1, instructorName2, "6");

        userBridge.setWorkingTime(instructorName1, 1, LocalTime.of(10, 0).toString(), LocalTime.of(12, 0).toString(),
                LocalTime.of(12, 0).toString(), LocalTime.of(14, 0).toString());

        scheduleBridge.generateSchedule(supervisorName1, res.getResult());

        userBridge.login(instructorName1);
        userBridge.login(instructorName2);

        Response<WorkPlanDTO> ins1WorkPlanSeptemberRes = userBridge.viewWorkPlan(instructorName1, year, 9);
        Response<WorkPlanDTO> ins2WorkPlanSeptemberRes = userBridge.viewWorkPlan(instructorName2, year, 9);

        Assert.assertFalse(ins1WorkPlanSeptemberRes.isFailure());
        Assert.assertFalse(ins2WorkPlanSeptemberRes.isFailure());
        ins1WorkPlanSeptemberRes.getResult().getCalendar().sort((o1, o2) -> { return o1.getFirst().isBefore(o2.getFirst()) ? -1 : 1 ; });
        ins2WorkPlanSeptemberRes.getResult().getCalendar().sort((o1, o2) -> { return o1.getFirst().isBefore(o2.getFirst()) ? -1 : 1 ; });

        List<Pair<LocalDateTime, ActivityDTO>> ins1CalendarSeptember = ins1WorkPlanSeptemberRes.getResult().getCalendar();

        Assert.assertEquals(ins1CalendarSeptember.get(0).getFirst(), LocalDateTime.of(2022, 9, 5, 10, 0));
        Assert.assertEquals(3, (int) ins1CalendarSeptember.get(0).getSecond().getGoalId());

        Assert.assertEquals(ins1CalendarSeptember.get(1).getFirst(), LocalDateTime.of(2022, 9, 5, 12, 0));
        Assert.assertEquals(3, (int) ins1CalendarSeptember.get(1).getSecond().getGoalId());

        Assert.assertEquals(ins1CalendarSeptember.get(2).getFirst(), LocalDateTime.of(2022, 9, 12, 10, 0));
        Assert.assertEquals(1, (int) ins1CalendarSeptember.get(2).getSecond().getGoalId());

        Assert.assertEquals(ins1CalendarSeptember.get(3).getFirst(), LocalDateTime.of(2022, 9, 12, 12, 0));
        Assert.assertEquals(1, (int) ins1CalendarSeptember.get(3).getSecond().getGoalId());

        List<Pair<LocalDateTime, ActivityDTO>> ins2Calendar = ins2WorkPlanSeptemberRes.getResult().getCalendar();

        Assert.assertEquals(ins2Calendar.get(0).getFirst(), LocalDateTime.of(2022, 9, 4, 8, 0));
        Assert.assertEquals(4, (int) ins2Calendar.get(0).getSecond().getGoalId());

        Assert.assertEquals(ins2Calendar.get(1).getFirst(), LocalDateTime.of(2022, 9, 4, 10, 0));
        Assert.assertEquals(4, (int) ins2Calendar.get(1).getSecond().getGoalId());

        Assert.assertEquals(ins2Calendar.get(2).getFirst(), LocalDateTime.of(2022, 9, 11, 8, 0));
        Assert.assertEquals(4, (int) ins2Calendar.get(2).getSecond().getGoalId());

        Assert.assertEquals(ins2Calendar.get(3).getFirst(), LocalDateTime.of(2022, 9, 11, 10, 0));
        Assert.assertEquals(3, (int) ins2Calendar.get(3).getSecond().getGoalId());

        Assert.assertEquals(ins2Calendar.get(4).getFirst(), LocalDateTime.of(2022, 9, 18, 8, 0));
        Assert.assertEquals(3, (int) ins2Calendar.get(4).getSecond().getGoalId());

        Assert.assertEquals(ins2Calendar.get(5).getFirst(), LocalDateTime.of(2022, 9, 18, 10, 0));
        Assert.assertEquals(3, (int) ins2Calendar.get(5).getSecond().getGoalId());

        Response<WorkPlanDTO> ins2WPOctoberRes = userBridge.viewWorkPlan(instructorName2, year, 10); //has 9 activities so 1 reached october
        List<Pair<LocalDateTime, ActivityDTO>> ins2CalendarOctober = ins2WPOctoberRes.getResult().getCalendar();

        Assert.assertEquals(ins2CalendarOctober.get(0).getFirst(), LocalDateTime.of(2022, 10, 2, 8, 0));
        Assert.assertEquals(2, (int) ins2CalendarOctober.get(0).getSecond().getGoalId());

        Assert.assertEquals(ins2CalendarOctober.get(1).getFirst(), LocalDateTime.of(2022, 10, 2, 10, 0));
        Assert.assertEquals(1, (int) ins2CalendarOctober.get(1).getSecond().getGoalId());

        Assert.assertEquals(ins2CalendarOctober.get(2).getFirst(), LocalDateTime.of(2022, 10, 23, 8, 0));
        Assert.assertEquals(1, (int) ins2CalendarOctober.get(2).getSecond().getGoalId());
    }
}

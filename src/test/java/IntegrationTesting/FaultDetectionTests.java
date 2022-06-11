package IntegrationTesting;

import Communication.DTOs.*;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.FaultDetector.Rules.Comparison;
import Domain.DataManagement.FaultDetector.Rules.RuleType;
import Domain.DataManagement.SurveyController;
import Domain.UsersManagment.UserController;
import Domain.UsersManagment.UserStateEnum;
import Persistence.SurveyDAO;
import Persistence.UserQueries;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static Domain.DataManagement.AnswerState.AnswerType.*;
import static Domain.DataManagement.AnswerState.AnswerType.MULTIPLE_CHOICE;
import static org.mockito.Mockito.when;

public class FaultDetectionTests {

    private SurveyDTO surveyDTO, surveyDTO3;
    private SurveyAnswersDTO answersDTO1,  answersDTO3;

    @InjectMocks
    private SurveyController surveyController;

    @Mock
    private SurveyDAO surveyDAO;

    @Before
    public void setUp(){
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();

        MockitoAnnotations.openMocks(this);

        surveyDTO = new SurveyDTO();
        surveyDAO.clearCache();

        UserQueries.getInstance().clearDB();
        UserController.getInstance().clearUsers();

        List<String> questions1 = Arrays.asList("que1", "que2", "que3");
        List<List<String>> answers1 = Arrays.asList(new LinkedList<>(), Arrays.asList("1", "2"), Arrays.asList("1", "2"));
        List<AnswerType> types1 = Arrays.asList(NUMERIC_ANSWER, MULTIPLE_CHOICE, MULTIPLE_CHOICE);

        surveyDTO.setId("");
        surveyDTO.setTitle("title");
        surveyDTO.setDescription("description");
        surveyDTO.setQuestions(questions1);
        surveyDTO.setTypes(types1);
        surveyDTO.setAnswers(answers1);

        surveyDTO3 = new SurveyDTO();

        List<String> questions5 = Arrays.asList("symbol", "que1", "que2");
        List<List<String>> answers5 = Arrays.asList(new LinkedList<>(), Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3"));
        List<AnswerType> types5 = Arrays.asList(NUMERIC_ANSWER, MULTIPLE_CHOICE, MULTIPLE_CHOICE);

        surveyDTO3.setId("");
        surveyDTO3.setTitle("title");
        surveyDTO3.setDescription("description");
        surveyDTO3.setQuestions(questions5);
        surveyDTO3.setTypes(types5);
        surveyDTO3.setAnswers(answers5);

        // legal answer
        answersDTO1 = new SurveyAnswersDTO();

        List<String> answers2 = Arrays.asList("30", "1", "2");
        List<AnswerType> types2 = Arrays.asList(NUMERIC_ANSWER, MULTIPLE_CHOICE, MULTIPLE_CHOICE);
        answersDTO1.setId("");
        answersDTO1.setAnswers(answers2);
        answersDTO1.setTypes(types2);


        // legal answer
        answersDTO3 = new SurveyAnswersDTO();

        List<String> answers4 = Arrays.asList("300000", "1");
        List<AnswerType> types4 = Arrays.asList(OPEN_ANSWER, MULTIPLE_CHOICE);
        answersDTO3.setId("");
        answersDTO3.setAnswers(answers4);
        answersDTO3.setTypes(types4);

    }

    @Test
    public void getAnswerSuccess(){
        Integer year = 2022;
        Response<SurveyStatsDTO> stats;

        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit12", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);
        answersDTO1.setId(res.getResult());
        surveyController.submitSurvey("Dvorit", res.getResult());

        List<SurveyAnswersDTO> answersDTOS = Arrays.asList(answersDTO1, answersDTO1);

        List<Pair<RuleDTO, Integer>> ruleDTOS = List.of(
                new Pair<>(new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.GREATER_THAN, 1, List.of(0)), 0)
        );

        when(surveyDAO.getRules(res.getResult())).thenReturn(ruleDTOS);
        when(surveyDAO.getAnswers(res.getResult())).thenReturn(answersDTOS);

        UserController.getInstance().addGoal("Dvorit", new GoalDTO(0, "goal0", "goal0", 1,1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(2, "goal2", "goal2", 1,1), year);

        stats = surveyController.getSurveyStats("Dvorit", res.getResult());

        Assert.assertEquals(2, stats.getResult().getSymbols().size());
    }

    @Test
    public void getAnswerNoPermissionFailure(){
        Integer year = 2022;
        Response<SurveyStatsDTO> stats;

        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit12", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.registerUserBySystemManager(adminName, "Levana", "zohariM333", UserStateEnum.SUPERVISOR, "", "hightech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");
        userController.login("Levana");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);
        answersDTO1.setId(res.getResult());
        surveyController.submitSurvey("Dvorit", res.getResult());

        List<SurveyAnswersDTO> answersDTOS = Arrays.asList(answersDTO1, answersDTO1);

        List<Pair<RuleDTO, Integer>> ruleDTOS = List.of(
                new Pair<>(new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.GREATER_THAN, 1, List.of(0)), 0)
        );

        when(surveyDAO.getRules(res.getResult())).thenReturn(ruleDTOS);
        when(surveyDAO.getAnswers(res.getResult())).thenReturn(answersDTOS);

        UserController.getInstance().addGoal("Dvorit", new GoalDTO(0, "goal0", "goal0", 1,1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(2, "goal2", "goal2", 1,1), year);

        stats = surveyController.getSurveyStats("Levana", res.getResult());

        Assert.assertTrue(stats.isFailure());
        Assert.assertNull(stats.getResult());
    }

    @Test
    public void getAnswerNotUserFailure(){
        Integer year = 2022;
        Response<SurveyStatsDTO> stats;

        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit12", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);
        answersDTO1.setId(res.getResult());
        surveyController.submitSurvey("Dvorit", res.getResult());

        List<SurveyAnswersDTO> answersDTOS = Arrays.asList(answersDTO1, answersDTO1);

        List<Pair<RuleDTO, Integer>> ruleDTOS = List.of(
                new Pair<>(new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.GREATER_THAN, 1, List.of(0)), 0)
        );

        when(surveyDAO.getRules(res.getResult())).thenReturn(ruleDTOS);
        when(surveyDAO.getAnswers(res.getResult())).thenReturn(answersDTOS);

        UserController.getInstance().addGoal("Dvorit", new GoalDTO(0, "goal0", "goal0", 1,1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(2, "goal2", "goal2", 1,1), year);

        stats = surveyController.getSurveyStats("NewUser", res.getResult());

        Assert.assertTrue(stats.isFailure());
        Assert.assertNull(stats.getResult());
    }


    @Test
    public void getStatisticsSuccess(){
        Integer year = 2022;
        Response<SurveyStatsDTO> stats;

        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO3);
        answersDTO3.setId(res.getResult());
        surveyController.submitSurvey("Dvorit", res.getResult());

        List<SurveyAnswersDTO> answersDTOS = Arrays.asList(answersDTO3, answersDTO3);

        List<Pair<RuleDTO, Integer>> ruleDTOS = List.of(
                new Pair<>(new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.GREATER_THAN, 1, List.of(0)), 0)
        );

        when(surveyDAO.getRules(res.getResult())).thenReturn(ruleDTOS);
        when(surveyDAO.getAnswers(res.getResult())).thenReturn(answersDTOS);

        UserController.getInstance().addGoal("Dvorit", new GoalDTO(0, "goal0", "goal0", 1,1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(2, "goal2", "goal2", 1,1), year);

        stats = surveyController.getSurveyStats("Dvorit", res.getResult());

        Assert.assertEquals(2, stats.getResult().getSymbols().size());
    }

    @Test
    public void getStatisticsNoPermissionFailure(){
        Integer year = 2022;
        Response<SurveyStatsDTO> stats;

        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.registerUserBySystemManager(adminName, "Levana", "zohariM333", UserStateEnum.SUPERVISOR, "", "hightech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");
        userController.login("Levana");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO3);
        answersDTO3.setId(res.getResult());
        surveyController.submitSurvey("Dvorit", res.getResult());

        List<SurveyAnswersDTO> answersDTOS = Arrays.asList(answersDTO3, answersDTO3);

        List<Pair<RuleDTO, Integer>> ruleDTOS = List.of(
                new Pair<>(new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.GREATER_THAN, 1, List.of(0)), 0)
        );

        when(surveyDAO.getRules(res.getResult())).thenReturn(ruleDTOS);
        when(surveyDAO.getAnswers(res.getResult())).thenReturn(answersDTOS);

        UserController.getInstance().addGoal("Dvorit", new GoalDTO(0, "goal0", "goal0", 1,1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(2, "goal2", "goal2", 1,1), year);

        stats = surveyController.getSurveyStats("Levana", res.getResult());

        Assert.assertTrue(stats.isFailure());
    }

    @Test
    public void getStatisticsNoUserFailure(){
        Integer year = 2022;
        Response<SurveyStatsDTO> stats;

        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO3);
        answersDTO3.setId(res.getResult());
        surveyController.submitSurvey("Dvorit", res.getResult());

        List<SurveyAnswersDTO> answersDTOS = Arrays.asList(answersDTO3, answersDTO3);

        List<Pair<RuleDTO, Integer>> ruleDTOS = List.of(
                new Pair<>(new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.GREATER_THAN, 1, List.of(0)), 0)
        );

        when(surveyDAO.getRules(res.getResult())).thenReturn(ruleDTOS);
        when(surveyDAO.getAnswers(res.getResult())).thenReturn(answersDTOS);

        UserController.getInstance().addGoal("Dvorit", new GoalDTO(0, "goal0", "goal0", 1,1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(2, "goal2", "goal2", 1,1), year);

        stats = surveyController.getSurveyStats("newUser", res.getResult());

        Assert.assertTrue(stats.isFailure());
    }

    @Test
    public void getStatisticsNotSubmittedSurveyFailure(){
        Integer year = 2022;
        Response<SurveyStatsDTO> stats;

        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO3);
        answersDTO3.setId(res.getResult());

        List<SurveyAnswersDTO> answersDTOS = Arrays.asList(answersDTO3, answersDTO3);

        List<Pair<RuleDTO, Integer>> ruleDTOS = List.of(
                new Pair<>(new RuleDTO(null, RuleType.MULTIPLE_CHOICE, Comparison.GREATER_THAN, 1, List.of(0)), 0)
        );

        when(surveyDAO.getRules(res.getResult())).thenReturn(ruleDTOS);
        when(surveyDAO.getAnswers(res.getResult())).thenReturn(answersDTOS);

        UserController.getInstance().addGoal("Dvorit", new GoalDTO(0, "goal0", "goal0", 1,1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(1, "goal1", "goal1", 1, 1), year);
        UserController.getInstance().addGoal("Dvorit", new GoalDTO(2, "goal2", "goal2", 1,1), year);

        stats = surveyController.getSurveyStats("Dvorit", res.getResult());

        Assert.assertTrue(stats.isFailure());
    }

}

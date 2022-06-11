package IntegrationTesting;

import Communication.DTOs.SurveyAnswersDTO;
import Communication.DTOs.SurveyDTO;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.FaultDetector.Rules.Comparison;
import Domain.DataManagement.FaultDetector.Rules.NumericBaseRule;
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

import static Domain.DataManagement.AnswerState.AnswerType.MULTIPLE_CHOICE;
import static Domain.DataManagement.AnswerState.AnswerType.NUMERIC_ANSWER;
import static org.mockito.Mockito.when;

public class SurveyIntegrationTests {

    private SurveyDTO surveyDTO;
    private SurveyAnswersDTO answersDTO;

    @InjectMocks
    private SurveyController surveyController;

    @Mock
    private SurveyDAO surveyDAO;


    @Before
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        surveyDTO = new SurveyDTO();
        surveyDAO.clearCache();
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();
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

        answersDTO = new SurveyAnswersDTO();

        List<String> answers2 = Arrays.asList("30", "1", "2");
        List<AnswerType> types2 = Arrays.asList(NUMERIC_ANSWER, MULTIPLE_CHOICE, MULTIPLE_CHOICE);

        answersDTO.setId("");
        answersDTO.setAnswers(answers2);
        answersDTO.setTypes(types2);

    }

    @Test
    public void surveyCreationSuccess(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login( "admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.login("Dvorit");

        Assert.assertFalse(surveyController.createSurvey("Dvorit", surveyDTO).isFailure());
    }

    @Test
    public void surveyCreationPermissionFailure(){
        UserController userController = UserController.getInstance();
        userController.login("admin");

        userController.registerUserBySystemManager("admin", "sup1", "sup1", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.registerUserBySystemManager("admin", "Shlomit", "Malka", UserStateEnum.INSTRUCTOR, "sup1", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.login("Shlomit");

        Assert.assertTrue(surveyController.createSurvey("Shlomit", surveyDTO).isFailure());
    }

    @Test
    public void faultDetectionSuccess(){
        Response<List<List<String>>> faults;
        Integer year = 2022;// "תשפ\"ג";

        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);
        answersDTO.setId(res.getResult());
        surveyController.addAnswers(answersDTO);

        faults = surveyController.detectFault("Dvorit", res.getResult(), year);

        Assert.assertFalse(faults.isFailure());
    }

    @Test
    public void faultDetectionWrongSupervisorFailure(){
        Response<List<List<String>>> faults;
        Integer year = 2022;// "תשפ\"ג";

        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.registerUserBySystemManager(adminName, "Shosh", "Bar", UserStateEnum.SUPERVISOR, "", "hebrew", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);
        answersDTO.setId(res.getResult());
        surveyController.addAnswers(answersDTO);

        faults = surveyController.detectFault("Shosh", res.getResult(), year);

        Assert.assertTrue(faults.isFailure());
    }

    @Test
    public void addRuleSuccess(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.login("Dvorit");

        Response<String> res2 = surveyController.createSurvey("Dvorit", surveyDTO);
        Response<Boolean> res = surveyController.addRule("Dvorit", res2.getResult(), new NumericBaseRule(0, Comparison.GREATER_THAN, 28), 0);

        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void addRuleNoPermissionFailure(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.registerUserBySystemManager(adminName, "Levana", "Zoharim", UserStateEnum.SUPERVISOR, "", "hebrew", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.login("Dvorit");

        Response<String> res2 = surveyController.createSurvey("Dvorit", surveyDTO);
        Response<Boolean> res = surveyController.addRule("Levana", res2.getResult(), new NumericBaseRule(0, Comparison.GREATER_THAN, 28), 0);

        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void removeRuleSuccess(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.login("Dvorit");

        Response<String> res2 = surveyController.createSurvey("Dvorit", surveyDTO);
        surveyController.addRule("Dvorit", res2.getResult(), new NumericBaseRule(0, Comparison.GREATER_THAN, 28), 0);

        when(surveyDAO.removeRule(0)).thenReturn(new Response<>(true, false, "OK"));

        Response<Boolean> res = surveyController.removeRule("Dvorit", res2.getResult(), 0);

        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void removeRuleNotExistsFailure(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.login("Dvorit");

        Response<String> res2 = surveyController.createSurvey("Dvorit", surveyDTO);
        when(surveyDAO.removeRule(0)).thenReturn(new Response<>(false, true, "rule not in db"));

        Response<Boolean> res = surveyController.removeRule("Dvorit", res2.getResult(), 0);

        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void removeRuleNoPermissionFailure(){
        UserController userController = UserController.getInstance();
        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.registerUserBySystemManager(adminName, "Levana", "Zoharim", UserStateEnum.SUPERVISOR, "", "hebrew", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.login("Dvorit");

        Response<String> res2 = surveyController.createSurvey("Dvorit", surveyDTO);
        surveyController.addRule("Dvorit", res2.getResult(), new NumericBaseRule(0, Comparison.GREATER_THAN, 28), 0);

        when(surveyDAO.removeRule(0)).thenReturn(new Response<>(false, true, "fail"));

        Response<Boolean> res = surveyController.removeRule("Levana", res2.getResult(), 0);

        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void RemoveQuestionsExistingSurveySuccess(){
        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);

        // delete all question
        surveyDTO.setTypes(new LinkedList<>());
        surveyDTO.setAnswers(new LinkedList<>());
        surveyDTO.setQuestions(new LinkedList<>());
        surveyDTO.setId(res.getResult());

        res = surveyController.createSurvey("Dvorit", surveyDTO);
        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void AddQuestionsExistingSurveySuccess(){
        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);

        // add new question to survey
        List<String> ques = new LinkedList<>(surveyDTO.getQuestions());
        List<List<String>> ans = new LinkedList<>(surveyDTO.getAnswers());
        List<AnswerType> types = new LinkedList<>(surveyDTO.getTypes());

        ques.add("new question");
        ans.add(new LinkedList<>());
        types.add(NUMERIC_ANSWER);

        // update survey questions
        surveyDTO.setTypes(types);
        surveyDTO.setAnswers(ans);
        surveyDTO.setQuestions(ques);
        surveyDTO.setId(res.getResult());

        res = surveyController.createSurvey("Dvorit", surveyDTO);
        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void updateExistingSurveyInvalidUserFailure(){
        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");
        userController.registerUserBySystemManager(adminName, "Levana", "Levana", UserStateEnum.GENERAL_SUPERVISOR, "Dvorit", "Levana", "", "", "Levana@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");
        userController.login("Levana");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);

        // delete all question
        surveyDTO.setTypes(new LinkedList<>());
        surveyDTO.setAnswers(new LinkedList<>());
        surveyDTO.setQuestions(new LinkedList<>());
        surveyDTO.setId(res.getResult());

        ServerContextInitializer.getInstance().setMockMode(false);

        res = surveyController.createSurvey("Levana", surveyDTO);

        ServerContextInitializer.getInstance().setMockMode(true);

        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void updateSurveyAfterSubmissionFailure(){
        UserController userController = UserController.getInstance();

        String adminName = userController.login("admin").getResult();
        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "dvorit@gmail.com", "055-555-5555", "");

        userController.logout(adminName);

        userController.login("Dvorit");

        Response<String> res = surveyController.createSurvey("Dvorit", surveyDTO);

        surveyController.submitSurvey("Dvorit", res.getResult());

        // delete all question
        surveyDTO.setTypes(new LinkedList<>());
        surveyDTO.setAnswers(new LinkedList<>());
        surveyDTO.setQuestions(new LinkedList<>());
        surveyDTO.setId(res.getResult());

        ServerContextInitializer.getInstance().setMockMode(false);

        res = surveyController.createSurvey("Dvorit", surveyDTO);

        ServerContextInitializer.getInstance().setMockMode(true);

        Assert.assertTrue(res.isFailure());
    }

}

package UnitTesting.DataBase;

import Communication.DTOs.SurveyAnswersDTO;
import Communication.DTOs.SurveyDTO;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Persistence.SurveyDAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class SurveyDbTests {
    SurveyDAO surveyQueries;
    SurveyDTO surveyDTO;

    @Before
    public void setUp(){
        surveyQueries = SurveyDAO.getInstance();

        List<String> questions = new LinkedList<>();
        questions.add("q1"); questions.add("q2");

        List <AnswerType> answerTypes = new LinkedList<>();
        answerTypes.add(AnswerType.MULTIPLE_CHOICE); answerTypes.add(AnswerType.OPEN_ANSWER);

        List <List<String>> answers = new LinkedList<>();
        List <String> answers1 = new LinkedList<>();
        answers1.add("a"); answers1.add("20"); answers1.add("30"); answers1.add("40");
        List <String> answers2 = new LinkedList<>();
        answers2.add("keep my wife's name, out your f* mouth");
        answers.add(answers1); answers.add(answers2);

        surveyDTO = new SurveyDTO(false, "0","survey1", "some desc", questions, answers, answerTypes, 2022);
    }

    @Test
    public void insertSurvey() throws SQLException {//todo tal cannot throw exception and test is bugged
        Assert.assertTrue(surveyQueries.insertSurvey(surveyDTO).isFailure());
    }

    @Test
    public void getSurvey() throws SQLException {//todo tal cannot throw exception and test is bugged
        Response<SurveyDTO> surveyDTO = surveyQueries.getSurvey("1");
        Assert.assertEquals(4, surveyDTO.getResult().getQuestions().size() + surveyDTO.getResult().getAnswers().size());
    }

    @Test
    public void insertAnswer() throws SQLException {//todo cannot throw exception
        List<String> answers = new LinkedList<>();
        answers.add("a1"); answers.add("a2");

        List<AnswerType> types = new LinkedList<>();
        types.add(AnswerType.OPEN_ANSWER); types.add(AnswerType.NUMERIC_ANSWER);

        surveyQueries.insertCoordinatorAnswers("1", "lala", answers, types);
    }

    @Test
    public void getAnswers() throws SQLException {//todo make it a test
        List<SurveyAnswersDTO> surveyAnswersDTOS = surveyQueries.getAnswers("1");
        int checkMe;
    }

}


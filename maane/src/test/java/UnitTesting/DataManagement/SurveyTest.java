package UnitTesting.DataManagement;

import Communication.DTOs.SurveyDTO;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.Survey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static Domain.DataManagement.AnswerState.AnswerType.*;

public class SurveyTest {

    private SurveyDTO surveyDTO1;
    private SurveyDTO surveyDTO2;
    private SurveyDTO surveyDTO3;
    private Survey survey;

    @Before
    public void setUp(){
        survey = null;

        // legal surveyDTO
        surveyDTO1 = new SurveyDTO();

        List<String> questions1 = Arrays.asList("que1", "que2", "que3");
        List<List<String>> answers1 = Arrays.asList(new LinkedList<>(), new LinkedList<>(), Arrays.asList("1", "2"));
        List<AnswerType> types1 = Arrays.asList(OPEN_ANSWER, NUMERIC_ANSWER, MULTIPLE_CHOICE);;

        surveyDTO1.setId("");
        surveyDTO1.setTitle("title");
        surveyDTO1.setDescription("description");
        surveyDTO1.setQuestions(questions1);
        surveyDTO1.setTypes(types1);
        surveyDTO1.setAnswers(answers1);

        // illegal surveyDTO
        surveyDTO2 = new SurveyDTO();

        List<String> questions2 = Arrays.asList("que1", "que2");
        List<List<String>> answers2 = Arrays.asList(new LinkedList<>(), List.of(""));
        List<AnswerType> types2 = Arrays.asList(OPEN_ANSWER, MULTIPLE_CHOICE);;

        surveyDTO2.setId("");
        surveyDTO2.setTitle("title");
        surveyDTO2.setDescription("description");
        surveyDTO2.setQuestions(questions2);
        surveyDTO2.setTypes(types2);
        surveyDTO2.setAnswers(answers2);

        // illegal surveyDTO
        surveyDTO3 = new SurveyDTO();

        List<String> questions3 = List.of("");
        List<List<String>> answers3 = List.of(new LinkedList<>());
        List<AnswerType> types3 = List.of(OPEN_ANSWER);;

        surveyDTO3.setId("");
        surveyDTO3.setTitle("title");
        surveyDTO3.setDescription("description");
        surveyDTO3.setQuestions(questions3);
        surveyDTO3.setTypes(types3);
        surveyDTO3.setAnswers(answers3);
    }

    @Test
    public void surveyCreationSuccess(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO1);
        survey = res.getResult();

        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void surveyCreationFail(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO2);
        survey = res.getResult();

        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void surveyCreationEmptyQueFail(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO3);
        survey = res.getResult();

        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void surveyAddAnswer(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO1);
        survey = res.getResult();

        survey.addAnswer(2, "3");

        Assert.assertTrue(!res.isFailure() && survey.getQuestion(2).getResult().getAnswers().getResult().size() == 3);
    }

    @Test
    public void surveyRemoveAnswer(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO1);
        survey = res.getResult();

        survey.removeAnswer(2, 0);

        Assert.assertTrue(!res.isFailure() && survey.getQuestion(2).getResult().getAnswers().getResult().size() == 1);
    }

    @Test
    public void surveyRemoveQuestion(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO1);
        survey = res.getResult();

        survey.removeQuestion(1);

        Assert.assertTrue(!res.isFailure() && survey.getQuestions().size() == 2);
    }

    @Test
    public void surveyAddQuestion(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO1);
        survey = res.getResult();

        survey.addQuestion("question", NUMERIC_ANSWER);

        Assert.assertTrue(!res.isFailure() && survey.getQuestions().size() == 4);
    }

    @Test
    public void surveyRemoveQuestionFailure(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO1);
        survey = res.getResult();

        survey.removeQuestion(5);

        Assert.assertTrue(!res.isFailure() && survey.getQuestions().size() == 3);
    }

    @Test
    public void surveyAddQuestionFailure(){
        Response<Survey> res = Survey.createSurvey("", surveyDTO1);
        survey = res.getResult();

        survey.addQuestion("", NUMERIC_ANSWER);

        Assert.assertTrue(!res.isFailure() && survey.getQuestions().size() == 3);
    }
}

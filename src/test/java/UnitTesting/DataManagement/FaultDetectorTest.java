package UnitTesting.DataManagement;

import Communication.DTOs.SurveyAnswersDTO;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.FaultDetector.FaultDetector;
import Domain.DataManagement.FaultDetector.Rules.*;
import Domain.DataManagement.SurveyAnswers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static Domain.DataManagement.AnswerState.AnswerType.*;
import static Domain.DataManagement.FaultDetector.Rules.Comparison.GREATER_THAN;
import static Domain.DataManagement.FaultDetector.Rules.Comparison.LESS_THAN;


public class FaultDetectorTest {

    private FaultDetector detector;
    private SurveyAnswers answers;
    private SurveyAnswersDTO answersDTO1, answersDTO2;


    @Before
    public void setUp(){
        detector = new FaultDetector();
        answers = new SurveyAnswers();

        // legal answers
        answersDTO1 = new SurveyAnswersDTO();

        List<String> answers1 = Arrays.asList("30", "1", "3");
        List<AnswerType> types1 = Arrays.asList(NUMERIC_ANSWER, MULTIPLE_CHOICE, MULTIPLE_CHOICE);

        answersDTO1.setAnswers(answers1);
        answersDTO1.setTypes(types1);

        // illegal answers
        answersDTO2 = new SurveyAnswersDTO();

        List<String> answers2 = Arrays.asList("a", "b", "c");
        List<AnswerType> types2 = Arrays.asList(OPEN_ANSWER, NUMERIC_ANSWER, MULTIPLE_CHOICE);

        answersDTO2.setAnswers(answers2);
        answersDTO2.setTypes(types2);
    }

    @Test
    public void createAnswersSuccess(){
        Response<Boolean> res = answers.addAnswers(answersDTO1);
        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void createAnswersFailure(){
        Response<Boolean> res = answers.addAnswers(answersDTO2);
        Assert.assertTrue(res.isFailure());
    }

    @Test
    public void baseNumericRule(){
        answers.addAnswers(answersDTO1);
        detector.addRule(new NumericBaseRule(0, GREATER_THAN, 28), 0);
        detector.addRule(new NumericBaseRule(0, GREATER_THAN, 32), 1);
        detector.addRule(new NumericBaseRule(0, LESS_THAN, 32), 2);

        Assert.assertEquals(2, detector.detectFault(answers).getResult().size());
    }

    @Test
    public void baseMultipleChoiceRule(){
        answers.addAnswers(answersDTO1);
        detector.addRule(new MultipleChoiceBaseRule(1, List.of(1)), 0);
        detector.addRule(new MultipleChoiceBaseRule(1, List.of(3)), 1);
        detector.addRule(new MultipleChoiceBaseRule(2, List.of(3)), 2);

        Assert.assertEquals(2, detector.detectFault(answers).getResult().size());
    }


    @Test
    public void illegalSurveyWithRulesAnd(){
        List<Rule> rules1 = new LinkedList<>();
        List<Rule> rules2 = new LinkedList<>();
        answers.addAnswers(answersDTO1);

        rules1.add(new MultipleChoiceBaseRule(1, List.of(1)));
        rules1.add(new NumericBaseRule(0, GREATER_THAN, 28));

        rules2.add(new MultipleChoiceBaseRule(1, List.of(1)));
        rules2.add(new NumericBaseRule(0, GREATER_THAN, 32));

        detector.addRule(new AndRule(rules1),  0);
        detector.addRule(new AndRule(rules2),  0);

        Assert.assertEquals(1, detector.detectFault(answers).getResult().size());
    }

    @Test
    public void illegalSurveyWithRulesOr(){
        List<Rule> rules1 = new LinkedList<>();
        List<Rule> rules2 = new LinkedList<>();
        answers.addAnswers(answersDTO1);

        rules1.add(new MultipleChoiceBaseRule(1, List.of(2)));
        rules1.add(new NumericBaseRule(0, LESS_THAN, 28));

        rules2.add(new MultipleChoiceBaseRule(1, List.of(1)));
        rules2.add(new NumericBaseRule(0, GREATER_THAN, 32));

        detector.addRule(new OrRule(rules1),  0);
        detector.addRule(new OrRule(rules2),  0);

        Assert.assertEquals(1, detector.detectFault(answers).getResult().size());
    }

    @Test
    public void illegalSurveyWithRulesImply(){
        answers.addAnswers(answersDTO1);
        detector.addRule(new ImplyRule(new NumericBaseRule(0, GREATER_THAN, 28), new MultipleChoiceBaseRule(1, List.of(1))), 0);
        detector.addRule(new ImplyRule(new NumericBaseRule(0, GREATER_THAN, 28), new MultipleChoiceBaseRule(1, List.of(2))), 1);

        Assert.assertEquals(1, detector.detectFault(answers).getResult().size());
    }

    @Test
    public void illegalSurveyWithRulesIff(){
        answers.addAnswers(answersDTO1);
        detector.addRule(new IffRule(new NumericBaseRule(0, GREATER_THAN, 28), new MultipleChoiceBaseRule(1, List.of(1))), 0);
        detector.addRule(new IffRule(new NumericBaseRule(0, GREATER_THAN, 28), new MultipleChoiceBaseRule(1, List.of(2))), 1);

        Assert.assertEquals(1, detector.detectFault(answers).getResult().size());
    }

    @Test
    public void illegalSurveyWithRulesComplex(){
        answers.addAnswers(answersDTO1);
        detector.addRule(new ImplyRule(new NumericBaseRule(0, GREATER_THAN, 28), new MultipleChoiceBaseRule(1, List.of(1))), 0);
        detector.addRule(new IffRule(new NumericBaseRule(0, GREATER_THAN, 28), new MultipleChoiceBaseRule(1, List.of(1))), 0);

        Assert.assertEquals(2, detector.detectFault(answers).getResult().size());

    }


}

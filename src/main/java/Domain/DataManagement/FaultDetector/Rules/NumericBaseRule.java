package Domain.DataManagement.FaultDetector.Rules;

import Communication.DTOs.RuleDTO;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.SurveyAnswers;

import java.util.Arrays;
import java.util.List;

import static Domain.DataManagement.AnswerState.AnswerType.NUMERIC_ANSWER;
import static Domain.DataManagement.FaultDetector.Rules.RuleType.NUMERIC;

public class NumericBaseRule implements Rule{

    private final int questionID;
    private final Comparison comparison;
    private final int num;

    public NumericBaseRule(int questionID, Comparison comparison, int num) {
        this.questionID = questionID;
        this.comparison = comparison;
        this.num = num;
    }

    @Override
    public boolean apply(SurveyAnswers answers) {
        Response<AnswerType> type = answers.getAnswerType(questionID);

        if(type.isFailure() || type.getResult() != NUMERIC_ANSWER)
            return false;

        int answer = Integer.parseInt(answers.getAnswer(questionID).getResult());

        switch (comparison){
            case GREATER_THAN:
                if(answer > num)
                    return true;
                break;
            case LESS_THAN:
                if(answer < num)
                    return true;
                break;
            case EQUAL:
                if(answer == num)
                    return true;
                break;
        }

        return false;
    }

    @Override
    public List<Integer> getQuestionIndex() {
        return Arrays.asList(questionID);
    }

    @Override
    public RuleDTO getDTO() {
        RuleDTO dto = new RuleDTO();
        dto.setQuestionID(questionID);
        dto.setComparison(comparison);
        dto.setAnswers(Arrays.asList(num));
        dto.setType(NUMERIC);
        dto.setSubRules(null);

        return dto;
    }
}

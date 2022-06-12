package Domain.DataManagement.FaultDetector.Rules;

import Communication.DTOs.RuleDTO;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.SurveyAnswers;

import java.util.Arrays;
import java.util.List;

import static Domain.DataManagement.AnswerState.AnswerType.MULTIPLE_CHOICE;

public class MultipleChoiceBaseRule implements Rule{

    private final int questionID;
    private final List<Integer> answersID;

    public MultipleChoiceBaseRule(int questionID, List<Integer> answersID) {
        this.questionID = questionID;
        this.answersID = answersID;
    }

    @Override
    public boolean apply(SurveyAnswers answers) {
        Response<AnswerType> type = answers.getAnswerType(questionID);

        if(type.isFailure() || type.getResult() != MULTIPLE_CHOICE)
            return false;

        for(Integer answerID: this.answersID){
            if(Integer.parseInt(answers.getAnswer(questionID).getResult()) == answerID){
                return true;
            }
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
        dto.setComparison(Comparison.NONE);
        dto.setAnswers(answersID);
        dto.setType(Domain.DataManagement.FaultDetector.Rules.RuleType.MULTIPLE_CHOICE);
        dto.setSubRules(null);

        return dto;
    }
}

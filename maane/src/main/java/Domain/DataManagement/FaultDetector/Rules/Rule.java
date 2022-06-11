package Domain.DataManagement.FaultDetector.Rules;

import Communication.DTOs.RuleDTO;
import Domain.DataManagement.SurveyAnswers;

import java.util.List;

public interface Rule {

    boolean apply(SurveyAnswers answers);

    List<Integer> getQuestionIndex();

    RuleDTO getDTO();
}

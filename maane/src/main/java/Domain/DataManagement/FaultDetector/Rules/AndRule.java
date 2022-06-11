package Domain.DataManagement.FaultDetector.Rules;

import Communication.DTOs.RuleDTO;
import Domain.DataManagement.SurveyAnswers;

import java.util.LinkedList;
import java.util.List;

import static Domain.DataManagement.FaultDetector.Rules.RuleType.AND;

public class AndRule implements Rule{

    private List<Rule> rules;

    public AndRule(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean apply(SurveyAnswers answers) {

        for(Rule rule: rules){
            if(!rule.apply(answers))
                return false;
        }

        return true;
    }

    @Override
    public List<Integer> getQuestionIndex() {
        List<Integer> res = new LinkedList<>();

        for(Rule rule: rules){
            res.addAll(rule.getQuestionIndex());
        }

        return res;
    }

    @Override
    public RuleDTO getDTO() {
        List<RuleDTO> ruleDTOs = new LinkedList<>();

        RuleDTO dto = new RuleDTO();
        dto.setQuestionID(-1);
        dto.setComparison(Comparison.NONE);
        dto.setAnswers(new LinkedList<>());
        dto.setType(AND);

        for(Rule rule: rules)
            ruleDTOs.add(rule.getDTO());

        dto.setSubRules(ruleDTOs);

        return dto;
    }
}

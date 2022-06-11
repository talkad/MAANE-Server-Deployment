package Domain.DataManagement.FaultDetector.Rules;


import Communication.DTOs.RuleDTO;
import Domain.DataManagement.SurveyAnswers;

import java.util.LinkedList;
import java.util.List;

import static Domain.DataManagement.FaultDetector.Rules.RuleType.IFF;

public class IffRule implements Rule{
    private Rule firstSide, secondSide;

    public IffRule(Rule firstSide, Rule secondSide) {
        this.firstSide = firstSide;
        this.secondSide = secondSide;
    }

    @Override
    public boolean apply(SurveyAnswers answers) {
        return firstSide.apply(answers) == secondSide.apply(answers);
    }

    @Override
    public List<Integer> getQuestionIndex() {
        List<Integer> res = new LinkedList<>();

        res.addAll(firstSide.getQuestionIndex());
        res.addAll(secondSide.getQuestionIndex());

        return res;
    }

    @Override
    public RuleDTO getDTO() {
        List<RuleDTO> ruleDTOs = new LinkedList<>();

        RuleDTO dto = new RuleDTO();
        dto.setQuestionID(-1);
        dto.setComparison(Comparison.NONE);
        dto.setAnswers(new LinkedList<>());
        dto.setType(IFF);

        ruleDTOs.add(firstSide.getDTO());
        ruleDTOs.add(secondSide.getDTO());

        dto.setSubRules(ruleDTOs);

        return dto;
    }
}

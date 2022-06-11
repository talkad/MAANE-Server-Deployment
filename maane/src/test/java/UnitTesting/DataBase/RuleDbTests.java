package UnitTesting.DataBase;

import Communication.DTOs.RuleDTO;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Pair;
import Domain.DataManagement.FaultDetector.Rules.Comparison;
import Domain.DataManagement.FaultDetector.Rules.RuleType;
import Persistence.SurveyDAO;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;


public class RuleDbTests {
    SurveyDAO surveyQueries;
    RuleDTO ruleDTO;
    RuleDTO subRuleDTO;

    @Before
    public void setUp(){
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();

        surveyQueries = SurveyDAO.getInstance();

        List<RuleDTO> subRuleSubRules= new LinkedList<>();
        subRuleDTO = new RuleDTO(subRuleSubRules, RuleType.IFF, Comparison.GREATER_THAN, 11, List.of(22));

        List<RuleDTO> subRules= new LinkedList<>();
        subRules.add(subRuleDTO);
        int question_id = 1;
        int answer = 2;
        ruleDTO = new RuleDTO(subRules, RuleType.AND, Comparison.EQUAL, question_id, List.of(answer));
    }

    @Test
    public void insertRule() throws SQLException {
        SurveyDAO surveyQueries = SurveyDAO.getInstance();
        surveyQueries.insertRule("1", 2 ,ruleDTO);
    }

    @Test
    public void deleteRule() throws SQLException {
        surveyQueries.removeRule(9);
    }

    @Test
    public void getRule() throws SQLException {
        List<Pair<RuleDTO, Integer>> dd = surveyQueries.getRules("1");
    }

}


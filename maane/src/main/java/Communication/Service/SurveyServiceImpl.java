package Communication.Service;

import Communication.DTOs.*;
import Communication.Service.Interfaces.SurveyService;
import Domain.CommonClasses.Response;
import Domain.DataManagement.FaultDetector.Rules.Rule;
import Domain.DataManagement.FaultDetector.Rules.RuleConverter;
import Domain.DataManagement.SurveyController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class SurveyServiceImpl implements SurveyService {


    private static class CreateSafeThreadSingleton {
        private static final SurveyServiceImpl INSTANCE = new SurveyServiceImpl();
    }

    public static SurveyServiceImpl getInstance() {
        return CreateSafeThreadSingleton.INSTANCE;
    }

    @Override
    public Response<String> createSurvey(String username, SurveyDTO surveyDTO) {
        Response<String> res = SurveyController.getInstance().createSurvey(username, surveyDTO);

        if(res.isFailure())
            log.error("{} failed to create new survey", username);
        else
            log.info("{} created new survey with title: {}", username, surveyDTO.getTitle());

        return res;
    }

    @Override
    public Response<Boolean> addAnswers(SurveyAnswersDTO answersDTO) {
        Response<Boolean> res = SurveyController.getInstance().addAnswers(answersDTO);

        if(res.isFailure())
            log.error("failed to add answers");
        else
            log.info("added answers successfully");

        return res;
    }

    @Override
    public Response<SurveyDTO> getSurvey(String surveyID) {
        Response<SurveyDTO> res = SurveyController.getInstance().getSurvey(surveyID);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info("survey {} found", surveyID);

        return res;
    }

    @Override
    public Response<Boolean> addRule(String username, String surveyID, List<RuleRequestDTO> rulesDTO) {
        Response<Boolean> res = SurveyController.getInstance().removeRules(username, surveyID);

        if(!res.isFailure()) {

            for (RuleRequestDTO ruleRequestDTO : rulesDTO) {

                //check validity of rule
                if(ruleRequestDTO.getGoalID() == null){
                    res = new Response<>(false, true, "failed to parse goal id");
                    break;
                }

                Rule rule = RuleConverter.getInstance().convertRule(ruleRequestDTO.getRuleDTO());

                if (rule == null) {
                    res = new Response<>(false, true, "failed to parse rule");
                    break;
                }

                res = SurveyController.getInstance().addRule(username, surveyID, rule, ruleRequestDTO.getGoalID());

                if (res.isFailure()) {
                    res = new Response<>(false, true, "failed to add rule associated with goal " + ruleRequestDTO.getGoalID());
                    break;
                }

            }
        }

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info("new rule added successfully");

        return res;
    }

    @Override
    public Response<Boolean> removeRule(String username, String surveyID, int ruleID) {
        Response<Boolean> res = SurveyController.getInstance().removeRule(username, surveyID, ruleID);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info("new rule added successfully");

        return res;
    }

    @Override
    public Response<List<List<String>>> detectFault(String username, String surveyID, Integer year) {
        Response<List<List<String>>> res = SurveyController.getInstance().detectFault(username, surveyID, year);

        if(res.isFailure())
            log.error("failed to detect faults in survey {}", surveyID);
        else
            log.info("detected faults in survey {}", surveyID);

        return res;
    }

    @Override
    public Response<Boolean> removeRules(String username, String surveyID) {
        Response<Boolean> res = SurveyController.getInstance().removeRules(username, surveyID);

        if(res.isFailure())
            log.error("failed to remove rules in survey {}", surveyID);
        else
            log.info("removed all rules in survey {}", surveyID);

        return res;
    }

    @Override
    public Response<Boolean> addQuestion(String username, QuestionDTO questionDTO) {
        Response<Boolean> res = SurveyController.getInstance().addQuestion(username, questionDTO);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<Boolean> removeQuestion(String username, String surveyID, Integer questionID) {
        Response<Boolean> res = SurveyController.getInstance().removeQuestion(username, surveyID, questionID);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<Boolean> submitSurvey(String username, String surveyID) {
        Response<Boolean> res = SurveyController.getInstance().submitSurvey(username, surveyID);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<SurveyStatsDTO> getSurveyStats(String username, String surveyID) {
        Response<SurveyStatsDTO> res = SurveyController.getInstance().getSurveyStats(username, surveyID);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<AnswersDTO> getAnswers(String username, String surveyID, String symbol) {
        Response<AnswersDTO> res = SurveyController.getInstance().getAnswers(username, surveyID, symbol);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    public Response<List<SurveyDetailsDTO>> getSurveys(String username) {
        Response<List<SurveyDetailsDTO>> res = SurveyController.getInstance().getSurveys(username);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info("get surveys");

        return res;
    }

    @Override
    public Response<RulesDTO> getRules(String surveyID) {
        Response<RulesDTO> res = SurveyController.getInstance().getRules(surveyID);

        if(res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info("get rules");

        return res;
    }


}

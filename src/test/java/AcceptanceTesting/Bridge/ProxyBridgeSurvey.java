package AcceptanceTesting.Bridge;

import Communication.DTOs.*;
import Communication.Service.Interfaces.SurveyService;
import Domain.CommonClasses.Response;

import java.util.List;

public class ProxyBridgeSurvey implements SurveyService {

    private SurveyService real;

    public ProxyBridgeSurvey(){
        real = null;
    }

    public void setRealBridge(SurveyService implementation) {
        if(real == null){
            real = implementation;
        }
    }

    @Override
    public Response<String> createSurvey(String username, SurveyDTO surveyDTO) {
        if (real != null){
            return real.createSurvey(username, surveyDTO);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<Boolean> addAnswers(SurveyAnswersDTO answersDTO) {
        if (real != null){
            return real.addAnswers(answersDTO);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<SurveyDTO> getSurvey(String surveyID) {
        if (real != null){
            return real.getSurvey(surveyID);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<Boolean> addRule(String username, String surveyID, List<RuleRequestDTO> rulesDTO) {
        if (real != null){
            return real.addRule(username, surveyID, rulesDTO);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<Boolean> removeRule(String username, String surveyID, int ruleID) {
        if (real != null){
            return real.removeRule(username, surveyID, ruleID);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<Boolean> removeRules(String username, String surveyID) {
        if (real != null){
            return real.removeRules(username, surveyID);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<Boolean> addQuestion(String result, QuestionDTO questionDTO) {
        if (real != null){
            return real.addQuestion(result, questionDTO);
        }

        return new Response<>(null, true, "not implemented");    }

    @Override
    public Response<Boolean> removeQuestion(String result, String surveyID, Integer questionID) {
        if (real != null){
            return real.removeQuestion(result, surveyID, questionID);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<Boolean> submitSurvey(String username, String surveyID) {
        if (real != null){
            return real.submitSurvey(username, surveyID);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<SurveyStatsDTO> getSurveyStats(String username, String surveyID) {
        if (real != null){
            return real.getSurveyStats(username, surveyID);
        }

        return new Response<>(null, true, "not implemented");
    }


    @Override
    public Response<AnswersDTO> getAnswers(String username, String surveyID, String symbol) {
        if (real != null){
            return real.getAnswers(username, surveyID, symbol);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<List<SurveyDetailsDTO>> getSurveys(String username) {
        if (real != null){
            return real.getSurveys(username);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<RulesDTO> getRules(String surveyID) {
        if (real != null){
            return real.getRules(surveyID);
        }

        return new Response<>(null, true, "not implemented");
    }

    @Override
    public Response<List<List<String>>> detectFault(String username, String surveyID, Integer year) {
        if (real != null){
            return real.detectFault(username, surveyID, year);
        }

        return new Response<>(null, true, "not implemented");
    }


}

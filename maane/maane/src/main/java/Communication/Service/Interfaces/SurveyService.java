package Communication.Service.Interfaces;

import Communication.DTOs.*;
import Domain.CommonClasses.Response;

import java.util.List;

public interface SurveyService {

    // Data Management Module
    Response<String> createSurvey(String username, SurveyDTO surveyDTO);

    Response<Boolean> addAnswers(SurveyAnswersDTO answersDTO);

    Response<SurveyDTO> getSurvey(String surveyID);

    Response<Boolean> addRule(String username, String surveyID, List<RuleRequestDTO> rulesDTO);

    Response<Boolean> removeRule(String username, String surveyID, int ruleID);

    Response<List<SurveyDetailsDTO>> getSurveys(String username);

    Response<RulesDTO> getRules(String surveyID);

//    Response<Boolean> removeSurvey(String username, int id);
//
//    Response<Survey> getSurvey(int id);
//
//    Response<Survey> publishSurvey(String username);
//
//    Response<Integer> addQuestion(int id, String questionText);
//
//    Response<Boolean> removeQuestion(int id, int questionID);
//
//    Response<Integer> addAnswer(int id, int questionID, String answer);
//
//    Response<Boolean> removeAnswer(int id, int questionID, int answerID);
//
//    Response<Boolean> addRule(String username, int id, Rule rule, String description);
//
//    Response<Boolean> removeRule(String username, int id, int index);

    Response<List<List<String>>> detectFault(String username, String surveyID, Integer year);

    Response<Boolean> removeRules(String username, String surveyID);

    Response<Boolean> addQuestion(String username, QuestionDTO questionDTO);

    Response<Boolean> removeQuestion(String username, String surveyID, Integer questionID);

    Response<Boolean> submitSurvey(String username, String surveyID);

    Response<SurveyStatsDTO> getSurveyStats(String username, String surveyID);

    Response<AnswersDTO> getAnswers(String username, String surveyID, String symbol);
}

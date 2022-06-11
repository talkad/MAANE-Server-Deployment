package Domain.DataManagement;

import Communication.DTOs.*;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.FaultDetector.FaultDetector;
import Domain.DataManagement.FaultDetector.Rules.Rule;
import Domain.DataManagement.FaultDetector.Rules.RuleConverter;
import Domain.UsersManagment.UserController;
import Persistence.DbDtos.SchoolDBDTO;
import Persistence.SurveyDAO;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SurveyController {

    private final SecureRandom secureRandom;
    private final Base64.Encoder base64Encoder;

    private final SurveyDAO surveyDAO;


    private static class CreateSafeThreadSingleton {
        private static final SurveyController INSTANCE = new SurveyController();
    }

    public static SurveyController getInstance() {
        return CreateSafeThreadSingleton.INSTANCE;
    }

    public SurveyController(){
        secureRandom = new SecureRandom();
        base64Encoder = Base64.getUrlEncoder();
        surveyDAO = SurveyDAO.getInstance();
    }

    /**
     * creates new survey
     * @param username the name of the user that wish to create a survey
     * @param surveyDTO is the questions and their types that the generated survey will contain
     * @return response with result of the new surveyID on success, -1 on failure
     */
    public Response<String> createSurvey(String username, SurveyDTO surveyDTO){
        Response<String> permissionRes;
        Response<Survey> surveyRes;

        if(surveyDTO.getId() != null && surveyDTO.getId().length() > 0)
            return updateSurvey(username, surveyDTO);

        String indexer = createToken();

        surveyDTO.setId(indexer);
        surveyDTO.setYear(LocalDateTime.now().getYear());

        permissionRes = UserController.getInstance().createSurvey(username, indexer);

        if(permissionRes.isFailure() || permissionRes.getResult().length() == 0)
            return new Response<>("", true, permissionRes.getErrMsg());
        surveyRes = Survey.createSurvey(indexer, surveyDTO);

        if(surveyRes.isFailure()) {
            Response<String> res = UserController.getInstance().removeSurvey(username, indexer);

            if(res.isFailure())
                return res;

            return new Response<>("", true, surveyRes.getErrMsg());
        }

        surveyDAO.insertSurvey(surveyDTO);

        return new Response<>(indexer, false, "new survey created successfully");
    }

    private Response<String> updateSurvey(String username, SurveyDTO surveyDTO) {
        Response<Boolean> removalRes;
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, surveyDTO.getId());

        if(!legalAdd.getResult() && !ServerContextInitializer.getInstance().isMockMode())
            return new Response<>("", true, username + " does not create survey " + surveyDTO.getId());

        if(surveyDAO.getSurveySubmission(surveyDTO.getId()).getResult())
            return new Response<>("", true, "cannot add question to an already submitted survey");

        removalRes = surveyDAO.removeSurvey(surveyDTO.getId());

        if(removalRes.isFailure())
            return new Response<>("", true, "survey removal failed");

        surveyDAO.insertSurvey(surveyDTO);

        return new Response<>(surveyDTO.getId(), false, "updated survey successfully");
    }

    public Response<Boolean> submitSurvey(String username, String surveyID){
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, surveyID);

        if(!legalAdd.getResult())
            return new Response<>(false, true, username + " does not create survey " + surveyID);

        if(!ServerContextInitializer.getInstance().isTestMode())
            UserController.getInstance().notifySurveyCreation(username, surveyID);

        return surveyDAO.surveySubmission(surveyID);
    }

    public Response<Boolean> addQuestion(String username, QuestionDTO questionDTO) {
        Response<Boolean> resDB;
        Response<SurveyDTO> resSurvey;
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, questionDTO.getSurveyID());

        if(surveyDAO.getSurveySubmission(questionDTO.getSurveyID()).getResult())
            return new Response<>(false, true, "cannot add question to an already submitted survey");

        if(!legalAdd.getResult())
            return new Response<>(false, true, username + " does not create survey " + questionDTO.getSurveyID());

        resSurvey = surveyDAO.getSurvey(questionDTO.getSurveyID());

        if(resSurvey.isFailure())
            return new Response<>(false, true, resSurvey.getErrMsg());

        resDB = surveyDAO.addQuestion(questionDTO, resSurvey.getResult().getQuestions().size());

        if(resDB.isFailure())
            return new Response<>(false, true, resDB.getErrMsg());

        return new Response<>(true, false, "question added successfully");
    }

    public Response<Boolean> removeQuestion(String username, String surveyID, Integer questionID) {
        Response<Boolean> resDB;
        Response<SurveyDTO> resSurvey;
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, surveyID);

        if(surveyDAO.getSurveySubmission(surveyID).getResult())
            return new Response<>(false, true, "cannot remove question of an already submitted survey");

        if(!legalAdd.getResult())
            return new Response<>(false, true, username + " does not created survey " + surveyID);

        resSurvey = surveyDAO.getSurvey(surveyID);
        resDB = surveyDAO.removeQuestions(surveyID, questionID, resSurvey.getResult().getQuestions().size());

        if(resDB.isFailure())
            return new Response<>(false, true, resDB.getErrMsg());

        return new Response<>(true, false, "question removed successfully");

    }

    /**
     * new answers for certain survey
     * @param answersDTO is the answers for a given survey
     * @return response with boolean that represents if the answers was
     */
    public Response<Boolean> addAnswers(SurveyAnswersDTO answersDTO){

        SurveyAnswers answer = new SurveyAnswers();
        Response<Boolean> answerRes = answer.addAnswers(answersDTO);
        Response<Boolean> validAnswer;
        String symbol;

        if(!surveyDAO.getSurveySubmission(answersDTO.getId()).getResult())
            return new Response<>(false, true, "The survey wasn't published yet");

        if(answerRes.isFailure())
            return new Response<>(false, true, answerRes.getErrMsg());

        Response<Survey> surveyResponse = loadSurvey(answersDTO.getId());
        if(surveyResponse.isFailure())
            return new Response<>(false, true, surveyResponse.getErrMsg());

        if(surveyResponse.getResult().getQuestions().size() != answersDTO.getTypes().size())
            return new Response<>(false, true, "number of answers cannot be different from number of questions");

        if(answersDTO.getAnswers().size() == 0)
            return new Response<>(true, false, "empty survey");

        symbol = answersDTO.getAnswers().get(0);

        if(symbol.length() == 0)
            return new Response<>(false, true, "School symbol cannot be empty string");

        if(parseInteger(symbol) == -1)
            return new Response<>(false, true, "School symbol must be a number");

        // check answer validity
        validAnswer = UserController.getInstance().isValidAnswer(symbol, answersDTO.getId());
        if(!validAnswer.getResult())
            return new Response<>(false, true, validAnswer.getErrMsg());

        // remove from db if exists
        surveyDAO.removeCoordinatorAnswers(answersDTO.getId(), symbol);

        // add to DB
        answer.setSymbol(symbol);
        List<String> answers = new LinkedList<>(answersDTO.getAnswers());
        answers.remove(0);

        List<AnswerType> types = new LinkedList<>(answersDTO.getTypes());
        types.remove(0);

        surveyDAO.insertCoordinatorAnswers(answersDTO.getId(), symbol, answers, types);

        return new Response<>(true, false, "the answer added successfully");
    }

    /**
     * get the survey by its ID
     * @param id of the desired survey
     * @return the survey with the given ID if exists, failure response otherwise
     */
    public Response<SurveyDTO> getSurvey(String id){
            return surveyDAO.getSurvey(id);
    }

    /**
     * add a new rule to a survey
     * @param username the name of the user that desired to add a rule
     * @param id of the survey
     * @param rule the new rule to be added
     * @param goalID the goal the rule represents
     * @return success response if the arguments are legal. failure otherwise
     */
    public Response<Boolean> addRule(String username, String id, Rule rule, int goalID){
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, id);

        if(!legalAdd.getResult())
            return new Response<>(false, true, username + " does not created survey " + id);

        surveyDAO.insertRule(id, goalID, rule.getDTO());

        return new Response<>(true, false, "OK");
    }

    /**
     * remove a rule from a survey
     * @param username the name of the user desired to remove a rule
     * @param id of the survey
     * @param ruleID id of the rule to be removed
     * @return successful response if the {@username} created the survey in first place
     */
    public Response<Boolean> removeRule(String username, String id, int ruleID){
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, id);

        if(!legalAdd.getResult())
            return new Response<>(false, true, username + " does not created survey " + id);

        return surveyDAO.removeRule(ruleID);
    }

    /**
     * remove all rules related to given surveyID
     * @param username the user trying to remove the rules
     * @param surveyID the survey all the rules will be deleted
     * @return if the function call succeeded
     */
    public Response<Boolean> removeRules(String username, String surveyID) {
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, surveyID);

        if(!legalAdd.getResult())
            return new Response<>(false, true, username + " removed all rules from survey " + surveyID);

        return surveyDAO.removeRules(surveyID);
    }

    /**
     * detects irregularities in survey answers
     * @param username the name of the user want to detect fault
     * @param id of the survey
     * @return response contains list of all goals that not consistent with the rules, for each answer
     */
    public Response<List<List<String>>> detectFault(String username, String id, Integer year){

        List<List<String>> faults = new LinkedList<>();
        FaultDetector faultDetector;
        List<GoalDTO> goals = UserController.getInstance().getGoals(username, year).getResult();
        List<String> currentFaults;
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, id);

        if(!legalAdd.getResult())
            return new Response<>(null, true, username + " does not created survey " + id);

        Response<Survey> surveyResponse = loadSurvey(id);
        if(surveyResponse.isFailure())
            return new Response<>(null, true, surveyResponse.getErrMsg());

        faultDetector = new FaultDetector(rulesConverter(surveyDAO.getRules(id)));
        List<SurveyAnswers> answers = answerConverter(surveyDAO.getAnswers(id));

        for(SurveyAnswers ans: answers){
            currentFaults = new LinkedList<>();

            for(Integer fault: faultDetector.detectFault(ans).getResult())
                if(goals.size() > fault)
                    currentFaults.add(goals.get(fault).getTitle());

            faults.add(currentFaults);
        }

        return new Response<>(faults, false, "faults detected");
    }

//    public Map<String, List<SurveyAnswers>> getAnswers() {
//        Map<String, List<SurveyAnswers>> answers = new ConcurrentHashMap<>();
//        Map<String, List<SurveyAnswersDTO>> answersDB = surveyDAO.getAllAnswers();
//
//        for(String key: answersDB.keySet())
//            answers.put(key, answerConverter(answersDB.get(key)));
//
//        return answers;
//    }

    public Response<List<SurveyAnswers>> getAnswersForSurvey(String surveyId) {
        return new Response<>(answerConverter(surveyDAO.getAnswers(surveyId)), false, "OK");
    }

    public Response<RulesDTO> getRules(String surveyID){
        FaultDetector fd;
        RulesDTO rulesDTO = new RulesDTO();
        List<RuleRequestDTO> ruleRequests = new LinkedList<>();
        rulesDTO.setSurveyID(surveyID);

        fd = new FaultDetector(rulesConverter(surveyDAO.getRules(surveyID)));
        for(Pair<Rule, Integer> p: fd.getRules()){
            ruleRequests.add(rulesDTOConverter(p));
        }

        rulesDTO.setRules(ruleRequests);

        return new Response<>(rulesDTO, false, "success");
    }

    /**
     * convert rule to RuleRequest form
     * @param rulePair pair of rule and the goal id it's associated with
     * @return the converted new form of the rule
     */
    private RuleRequestDTO rulesDTOConverter(Pair<Rule, Integer> rulePair) {
        RuleRequestDTO ruleRequest = new RuleRequestDTO();
        RuleDTO ruleDTO = rulePair.getFirst().getDTO();

        ruleRequest.setGoalID(rulePair.getSecond());
        ruleRequest.setRuleDTO(ruleDTO);

        return ruleRequest;
    }


    public Response<List<SurveyDetailsDTO>> getSurveys(String username){
        Response<List<String>> res = UserController.getInstance().getSurveys(username);
        List<SurveyDetailsDTO> surveyInfo = new LinkedList<>();
        Response<SurveyDTO> survey;
        StringBuilder errMsg = new StringBuilder("couldn't load surveys: \n");

        if(res.isFailure())
            return new Response<>(new LinkedList<>(), true, res.getErrMsg());

        if(res.getResult() == null)
            return new Response<>(new LinkedList<>(), false, "There are no surveys");

        for(String surveyID: res.getResult()) {
            survey = surveyDAO.getSurvey(surveyID);

            if(!survey.isFailure())
                surveyInfo.add(new SurveyDetailsDTO(survey.getResult().isPublished(), survey.getResult().getTitle(), survey.getResult().getDescription(), surveyID, survey.getResult().getYear()));
            else
                errMsg.append(surveyID).append("\n");
        }

        return new Response<>(surveyInfo, false, errMsg.toString());
    }


    /**
     * detect all irregularities in certain school
     * @param username the name of the user that want to detect the faults
     * @param id of the survey
     * @param symbol of the school
     * @return list of all goals that are not consistent with the rules
     */
    public Response<List<Integer>> detectSchoolFault(String username, String id, String symbol, Integer year){
        FaultDetector faultDetector;
        List<Integer> currentFaults = new LinkedList<>();

        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, id);

        if(!legalAdd.getResult())
            return new Response<>(null, true, username + " did not create survey " + id);

        faultDetector = new FaultDetector(rulesConverter(surveyDAO.getRules(id)));
        List<SurveyAnswers> answers = answerConverter(surveyDAO.getAnswers(id));

        // remove redundant answer
        for(SurveyAnswers ans: answers){
            ans.removeSymbolAnswer();
        }

        for(SurveyAnswers ans: answers){

            if(ans.getSymbol().equals(symbol)){
                currentFaults.addAll(faultDetector.detectFault(ans).getResult());
            }
        }

        return new Response<>(currentFaults, false, "faults detected");
    }


    public Response<List<Integer>> detectSchoolFaultsMock(List<Pair<String, List<Integer>>> schoolsAndFaults, String schoolId){
        for (Pair<String, List<Integer>> schoolAndFaults: schoolsAndFaults)
        {
            if(schoolId.equals(schoolAndFaults.getFirst())) {
                //System.out.println("schoodId: " + schoolId + " schoolidFromList: " + schoolAndFaults.getFirst() + " faults: " + schoolAndFaults.getSecond().toString());
                return new Response<>(schoolAndFaults.getSecond(), false, "faults detected");
            }
        }
        return new Response<>(null, false, "faults detected");
    }

    /**
     * Get the survey stats (according to answers) for a given survey
     * @param username belong to the request
     * @param surveyID the id of the survey
     * @return Stats of a survey
     */
    public Response<SurveyStatsDTO> getSurveyStats(String username, String surveyID) {
        Response<SurveyDTO> surveyRes;
        Response<Boolean> legalAdd = UserController.getInstance().hasCreatedSurvey(username, surveyID);

        if(!legalAdd.getResult())
            return new Response<>(null, true, username + " did not create survey " + surveyID);

        surveyRes = surveyDAO.getSurvey(surveyID);

        if(surveyRes.isFailure())
            return new Response<>(null, true, surveyRes.getErrMsg());

        if(!surveyRes.getResult().isPublished())
            return new Response<>(null, true, "The survey is not published yet");

        List<SurveyAnswers> answers = answerConverter(surveyDAO.getAnswers(surveyID));

        return buildSurveyStats(answers, surveyRes.getResult());
    }

    /**
     * get answers of given school
     * @param username the username of the request
     * @param surveyID survey identifier
     * @param symbol identifier of school
     * @return answers
     */
    public Response<AnswersDTO> getAnswers(String username, String surveyID, String symbol) {
        FaultDetector faultDetector;
        List<String> actualAnswers;
        Response<Boolean> legalGet = UserController.getInstance().hasCreatedSurvey(username, surveyID);

        if(!legalGet.getResult())
            return new Response<>(null, true, username + " did not create survey " + surveyID);

        faultDetector = new FaultDetector(rulesConverter(surveyDAO.getRules(surveyID)));

        SurveyAnswersDTO surveyAnswersDTO = surveyDAO.getAnswersPerSchool(surveyID, symbol);
        if(surveyAnswersDTO == null)
            return new Response<>(null, true, "answers was not found with symbol: " + symbol);

        SurveyAnswers answers = new SurveyAnswers(surveyAnswersDTO);

        actualAnswers = new LinkedList<>();

        for(Integer questionIndex: answers.getAnswers().keySet())
            actualAnswers.add(answers.getAnswers().get(questionIndex).getSecond());

        return new Response<>(new AnswersDTO(actualAnswers, faultDetector.getIllegalQuestionID(answers).getResult(), faultDetector.detectFaultTitles(answers).getResult()), false, "Got answers successfully");
    }

    private Response<SurveyStatsDTO> buildSurveyStats(List<SurveyAnswers> answers, SurveyDTO survey) {
        int numOfAnswers = answers.size();
        SchoolDBDTO school;
        Map<Integer, Pair<AnswerType, String>> answerMap;

        List<String> symbols = new LinkedList<>();
        List<String> schoolNames = new LinkedList<>();
        Map<Integer, Integer> numericAverage = new ConcurrentHashMap<>();
        Map<Integer, List<Integer>> multipleHistogram = new ConcurrentHashMap<>();

        // traverse over answers
        for(SurveyAnswers answer: answers){

            symbols.add(answer.getSymbol());
            school = DataController.getInstance().getSchool(answer.getSymbol());

            schoolNames.add(school==null? "Do Not Exists": school.getName());

            answerMap = answer.getAnswers();

            for(Integer questionIndex: answerMap.keySet()){
                switch (answerMap.get(questionIndex).getFirst()){
                    case MULTIPLE_CHOICE:
                        if(!multipleHistogram.containsKey(questionIndex))
                            multipleHistogram.put(questionIndex, new LinkedList<>((Collections.nCopies(survey.getAnswers().get(questionIndex - 1).size(), 0))));

                        List<Integer> currentHist = multipleHistogram.get(questionIndex);
                        int answerIndex = parseInteger(answerMap.get(questionIndex).getSecond());

                        currentHist.set(answerIndex, currentHist.get(answerIndex) + 1);
                        multipleHistogram.put(questionIndex, currentHist);

                        break;
                    case NUMERIC_ANSWER:
                        if(!numericAverage.containsKey(questionIndex))
                            numericAverage.put(questionIndex, 0);

                        numericAverage.put(questionIndex, numericAverage.get(questionIndex) + parseInteger(answerMap.get(questionIndex).getSecond()) / numOfAnswers);

                        break;
                }
            }

        }

        return new Response<>(new SurveyStatsDTO(symbols, schoolNames, numericAverage, multipleHistogram), false, "stats for survey: " + survey.getId());
    }

    private int parseInteger(String numStr){
        int num = -1;
        try {
            num = Integer.parseInt(numStr);
        }
        catch(NumberFormatException e){
            System.out.println(numStr + " is not a number");
        }

        return num;
    }

    private String createToken(){
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }



    private List<Pair<Rule, Integer>> rulesConverter(List<Pair<RuleDTO, Integer>> ruleDTOs){
        List<Pair<Rule, Integer>> rules = new LinkedList<>();

        for(Pair<RuleDTO, Integer> rule: ruleDTOs){
            rules.add(new Pair<>(RuleConverter.getInstance().convertRule(rule.getFirst()), rule.getSecond()));
        }

        return rules;
    }

    private List<SurveyAnswers> answerConverter(List<SurveyAnswersDTO> answerDTOs){
        List<SurveyAnswers> answers = new LinkedList<>();

        for(SurveyAnswersDTO ans: answerDTOs){
            answers.add(new SurveyAnswers(ans));
        }

        return answers;
    }

    private Response<Survey> loadSurvey(String id){

        Survey survey;
        Response<SurveyDTO> surveyRes;

        surveyRes = surveyDAO.getSurvey(id);

        if(surveyRes.isFailure())
            return new Response<>(null, true, surveyRes.getErrMsg());

        Response<Survey> surveyCreation = Survey.createSurvey(surveyRes.getResult().getId(), surveyRes.getResult());

        if(surveyCreation.isFailure())
            return new Response<>(null, true, surveyCreation.getErrMsg());

        survey = surveyCreation.getResult();


        return new Response<>(survey, false, "OK");
    }

    public Response<Integer> getSurveyYear(String surveyId) {
        return surveyDAO.getSurveyYear(surveyId);
    }

}

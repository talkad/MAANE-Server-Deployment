package Persistence;

import Communication.DTOs.QuestionDTO;
import Communication.DTOs.RuleDTO;
import Communication.DTOs.SurveyAnswersDTO;
import Communication.DTOs.SurveyDTO;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Persistence.DbAdapter.SurveyDAOMockAdapter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * cache optimization for DataManagement Module
 * The main DTOs of this module, SurveyDTO, SurveyAnswersDTO, will  be cached
 *
 * Parallelism optimization:
 * for each function related to cache, the update of DB will occur concurrently.
 */
public class SurveyDAO {

    /**
     * The cache will be implemented as LRU
     */
    private final Map<String, Pair<LocalDateTime, SurveyDTO>> surveys;
    private final Map<String, Pair<LocalDateTime, List<SurveyAnswersDTO>>> answers;

    /**
     * connector to DB
     */
    private final SurveyPersistence persistence;

    /**
     * Threads for executing DB functions
     */
    //private final ThreadPoolExecutor executor;


    /**
     * maximal size of cache
     */
    private final int cacheSize;

    private static class CreateSafeThreadSingleton {
        private static final SurveyDAO INSTANCE = new SurveyDAO();
    }

    public static SurveyDAO getInstance() {
        return SurveyDAO.CreateSafeThreadSingleton.INSTANCE;
    }

    public SurveyDAO() {
        this.surveys = new ConcurrentHashMap<>();
        this.answers = new ConcurrentHashMap<>();
        this.persistence = SurveyPersistence.getInstance();
        //this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        this.cacheSize = 50;
    }

    //========================== Survey ==========================

    public Response<Boolean> insertSurvey(SurveyDTO surveyDTO) {
        addSurveyToCache(surveyDTO.getId(), surveyDTO);

//        executor.execute(() -> persistence.insertSurvey(surveyDTO));
//
//        return new Response<>(true, false, "survey inserted to cache");

        return persistence.insertSurvey(surveyDTO);
    }

    public Response<Boolean> removeSurvey(String surveyID) {

        removeSurveyFromCache(surveyID);
        return persistence.removeSurvey(surveyID);
    }

    public Response<Boolean> addQuestion(QuestionDTO questionDTO, int question_index) {
        Response<Boolean> res;

        removeSurveyFromCache(questionDTO.getSurveyID());
        res = persistence.addQuestion(questionDTO, question_index);
        loadSurveyToCache(questionDTO.getSurveyID());

//        executor.execute(() -> loadSurveyToCache(questionDTO.getSurveyID()));

        return res;
    }

    public Response<Boolean> surveySubmission(String surveyID) {
        Response<Boolean> res;

        removeSurveyFromCache(surveyID);
        res = persistence.surveySubmission(surveyID);
        loadSurveyToCache(surveyID);

//        executor.execute(() -> loadSurveyToCache(questionDTO.getSurveyID()));

        return res;
    }

    public Response<Boolean> removeQuestions(String surveyID, int questionID, int questionsNum) {
        Response<Boolean> res;

        removeSurveyFromCache(surveyID);
        res = persistence.removeQuestions(surveyID, questionID, questionsNum);
        loadSurveyToCache(surveyID);

//        executor.execute(() -> loadSurveyToCache(surveyID));

        return res;
    }

    public Response<SurveyDTO> getSurvey(String id)  {
        Response<SurveyDTO> res;

        if(surveys.containsKey(id))
            return new Response<>(surveys.get(id).getSecond(), false, "survey found in cache");

        res = persistence.getSurvey(id);

        if(!res.isFailure())
            addSurveyToCache(id, res.getResult());
//            executor.execute(() -> addSurveyToCache(id, res.getResult()));

        return res;
    }

    public Response<Integer> getSurveyYear(String surveyID)  {
        Response<Integer> res;

        if(surveys.containsKey(surveyID))
            return new Response<>(surveys.get(surveyID).getSecond().getYear(), false, "survey found in cache");

        res = persistence.getSurveyYear(surveyID);

        return res;
    }

    public Response<Boolean> getSurveySubmission(String id)  {
        return persistence.getSurveySubmission(id);
    }

    //===========================================================

    //========================== Rules ==========================

    public void insertRule(String survey_id, int goalID, RuleDTO dto) {
        persistence.insertRule(survey_id, goalID, dto);
    }

    public Response<Boolean> removeRule (int ruleID) {
        return persistence.removeRule(ruleID);
    }

    public Response<Boolean> removeRules(String surveyID) {
        return persistence.removeRules(surveyID);
    }

    public List<Pair<RuleDTO, Integer>> getRules(String surveyID) {
        if(ServerContextInitializer.getInstance().isTestMode())
            return SurveyDAOMockAdapter.getInstance().getRules(surveyID);

        return persistence.getRules(surveyID);
    }

    //===========================================================

    //========================== Answers ==========================

    public List<SurveyAnswersDTO> getAnswers(String surveyId) {
        List<SurveyAnswersDTO> surveyAnswers;

        if(ServerContextInitializer.getInstance().isTestMode())
            return SurveyDAOMockAdapter.getInstance().getAnswers(surveyId);

//        if(answers.containsKey(surveyId))
//            return answers.get(surveyId).getSecond();

        surveyAnswers = persistence.getAnswers(surveyId);
        addAnswersToCache(surveyId, surveyAnswers);

//        executor.execute(() -> addAnswersToCache(surveyId, surveyAnswers));

        return surveyAnswers;
    }

    public SurveyAnswersDTO getAnswersPerSchool(String surveyID, String symbol) {
        SurveyAnswersDTO surveyAnswers;

        surveyAnswers = persistence.getAnswersPerSchool(surveyID, symbol);

//        executor.execute(() -> addAnswersToCache(surveyId, surveyAnswers));

        return surveyAnswers;
    }

//    private SurveyAnswersDTO findSchoolAnswers(List<SurveyAnswersDTO> answers, int symbol) {
//
//        for(SurveyAnswersDTO surveyAnswersDTO: answers){
//            if(surveyAnswersDTO.getId().equals(symbol+""))
//                return surveyAnswersDTO;
//        }
//
//        return null;
//    }


    public void insertCoordinatorAnswers(String id, String symbol, List<String> answers, List<AnswerType> types) {
        SurveyAnswersDTO answersDTO;

        types.add(0, AnswerType.OPEN_ANSWER);
        answers.add(0, symbol);

        answersDTO = new SurveyAnswersDTO(id, answers, types);

        addAnswerToCache(id, answersDTO);

//        executor.execute(() -> persistence.insertCoordinatorAnswers(id, symbol, answers, types));
        persistence.insertCoordinatorAnswers(id, symbol, answers, types);
    }

    public void removeCoordinatorAnswers(String surveyID, String symbol) {

        removeAnswersFromCache(surveyID);
        persistence.removeCoordinatorAnswers(surveyID, symbol);

    }

    // ==================== Cache Management =======================
    private void addSurveyToCache(String surveyID, SurveyDTO survey){
        if(surveys.size() > cacheSize)
            removeSurveyLRU();

        surveys.put(surveyID, new Pair<>(LocalDateTime.now(), survey));
    }

    private void addAnswersToCache(String surveyID, List<SurveyAnswersDTO> surveyAnswers) {
        if(answers.size() > cacheSize)
            removeAnswersLRU();

        answers.put(surveyID, new Pair<>(LocalDateTime.now(), surveyAnswers));
    }

    private void addAnswerToCache(String surveyID, SurveyAnswersDTO answersDTO) {
        List<SurveyAnswersDTO> answersList;

        if(answers.size() > cacheSize)
            removeAnswersLRU();

        if(!answers.containsKey(surveyID)){
            answers.put(surveyID, new Pair<>(LocalDateTime.now(), Arrays.asList(answersDTO)));
        }
        else{
            answersList = new LinkedList<>(answers.get(surveyID).getSecond());
            answersList.add(answersDTO);

            answers.put(surveyID, new Pair<>(LocalDateTime.now(), answersList));
        }
    }

    private void loadSurveyToCache(String surveyID){
        Response<SurveyDTO> surveyDTO = persistence.getSurvey(surveyID);

        if(!surveyDTO.isFailure())
            addSurveyToCache(surveyID, surveyDTO.getResult());
    }

    private void removeSurveyLRU(){
        LocalDateTime lastDate = LocalDateTime.now();
        String lastIndex = "";

        for(String index: surveys.keySet()){
            if(surveys.get(index).getFirst().isBefore(lastDate)){
                lastDate = surveys.get(index).getFirst();
                lastIndex = index;
            }
        }

        removeSurveyFromCache(lastIndex);
    }

    private void removeAnswersLRU(){
        LocalDateTime lastDate = LocalDateTime.now();
        String lastIndex = "";

        for(String index: answers.keySet()){
            if(answers.get(index).getFirst().isBefore(lastDate)){
                lastDate = answers.get(index).getFirst();
                lastIndex = index;
            }
        }

        removeAnswersFromCache(lastIndex);
    }

    private Response<Boolean> removeSurveyFromCache(String index){
        if(!surveys.containsKey(index))
            return new Response<>(false, true, "survey with id " + index + " is not in cache");

        surveys.remove(index);
        return new Response<>(true, false, "survey with id " + index + " removed successfully");
    }

    private Response<Boolean> removeAnswersFromCache(String index) {
        if(!answers.containsKey(index))
            return new Response<>(false, true, "survey with id " + index + " is not in cache");

        answers.remove(index);
        return new Response<>(true, false, "survey with id " + index + " removed successfully");
    }

    // for testing purpose only
    public void clearCache(){
        surveys.clear();
        answers.clear();
    }
}

//===========================================================

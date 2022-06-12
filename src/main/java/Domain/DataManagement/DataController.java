package Domain.DataManagement;


import Communication.DTOs.GoalDTO;
import Communication.DTOs.SurveyAnswersDTO;
import Communication.DTOs.SurveyDTO;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.FaultDetector.Rules.*;
import Domain.UsersManagment.UserController;
import Domain.UsersManagment.UserStateEnum;
import Domain.WorkPlan.GoalsManagement;
import Persistence.DbDtos.SchoolDBDTO;
import Persistence.DbDtos.UserDBDTO;
import Persistence.ExcelFormatter;
import Persistence.SchoolQueries;
import Persistence.UserQueries;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DataController {

    private SchoolQueries schoolDAO;

    private DataController() {
        this.schoolDAO = SchoolQueries.getInstance();
    }

    private static class CreateSafeThreadSingleton {
        private static final DataController INSTANCE = new DataController();
    }

    public static DataController getInstance() {
        return CreateSafeThreadSingleton.INSTANCE;
    }

    public Response<Boolean> loadSchoolsToDB() {
        Response<Boolean> emptyTable = ExcelFormatter.getInstance().isEmpty();

        if(emptyTable.isFailure())
            return emptyTable;

        if(emptyTable.getResult())
            return ExcelFormatter.getInstance().SchoolExcelToDb();

        return new Response<>(false, false, "Schools table is not empty");
    }

    public Response<List<String>> getCoordinatorsEmails(String workField){
        return UserQueries.getInstance().getCoordinatorEmails(workField); //todo check that there is actually an email assigned
    }

    public Response<Boolean> assignCoordinator(String currUser, String workField, String firstName, String lastName, String email, String phoneNumber, String school) {
        if(schoolDAO.schoolSymbolExists(school)){
            return UserController.getInstance().assignCoordinator(currUser, workField, firstName, lastName, email, phoneNumber, school);
        }
        else{
            return new Response<>(false, true, "no such school exists or coordinator already assigned");
        }
    }

    public Response<Boolean> removeCoordinator(String currUser, String workField, String school) {
        if(schoolDAO.schoolSymbolExists(school)){
            return UserController.getInstance().removeCoordinator(currUser, workField, school);
        }
        else {
            return new Response<>(false, true, "no such coordinator found");
        }
    }

    public Response<Boolean> insertSchool (SchoolDBDTO school){
        return schoolDAO.insertSchool(school);
    }

    public Response<Boolean> removeSchool (String symbol){
        return schoolDAO.removeSchool(symbol);
    }

    public Response<Boolean> updateSchool (String symbol, SchoolDBDTO school){
        return schoolDAO.updateSchool(symbol, school);
    }

    public SchoolDBDTO getSchool(String symbol){
        return schoolDAO.getSchool(symbol);
    }

    public Response<SchoolDBDTO> getSchool(String username, String symbol){
        Response<String> schoolsRes = UserController.getInstance().hasSchool(username, symbol);
        if(!schoolsRes.isFailure()){
            SchoolDBDTO school = schoolDAO.getSchool(symbol);
            if(!schoolsRes.getResult().equals("")) { //admin case - no workField
                Response<UserDBDTO> coordinator = UserController.getInstance().getCoordinator(username, schoolsRes.getResult(), symbol);
                if (!coordinator.isFailure() && coordinator.getResult() != null) {
                    school.setCoordinatorFirstName(coordinator.getResult().getFirstName());
                    school.setCoordinatorLastName(coordinator.getResult().getLastName());
                    school.setCoordinatorPhone(coordinator.getResult().getPhoneNumber());
                    school.setCoordinatorEmail(coordinator.getResult().getEmail());
                }
            }
            return new Response<>(school, false, "successfully acquired the school");
        }
        return new Response<>(null, true, schoolsRes.getErrMsg());
    }

    public Response<List<Pair<String, String>>> getUserSchools(String username) {  //pair<schoolName, symbol> //todo test it
        Response<List<String>> schoolsRes = UserController.getInstance().getUserSchools(username);
        if (!schoolsRes.isFailure()) {
            return schoolDAO.getSchoolNameAndSymbol(schoolsRes.getResult());
        } else return new Response<>(null, true, schoolsRes.getErrMsg());
    }

    public Response<Boolean> resetDB(){
        UserController userController = UserController.getInstance();
        userController.resetDB();
        userController.login("admin");

        userController.registerUserBySystemManager("admin","ronit", "1234abcd", UserStateEnum.SUPERVISOR, "", "tech",
            "ronit", "newe", "ronit@gmail.com", "055-555-5555",  "");

        userController.registerUserBySystemManager("admin","tal", "1234abcd", UserStateEnum.INSTRUCTOR, "ronit", "tech",
                    "tal", "kad", "tal@gmail.com", "055-555-5555",  "");

        userController.registerUserBySystemManager("admin","shaked", "1234abcd", UserStateEnum.INSTRUCTOR, "ronit", "tech",
                    "shaked", "ch", "shaked@gmail.com", "055-555-5555",  "");

        insertSchool(new SchoolDBDTO("1111111", "testing school", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 30));

        insertSchool(new SchoolDBDTO("2222222", "testing school2", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 31));

        assignCoordinator("admin", "tech", "aviad", "shal", "aviad@gmail.com", "0555555555", "1111111");

        userController.logout("admin");
        userController.login("ronit");

        String school1 = "1111111";
        String school2 = "2222222";
        String school3 = "33333333";

        userController.assignSchoolToUser("ronit", "tal", school1);
        userController.assignSchoolToUser("ronit", "shaked", school2);
        userController.assignSchoolToUser("ronit", "shaked", school3);

        // create survey
        SurveyDTO surveyDTO = new SurveyDTO(false, "1111", "survey1", "description",
                Arrays.asList("symbol", "open?", "numeric?", "multiple choice?"),
                Arrays.asList(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), Arrays.asList("correct", "wrong")),
                Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE), 2022);

        SurveyController.getInstance().createSurvey("ronit", surveyDTO);

        // create goals
        GoalDTO goalDTO1 = new GoalDTO(1, "yahad1", "", 1,
                5, "tech", 2022);
        GoalDTO goalDTO2 = new GoalDTO(2, "yahad2", "", 2,
                10, "tech",2022);

        GoalsManagement.getInstance().addGoalToField("tech", goalDTO1, 2022);
        GoalsManagement.getInstance().addGoalToField("tech", goalDTO2, 2022);

        Rule rule1 = new AndRule(Arrays.asList(new NumericBaseRule(2, Comparison.EQUAL, 40),
                new MultipleChoiceBaseRule(3, Arrays.asList(1))));

        Rule rule2 = new NumericBaseRule(2, Comparison.EQUAL, 30);

        // create rules
        SurveyController.getInstance().addRule("ronit", "1111", rule1, 1);
        SurveyController.getInstance().addRule("ronit", "1111", rule2, 2);

        // submit survey
        SurveyController.getInstance().submitSurvey("ronit", "1111");

        // add answers
        SurveyController.getInstance().addAnswers(new SurveyAnswersDTO("1111",
                new LinkedList<>(Arrays.asList("1111111", "open ans","30", "0")),
                new LinkedList<>(Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE))));

        SurveyController.getInstance().addAnswers(new SurveyAnswersDTO("1111",
                new LinkedList<>(Arrays.asList("2222222", "open ans", "40", "1")),
                new LinkedList<>(Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE))));

        // create another survey
        surveyDTO = new SurveyDTO(true, "2222", "title", "description",
                Arrays.asList("symbol", "open?", "numeric?", "multiple choice?"),
                Arrays.asList(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), Arrays.asList("correct", "wrong")),
                Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE), 2022);

        SurveyController.getInstance().createSurvey("ronit", surveyDTO);

        userController.logout("ronit");

        userController.login("tal");
        userController.setWorkingTime("tal", 0, LocalTime.of(8, 30).toString(), LocalTime.of(10, 30).toString(), LocalTime.of(11, 0).toString(), LocalTime.of(13, 0).toString());
        userController.logout("tal");

        userController.login("shaked");
        userController.setWorkingTime("shaked", 3, LocalTime.of(10, 0).toString(), LocalTime.of(12, 0).toString(), LocalTime.of(13, 0).toString(), LocalTime.of(15, 0).toString());
        userController.logout("shaked");

        return new Response<>(true, false, "reset db");
    }

    //for test purposes only
    public void clearSchools() {
        schoolDAO.deleteSchools();
    }

    //for test purposes only
    public void addOneSchool() {
        SchoolDBDTO schoolDBDTO = new SchoolDBDTO();
        schoolDBDTO.setSymbol("1");
        schoolDAO.insertSchool(schoolDBDTO);
    }

    //for test purposes only
    public Response<Boolean> removeCoordinatorTester(String school){
        if(schoolDAO.schoolSymbolExists(school)){
            return UserController.getInstance().removeCoordinatorTester("ronit", "ignore", school);
        }
        else {
            return new Response<>(false, true, "no such coordinator found");
        }
    }

    //for test purposes only
    public Response<Boolean> assignCoordinatorTester(String school){
        if(schoolDAO.schoolSymbolExists(school)){
            return UserController.getInstance().assignCoordinatorTester("ronit", "ignore", "coor", "dinator", "a@gmail.com", "0555555555", school);
        }
        else {
            return new Response<>(false, true, "no such coordinator found");
        }
    }
}

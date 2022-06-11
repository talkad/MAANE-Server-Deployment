package Persistence;

import Communication.DTOs.QuestionDTO;
import Communication.DTOs.RuleDTO;
import Communication.DTOs.SurveyAnswersDTO;
import Communication.DTOs.SurveyDTO;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.FaultDetector.Rules.Comparison;
import Domain.DataManagement.FaultDetector.Rules.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


@Repository
@Slf4j
public class SurveyPersistence {


    private static class CreateSafeThreadSingleton {
        private static final SurveyPersistence INSTANCE = new SurveyPersistence();
    }

    public static SurveyPersistence getInstance() {
        return SurveyPersistence.CreateSafeThreadSingleton.INSTANCE;
    }

    //========================== Survey ==========================

    public Response<Boolean> insertSurvey(SurveyDTO surveyDTO) {
        Connect.createConnection();
        int rows = 0;
        String sql = "INSERT INTO \"Surveys\" (id, title, description, submit, year) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, surveyDTO.getId());
            preparedStatement.setString(2, surveyDTO.getTitle());
            preparedStatement.setString(3, surveyDTO.getDescription());
            preparedStatement.setBoolean(4, false);
            preparedStatement.setInt(5, surveyDTO.getYear());

            rows = preparedStatement.executeUpdate();

            insertQuestions(surveyDTO.getId(), surveyDTO.getQuestions());

            insertAnswers(surveyDTO.getId(), ListListToStringList(surveyDTO.getAnswers()), surveyDTO.getTypes());
            Connect.closeConnection();

            log.info("DB: insert survey successfully");

        } catch (SQLException e) {
            log.error("DB: failed to insert survey: \n" + e.getMessage());
            return new Response<>(false, true, "bad Db writing");
        }

        return rows>0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "bad Db writing");
    }

    public Response<Integer> getSurveyYear(String surveyID) {
        int year = -1;

        Connect.createConnection();
        String sqlSurvey = "SELECT year FROM \"Surveys\" WHERE id = ?";
        try {
            PreparedStatement statement = Connect.conn.prepareStatement(sqlSurvey);
            statement.setString(1, surveyID);
            ResultSet resultSurvey = statement.executeQuery();

            if (resultSurvey.next()) {
                year = resultSurvey.getInt("year");
                Connect.closeConnection();
            } else {
                Connect.closeConnection();
                return new Response<>(year, true, "failed to get survey");
            }

            return new Response<>(year, false, "survey loaded successfully");
        } catch(SQLException e) {
            return new Response<>(year, true, "Failed to get survey");
        }
    }

    public Response<Boolean> removeSurvey(String surveyID) {
        Connect.createConnection();
        String sql = "BEGIN;\n";
        sql += "DELETE FROM  \"Surveys\" WHERE id=?;\n";
        sql += "DELETE FROM  \"Questions\" WHERE survey_id=?;\n";
        sql += "DELETE FROM  \"MultiChoices\" WHERE survey_id=?;\n";
        sql += "COMMIT;\n";

        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);

            // remove survey
            preparedStatement.setString(1, surveyID);
            preparedStatement.setString(2, surveyID);
            preparedStatement.setString(3, surveyID);

            preparedStatement.executeUpdate();
            Connect.closeConnection();

            log.info("DB: removed survey successfully");

        } catch (SQLException e) {
            log.error("DB: failed to remove survey \n" + e.getMessage());

            return new Response<>(false, true, "survey deletion failed");
        }

        return new Response<>(true, false, "survey removed successfully");
    }

    public void insertQuestions (String surveyId, List<String> questions) {
        String sql = "INSERT INTO \"Questions\" (survey_id, index, question) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            for (String question : questions){
                preparedStatement.setString(1, surveyId);
                preparedStatement.setInt(2, questions.indexOf(question));
                preparedStatement.setString(3, question);
                preparedStatement.executeUpdate();
            }

            log.info("DB: insert questions successfully");

        } catch (SQLException e) {
            log.error("DB: failed to questions: \n" + e.getMessage());
        }
    }

    public Response<Boolean> surveySubmission(String surveyID) {
        Connect.createConnection();
        String sql = "UPDATE \"Surveys\" SET submit=TRUE, year=?  WHERE id=?;\n";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);

            preparedStatement.setInt(1, Calendar.getInstance().get(Calendar.YEAR));
            preparedStatement.setString(2, surveyID);
            preparedStatement.executeUpdate();
            Connect.closeConnection();

            log.info("DB: survey submitted successfully");

        } catch (SQLException e) {
            log.error("DB: failed to submit survey \n" + e.getMessage());

            return new Response<>(false, true, "failed to submit survey ");
        }

        return new Response<>(true, false, "survey submitted successfully");
    }

    public Response<Boolean> addQuestion(QuestionDTO questionDTO, int question_index) {
        Connect.createConnection();

        String sql = "INSERT INTO \"Questions\" (survey_id, index, question) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, questionDTO.getSurveyID());
            preparedStatement.setInt(2, question_index);
            preparedStatement.setString(3, questionDTO.getQuestion());
            preparedStatement.executeUpdate();

            log.info("DB: insert question successfully");

        } catch (SQLException e) {
            log.error("DB: failed to add question \n" + e.getMessage());
        }

        String sql2 = "INSERT INTO \"MultiChoices\" (survey_id, question_index, answer_type, choices) VALUES (?, ?, ?, ?)";
        try{
            preparedStatement = Connect.conn.prepareStatement(sql2);
            preparedStatement.setString(1, questionDTO.getSurveyID());
            preparedStatement.setInt(2, question_index);
            preparedStatement.setString(3, questionDTO.getType().getType());
            preparedStatement.setString(4, questionDTO.getAnswers().toString());

            preparedStatement.executeUpdate();
            Connect.closeConnection();

            log.info("DB: insert question successfully");

        } catch (SQLException e) {
            log.error("DB: failed to add question \n" + e.getMessage());

            return new Response<>(false, true, "failed to add answers");
        }

        return new Response<>(true, false, "inserted question successfully");
    }

    public Response<Boolean> removeQuestions(String surveyID, int questionID, int questionsNum) {
        int indexer = 5;

        Connect.createConnection();
        String sql = "BEGIN;\n";
        sql += "DELETE FROM  \"Questions\" WHERE survey_id=? and index=?;\n";
        sql += "DELETE FROM  \"MultiChoices\" WHERE survey_id=? and question_index=?;\n";

        for(int i = questionID + 1; i < questionsNum; i++){
            sql += "UPDATE \"Questions\" SET index=index-1  WHERE survey_id=? and index=?;\n";
            sql += "UPDATE \"MultiChoices\" SET question_index=question_index-1  WHERE survey_id=? and question_index=?;\n";
        }

        sql += "COMMIT;\n";

        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);

            // remove question by id
            preparedStatement.setString(1, surveyID);
            preparedStatement.setInt(2, questionID);

            preparedStatement.setString(3, surveyID);
            preparedStatement.setInt(4, questionID);

            // update question indexes
            for(int i = questionID + 1; i < questionsNum; i++){
                preparedStatement.setString(indexer, surveyID);
                preparedStatement.setInt(indexer+1, i);

                preparedStatement.setString(indexer+2, surveyID);
                preparedStatement.setInt(indexer+3, i);

                indexer += 4;
            }

            preparedStatement.executeUpdate();
            Connect.closeConnection();

            log.info("DB: removed question successfully");

        } catch (SQLException e) {
            log.error("DB: failed to remove question \n" + e.getMessage());

            return new Response<>(false, true, "question deletion failed");
        }

        return new Response<>(true, false, "question removed successfully");
    }

    public void insertAnswers (String surveyId, List<String> answers, List<AnswerType> answerTypes) {
        int indexer = 0;

        String sql = "INSERT INTO \"MultiChoices\" (survey_id, question_index, answer_type, choices) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            for (String answer: answers) {
                preparedStatement.setString(1, surveyId);
                preparedStatement.setInt(2, indexer);
                preparedStatement.setString(3, answerTypes.get(indexer).toString());
                preparedStatement.setString(4, answer);
                preparedStatement.executeUpdate();

                indexer++;
            }

            log.info("DB: added MultiChoices answers successfully");

        } catch (SQLException e) {
            log.error("DB: failed to add MultiChoices answers \n" + e.getMessage());
        }
    }

    private List<String> ListListToStringList(List<List<String>> l){
        List<String> result = new LinkedList<>();

        for(List<String> list: l){
            result.add(list.toString());
        }
        return result;
    }

    public Response<SurveyDTO> getSurvey(String id)  {
        Connect.createConnection();
        String sqlSurvey = "SELECT * FROM \"Surveys\" WHERE id = ?";
        try {
            PreparedStatement statement = Connect.conn.prepareStatement(sqlSurvey);
            statement.setString(1, id);
            ResultSet resultSurvey = statement.executeQuery();
            SurveyDTO surveyDTO = new SurveyDTO();

            if (resultSurvey.next()) {
                surveyDTO.setPublished(resultSurvey.getBoolean("submit"));
                surveyDTO.setId(resultSurvey.getString("id"));
                surveyDTO.setTitle(resultSurvey.getString("title"));
                surveyDTO.setDescription(resultSurvey.getString("description"));
                List<String> questions = getSurveyQuestions(id);
                List<List<String>> answers = getSurveyAnswers(id);
                List<AnswerType> types = getSurveyTypes(id);
                surveyDTO.setQuestions(questions);
                surveyDTO.setAnswers(answers);
                surveyDTO.setTypes(types);
                surveyDTO.setYear(resultSurvey.getInt("year"));

                Connect.closeConnection();
            } else {
                Connect.closeConnection();
                return new Response<>(null, true, "failed to get survey");
            }

            return new Response<>(surveyDTO, false, "survey loaded successfully");
        } catch(SQLException e) {
            return new Response<>(null, true, "Failed to get survey");
        }
    }

    public Response<Boolean> getSurveySubmission(String id)  {
        boolean submit;

        Connect.createConnection();
        String sqlSurvey = "SELECT submit FROM \"Surveys\" WHERE id = ?";
        try {
            PreparedStatement statement = Connect.conn.prepareStatement(sqlSurvey);
            statement.setString(1, id);
            ResultSet resultSurvey = statement.executeQuery();

            if (resultSurvey.next()) {
                submit = resultSurvey.getBoolean("submit");

                Connect.closeConnection();
            } else {
                Connect.closeConnection();
                return new Response<>(false, true, "failed to get survey");
            }

        } catch(SQLException e) {
            return new Response<>(false, true, "Failed to get survey");
        }

        return new Response<>(submit, false, "survey loaded successfully");
    }

    private List<String> getSurveyQuestions (String survey_id) throws SQLException {
        String query = "SELECT * FROM \"Questions\" WHERE survey_id = ?";
        PreparedStatement statement = Connect.conn.prepareStatement(query);
        statement.setString(1, survey_id);
        ResultSet result = statement.executeQuery();
        List<String> questions = new LinkedList<>();

        while (result.next()) {
            // int index = result.getInt("index");
            String question = result.getString("question");
            questions.add(question);
        }
        return questions;
    }

    private List<List<String>> getSurveyAnswers (String survey_id) throws SQLException {
        String query = "SELECT * FROM \"MultiChoices\" WHERE survey_id = ?";
        PreparedStatement statement = Connect.conn.prepareStatement(query);
        statement.setString(1, survey_id);
        ResultSet result = statement.executeQuery();
        List<List<String>> answers = new LinkedList<>();

        while (result.next()) {
            String answer = result.getString("choices");
            answer = answer.substring(1,answer.length()-1);
            String [] optionsArr = answer.split(",");
            List<String> optionsList = new LinkedList<>(Arrays.asList(optionsArr));
            answers.add(optionsList);
        }
        return answers;
    }

    private List<AnswerType> getSurveyTypes (String survey_id) throws SQLException {
        String query = "SELECT * FROM \"MultiChoices\" WHERE survey_id = ?";
        PreparedStatement statement = Connect.conn.prepareStatement(query);
        statement.setString(1, survey_id);
        ResultSet result = statement.executeQuery();
        List<AnswerType> answers = new LinkedList<>();

        while (result.next()) {
            String answer = result.getString("answer_type");
            answers.add(AnswerType.valueOf(answer));
        }
        return answers;
    }

    //===========================================================

    //========================== Rules ==========================

    public void insertRule(String survey_id, int goalID, RuleDTO dto) {
        Connect.createConnection();
        String sql = "INSERT INTO \"Rules\" (survey_id, goal_id, type, comparison, question_id, answer) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, survey_id);
            preparedStatement.setInt(2, goalID);
            preparedStatement.setString(3, dto.getType().getType());
            preparedStatement.setString(4, dto.getComparison().getComparison());
            preparedStatement.setInt(5, dto.getQuestionID());
            preparedStatement.setString(6, dto.getAnswers().toString());
            preparedStatement.execute();

            ResultSet last_updated_person = preparedStatement.getResultSet();
            last_updated_person.next();
            int last_updated_person_id = last_updated_person.getInt(1);

            if(dto.getSubRules() != null) {
                for (RuleDTO rule : dto.getSubRules()) {
                    insertSubRule(survey_id, goalID, rule, last_updated_person_id);
                }
            }

            Connect.closeConnection();
            log.info("DB: added rules successfully");

        } catch (SQLException e) {
            log.error("DB: failed to add rules \n" + e.getMessage());
        }
    }

    private void insertSubRule(String survey_id, int goalID, RuleDTO dto, int parent_id) {
        String sql = "INSERT INTO \"Rules\" (survey_id, goal_id, type, comparison, question_id, answer, parent_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, survey_id);
            preparedStatement.setInt(2, goalID);
            preparedStatement.setString(3, dto.getType().getType());
            preparedStatement.setString(4, dto.getComparison().getComparison());
            preparedStatement.setInt(5, dto.getQuestionID());
            preparedStatement.setString(6, dto.getAnswers().toString());
            preparedStatement.setInt(7, parent_id);
            preparedStatement.executeUpdate();

            if(dto.getSubRules() != null) {
                int parentID = getMaxRuleIndex();

                for (RuleDTO rule : dto.getSubRules()) {
                    insertSubRule(survey_id, goalID, rule, parentID);
                }
            }

            log.info("DB: added  sub rules successfully");

        } catch (SQLException e) {
            log.error("DB: failed to add sub rules \n" + e.getMessage());
        }
    }

    private int getMaxRuleIndex(){
        int maxID = 0;

        String sql = "SELECT MAX(id) FROM \"Rules\"";
        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);

            ResultSet result = statement.executeQuery();
            if(result.next())
                maxID = result.getInt(1);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return maxID;
    }

    public Response<Boolean> removeRule (int ruleID) {
        Connect.createConnection();
        String sql = "DELETE FROM \"Rules\" WHERE id = ?";
        int rows = 0;
        try (PreparedStatement pstmt = Connect.conn.prepareStatement(sql)) {
            pstmt.setInt(1, ruleID);
            rows = pstmt.executeUpdate();
            removeSubRules(ruleID);

            Connect.closeConnection();

            log.info("DB: removed rules successfully");

        } catch (SQLException e) {
            log.error("DB: failed to remove rules \n" + e.getMessage());

            return new Response<>(false, true, "bad Db writing");
        }

        return new Response<>(true, false, "");
    }

    public Response<Boolean> removeRules(String surveyID) {
        Connect.createConnection();
        String sql = "DELETE FROM \"Rules\" WHERE survey_id = ?";
        int rows = 0;
        try (PreparedStatement pstmt = Connect.conn.prepareStatement(sql)) {
            pstmt.setString(1, surveyID);
            rows = pstmt.executeUpdate();

            Connect.closeConnection();

            log.info("DB: removed rules successfully");
            return new Response<>(true, false, "");

        } catch (SQLException e) {
            log.error("DB: failed to remove rules \n" + e.getMessage());
        }

        return new Response<>(false, true, "bad Db writing");
    }

    private void removeSubRules (int parentId){
        String sql = "DELETE FROM \"Rules\" WHERE parent_id = ?";
        try (PreparedStatement pstmt = Connect.conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            pstmt.executeUpdate();

            log.info("DB: removed sub rules successfully");

        } catch (SQLException e) {
            log.error("DB: failed to remove sub rules \n" + e.getMessage());
        }
    }

    public List<Pair<RuleDTO, Integer>> getRules(String surveyID) {
        Connect.createConnection();
        String query = "SELECT * FROM \"Rules\" WHERE survey_id = ?";
        PreparedStatement statement;
        List<Pair<RuleDTO, Integer>> rules = new LinkedList<>();;
        try {
            statement = Connect.conn.prepareStatement(query);
            statement.setString(1, surveyID);
            ResultSet result = statement.executeQuery();
            while (result.next()) {

                if (result.getInt("parent_id") == 0) {

                    String type = result.getString("type");
                    String comparison = result.getString("comparison");
                    int questionId = result.getInt("question_id");
                    String answer = result.getString("answer");
                    int id = result.getInt("id");
                    int goalID = result.getInt("goal_id");

                    List<RuleDTO> subRules = getSubRules(id);

                    RuleDTO ruleDTO = new RuleDTO(subRules, RuleType.valueOf(type), Comparison.valueOf(comparison), questionId, parseList(Arrays.asList(answer.substring(1, answer.length() - 1).split(", "))));
                    Pair<RuleDTO, Integer> toAdd = new Pair<>(ruleDTO, goalID);
                    rules.add(toAdd);
                }
            }
            Connect.closeConnection();

            log.info("DB: get rules successfully");

        } catch (SQLException e) {
            log.error("DB: failed to get rules \n" + e.getMessage());
        }

        return rules;
    }

    private List<Integer> parseList(List<String> stringList) {
        List<Integer> integerList = new LinkedList<>();

        for(String s: stringList)
            try {
                integerList.add(Integer.parseInt(s));
            } catch(Exception e){
                integerList.add(-1);
            }

        return integerList;
    }

    private List<RuleDTO> getSubRules (int parent_id) {
        String query = "SELECT * FROM \"Rules\" WHERE parent_id = ?";
        PreparedStatement statement;
        List<RuleDTO> rules = new LinkedList<>();;
        try {
            statement = Connect.conn.prepareStatement(query);
            statement.setInt(1,parent_id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String type = result.getString("type");
                String comparison = result.getString("comparison");
                int questionId = result.getInt("question_id");
                String answer = result.getString("answer");
                int id = result.getInt("id");
                List<RuleDTO> subRules = getSubRules(id);
                RuleDTO ruleDTO = new RuleDTO(subRules, RuleType.valueOf(type), Comparison.valueOf(comparison), questionId, parseList(Arrays.asList(answer.substring(1, answer.length() - 1).split(", "))));
                rules.add(ruleDTO);
            }
        } catch (SQLException e) {e.printStackTrace();}
        return rules;
    }

    //list of survey id's
    public List<String> getSurveyTitles(List<String> surveyIds) {
        List<String> titles = new LinkedList<>();
        Connect.createConnection();
        try {
            for (String title : surveyIds) { titles.addAll(getTitles(title)); }
            Connect.closeConnection();
        } catch (SQLException e) { e.printStackTrace(); }
        return titles;
    }

    //all titles of single survey id
    private List<String> getTitles (String surveyId) throws SQLException {
        String query = "SELECT * FROM \"Surveys\" WHERE id = ?";
        PreparedStatement statement = Connect.conn.prepareStatement(query);
        statement.setString(1, surveyId);
        ResultSet result = statement.executeQuery();
        List<String> titles = new LinkedList<>();

        while (result.next()) {
            String title = result.getString("title");
            titles.add(title);
        }
        return titles;
    }

    //===========================================================

    //========================== Answers ==========================

    public List<SurveyAnswersDTO> getAnswers(String surveyId) {
        Connect.createConnection();
        String query = "SELECT * FROM \"Answers\" WHERE survey_id = ?";
        PreparedStatement statement = null;

        List<SurveyAnswersDTO> output = new LinkedList<>();
        SurveyAnswersDTO surveyAnswersDTO;
        String symbol = "";
        try {
            statement = Connect.conn.prepareStatement(query);
            statement.setString(1, surveyId);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                symbol = result.getString("school_symbol");
                String answer = result.getString("answer");
                String answerType = result.getString("answer_type");

                List<String> parsedAnswers = Arrays.asList(answer.split(","));
                List<String> answers = new LinkedList<>(parsedAnswers);

                List<String> parsedTypes = Arrays.asList(answerType.split(","));
                List<String> typesStrings = new LinkedList<>(parsedTypes);
                List<AnswerType> types = new LinkedList<>();
                for (String s : typesStrings) { types.add(AnswerType.valueOf(s)); };

                types.add(0, AnswerType.OPEN_ANSWER);
                answers.add(0, symbol);

                surveyAnswersDTO = new SurveyAnswersDTO(surveyId, answers, types);
                output.add(surveyAnswersDTO);
            }

            Connect.closeConnection();

            log.info("DB: got answers successfully");

        } catch (SQLException e) {
            log.error("DB: failed to get answers \n" + e.getMessage());
        }

        return output;
    }

    public SurveyAnswersDTO getAnswersPerSchool(String surveyID, String symbol) {
        Connect.createConnection();
        String query = "SELECT * FROM \"Answers\" WHERE survey_id = ? AND school_symbol = ?";
        PreparedStatement statement;

        SurveyAnswersDTO surveyAnswersDTO = null;
        try {

            statement = Connect.conn.prepareStatement(query);
            statement.setString(1, surveyID);
            statement.setString(2, symbol);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                symbol = result.getString("school_symbol");
                String answer = result.getString("answer");
                String answerType = result.getString("answer_type");

                List<String> parsedAnswers = Arrays.asList(answer.split(","));
                List<String> answers = new LinkedList<>(parsedAnswers);

                List<String> parsedTypes = Arrays.asList(answerType.split(","));
                List<String> typesStrings = new LinkedList<>(parsedTypes);
                List<AnswerType> types = new LinkedList<>();
                for (String s : typesStrings) { types.add(AnswerType.valueOf(s)); };

//                types.add(0, AnswerType.OPEN_ANSWER);
//                answers.add(0, symbol+"");

                surveyAnswersDTO = new SurveyAnswersDTO(surveyID, answers, types);
            }

            Connect.closeConnection();

            log.info("DB: got answers successfully");

        } catch (SQLException e) {
            log.error("DB: failed to get answers \n" + e.getMessage());
        }

        return surveyAnswersDTO;
    }

    public void insertCoordinatorAnswers(String id, String symbol, List<String> answers, List<AnswerType> types) {
        Connect.createConnection();
        String sql = "INSERT INTO \"Answers\" (survey_id, school_symbol, answer, answer_type) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, symbol);
            preparedStatement.setString(3, ListToString(answers));
            preparedStatement.setString(4, ListATToString(types));
            preparedStatement.executeUpdate();

            Connect.closeConnection();

            log.info("DB: added coordinator answers successfully");

        } catch (SQLException e) {
            log.error("DB: failed to add coordinator answers \n" + e.getMessage());
        }
    }

    public void removeCoordinatorAnswers(String surveyID, String symbol) {
        Connect.createConnection();
        String sql = "DELETE FROM  \"Answers\" WHERE survey_id = ? AND school_symbol = ?";

        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);

            // remove survey
            preparedStatement.setString(1, surveyID);
            preparedStatement.setString(2, symbol);

            preparedStatement.executeUpdate();
            Connect.closeConnection();

            log.info("DB: removed answers successfully");

        } catch (SQLException e) {
            log.error("DB: failed to remove answers \n" + e.getMessage());
        }
    }

    private String ListToString (List<String> list){ //output seperated by ,
        StringBuilder output = new StringBuilder();
        for (String s : list){
            output.append(s).append(",");
        }
        return output.substring(0,output.length()-1); //drop last ,
    }

    private String ListATToString (List<AnswerType> list){ //output seperated by ,
        StringBuilder output = new StringBuilder();
        for (AnswerType s : list){
            output.append(s).append(",");
        }
        return output.substring(0,output.length()-1); //drop last ,
    }

}

//===========================================================

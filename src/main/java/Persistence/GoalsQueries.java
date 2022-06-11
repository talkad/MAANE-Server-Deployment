package Persistence;

import Communication.DTOs.GoalDTO;
import Domain.CommonClasses.Response;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class GoalsQueries {

    private GoalsQueries() {

    }

    private static class CreateSafeThreadSingleton {
        private static final GoalsQueries INSTANCE = new GoalsQueries();
    }

    public static GoalsQueries getInstance() {
        return GoalsQueries.CreateSafeThreadSingleton.INSTANCE;
    }

    public Response<GoalDTO> getGoal(int goalid) {//todo remove it later potentially
        Connect.createConnection();
        String sql = "SELECT * FROM \"Goals\" WHERE goalid = ?";
        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);

            statement.setInt(1, goalid);
            ResultSet result = statement.executeQuery();
            if(result.next()) {
                GoalDTO goalDTO = new GoalDTO();
                goalDTO.setGoalId(result.getInt("goalid"));
                goalDTO.setTitle(result.getString("title"));
                goalDTO.setDescription(result.getString("description"));
                goalDTO.setQuarterly(result.getInt("quarterly"));
                goalDTO.setWeight(result.getInt("weight"));
                goalDTO.setWorkField(result.getString("workfield"));
                goalDTO.setYear(result.getInt("goalyear"));
                Connect.closeConnection();
                return new Response<>(goalDTO, false, "successfully acquired goal");
            }
            Connect.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new Response<>(null, true, "failed to get goal");
    }

    public Response<List<GoalDTO>> getGoals(String workField, Integer year) {
        Connect.createConnection();
        String sql = "SELECT * FROM \"Goals\" WHERE workfield = ? AND goalyear = ?";
        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);

            statement.setString(1, workField);
            statement.setInt(2, year);

            ResultSet result = statement.executeQuery();
            List<GoalDTO> goals = new Vector<>();
            while(result.next())
            {
                GoalDTO goalDTO = new GoalDTO();
                goalDTO.setGoalId(result.getInt("goalid"));
                goalDTO.setTitle(result.getString("title"));
                goalDTO.setDescription(result.getString("description"));
                goalDTO.setQuarterly(result.getInt("quarterly"));
                goalDTO.setWeight(result.getInt("weight"));
                goalDTO.setWorkField(result.getString("workfield"));
                goalDTO.setYear(result.getInt("goalyear"));
                goals.add(goalDTO);
            }
            Connect.closeConnection();
            return new Response<>(goals, false, "successfully acquired goal");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new Response<>(null, true, "failed to get goal");
    }

    public Response<List<GoalDTO>> getGoalsById(List<Integer> goalsIds) {
        Connect.createConnection();
        String sql = "SELECT * FROM \"Goals\" WHERE goalid IN (" + "?, ".repeat(goalsIds.size());
        sql = sql.substring(0, sql.length() - 2) + ")";

        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);
            //String listString = "(";//goalId.toString();
            int i = 1;
            for (Integer goalId: goalsIds) {
                statement.setInt(i, goalId);
                i++;
            }
            /*for (Integer goalId: goalsIds) {
                listString = listString + goalId.toString() + ", ";
            }
            listString = listString.substring(0, listString.length() - 2);
            listString += ")";*/
            /*listString = listString.replace("[", "(");
            listString = listString.replace("]", ")");*/
            //System.out.println(listString);
            //statement.setString(1, listString);

            ResultSet result = statement.executeQuery();
            List<GoalDTO> goals = new Vector<>();
            while(result.next())
            {
                GoalDTO goalDTO = new GoalDTO();
                goalDTO.setGoalId(result.getInt("goalid"));
                goalDTO.setTitle(result.getString("title"));
                goalDTO.setDescription(result.getString("description"));
                goalDTO.setQuarterly(result.getInt("quarterly"));
                goalDTO.setWeight(result.getInt("weight"));
                goalDTO.setWorkField(result.getString("workfield"));
                goalDTO.setYear(result.getInt("goalyear"));
                goals.add(goalDTO);
            }
            Connect.closeConnection();
            return new Response<>(goals, false, "successfully acquired goal");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new Response<>(null, true, "failed to get goal");
    }

    public Response<Boolean> insertGoal(GoalDTO goalDTO){
        Connect.createConnection();
        int rows = 0;
        String sql = "INSERT INTO \"Goals\"(goalId, title, description , quarterly, weight, workField, goalyear) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);

            preparedStatement.setInt(1, getNextGoalID());
            preparedStatement.setString(2, goalDTO.getTitle());
            preparedStatement.setString(3, goalDTO.getDescription());
            preparedStatement.setInt(4, goalDTO.getQuarterly());
            preparedStatement.setInt(5, goalDTO.getWeight());
            preparedStatement.setString(6, goalDTO.getWorkField());
            preparedStatement.setInt(7, goalDTO.getYear());
            rows = preparedStatement.executeUpdate();
            Connect.closeConnection();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rows > 0 ? new Response<>(true, false, "successfully added goals to the work field: " + goalDTO.getWorkField()) :
                new Response<>(false, true, "bad Db writing");
    }

    // for testing purposes only
    public Response<Boolean> insertGoalMock(GoalDTO goalDTO){
        Connect.createConnection();
        int rows = 0;
        String sql = "INSERT INTO \"Goals\"(goalId, title, description , quarterly, weight, workField, goalyear) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);

            preparedStatement.setInt(1, goalDTO.getGoalId());
            preparedStatement.setString(2, goalDTO.getTitle());
            preparedStatement.setString(3, goalDTO.getDescription());
            preparedStatement.setInt(4, goalDTO.getQuarterly());
            preparedStatement.setInt(5, goalDTO.getWeight());
            preparedStatement.setString(6, goalDTO.getWorkField());
            preparedStatement.setInt(7, goalDTO.getYear());
            rows = preparedStatement.executeUpdate();
            Connect.closeConnection();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rows > 0 ? new Response<>(true, false, "successfully added goals to the work field: " + goalDTO.getWorkField()) :
                new Response<>(false, true, "bad Db writing");
    }

    private int getNextGoalID(){
        int maxID = 0;

        Connect.createConnection();
        String sql = "SELECT MAX(goalid) FROM \"Goals\"";
        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);

            ResultSet result = statement.executeQuery();
            if(result.next())
                maxID = result.getInt(1);

            Connect.closeConnection();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return maxID + 1;
    }

    public Response<Boolean> removeGoal(int goalId){
        Connect.createConnection();
        int rows = 0;
        String sql = "DELETE FROM \"Goals\" WHERE goalid = ?";//todo see if its possible to make it as one query

        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setInt(1, goalId);
            rows = preparedStatement.executeUpdate();
            Connect.closeConnection();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rows > 0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "bad Db writing");
    }

    public void deleteGoals() {
        Connect.createConnection();
        String sql = "TRUNCATE \"Goals\"";

        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.executeUpdate();

            Connect.closeConnection();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

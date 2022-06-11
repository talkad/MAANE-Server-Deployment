package Persistence;

import Communication.DTOs.ActivityDTO;
import Communication.DTOs.WorkPlanDTO;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

@Repository
public class WorkPlanQueries {

    private static class CreateSafeThreadSingleton {
        private static final WorkPlanQueries INSTANCE = new WorkPlanQueries();
    }

    public static WorkPlanQueries getInstance() {
        return WorkPlanQueries.CreateSafeThreadSingleton.INSTANCE;
    }

    public Response<Boolean> insertUserWorkPlan(String username, WorkPlanDTO workPlan, Integer year){
        Connect.createConnection();
        int rows = 0;
        String sql = "INSERT INTO \"WorkPlans\" (username, year, date, activities, endactivity) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            for (Pair<LocalDateTime, ActivityDTO> annualPlan : workPlan.getCalendar()) {
                if(annualPlan.getSecond() != null) {
                    LocalDateTime date = annualPlan.getFirst();
                    String activities = ActivitiesToString(annualPlan.getSecond());
                    preparedStatement.setString(1, username);
                    preparedStatement.setInt(2, year);
                    preparedStatement.setTimestamp(3, Timestamp.valueOf(date));
                    preparedStatement.setString(4, activities);
                    preparedStatement.setTimestamp(5, Timestamp.valueOf(annualPlan.getSecond().getEndActivity()));
                    rows = preparedStatement.executeUpdate();
                }
            }

            Connect.closeConnection();
        } catch (SQLException e) {e.printStackTrace();}

        return rows > 0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "bad Db writing");
    }

    /***
     *
     * @param activity
     * @return String in the shape of ActivityDto_1 | ActivityDto_2 | ActivityDto_3 ....
     * while ActivityDto in the shape of school_id title
     */
    private String ActivitiesToString (ActivityDTO activity){
        if (activity == null) return "";
        return activity.getSchoolId() + " " + activity.getGoalId().toString() + " " + activity.getTitle();
    }

    public Response<WorkPlanDTO> getUserWorkPlanByYearAndMonth(String username, Integer year, Integer month){
        Connect.createConnection();
        String sql = "SELECT * FROM \"WorkPlans\" WHERE username = ? AND (year = ? AND EXTRACT(MONTH FROM date) = ?)";
        PreparedStatement statement;
        WorkPlanDTO workPlanDTO;
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setInt(2, year);
            statement.setInt(3, month);

            ResultSet result = statement.executeQuery();

            List<Pair<LocalDateTime, ActivityDTO>> calendar = new LinkedList<>();
            while(result.next()) {
                LocalDateTime date = result.getTimestamp("date").toInstant().atZone(TimeZone.getTimeZone("Asia/Jerusalem").toZoneId()).toLocalDateTime();
                LocalDateTime endActivity = result.getTimestamp("endactivity").toInstant().atZone(TimeZone.getTimeZone("Asia/Jerusalem").toZoneId()).toLocalDateTime();

                String activities = result.getString("activities");
                ActivityDTO activityDTO = StringToActivities(activities, endActivity);
                Pair<LocalDateTime, ActivityDTO> dateAndActivity = new Pair<>(date, activityDTO);
                calendar.add(dateAndActivity);
            }
            workPlanDTO = new WorkPlanDTO(calendar);

            Connect.closeConnection();
            return new Response<>(workPlanDTO, false, "successfully got work plans");
        } catch (SQLException throwables) {throwables.printStackTrace();}
        return new Response<>(null, true, "failed to get work plans");
    }

    /***
     *
     * @param activity string
     * @return parse back
     */
    protected ActivityDTO StringToActivities (String activity, LocalDateTime endActivity){
        if(activity == null || activity.equals("")) return new ActivityDTO("", -1, "", null);//todo problem
        String [] activitiesArray = activity.split(" ", 3);
        String schoolId = activitiesArray[0];
        Integer goalId = Integer.parseInt(activitiesArray[1]);
        String title = activitiesArray[2];
        return new ActivityDTO(schoolId, goalId, title, endActivity);
    }

    public Response<Boolean> updateActivity(String username, LocalDateTime currActStart, LocalDateTime newActStart, LocalDateTime newActEnd) { //todo find a way to block collision
        Connect.createConnection();
        int rows = 0;
        String sql = "UPDATE \"WorkPlans\" SET date = ?, endactivity = ? WHERE (username = ? AND date = ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);

            preparedStatement.setTimestamp(1, Timestamp.valueOf(newActStart));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(newActEnd));
            preparedStatement.setString(3, username);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(currActStart));

            rows = preparedStatement.executeUpdate();
            Connect.closeConnection();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rows > 0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "failed to write to db");
    }

    public Response<Boolean> addActivity(String username, LocalDateTime startAct, ActivityDTO activity, int year) {
        Connect.createConnection();
        int rows = 0;
        String sql = "INSERT INTO \"WorkPlans\" (username, year, date, activities, endactivity) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            String activities = ActivitiesToString(activity);
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, year);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(startAct));
            preparedStatement.setString(4, activities);
            preparedStatement.setTimestamp(5, Timestamp.valueOf(activity.getEndActivity()));
            rows = preparedStatement.executeUpdate();


            Connect.closeConnection();
        } catch (SQLException e) {e.printStackTrace();}//todo if collides will be caught here

        return rows > 0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "bad Db writing");
    }

    public Response<Boolean> removeActivity(String username, LocalDateTime startAct) {
        Connect.createConnection();
        int rows = 0;
        String sql = "DELETE FROM \"WorkPlans\" WHERE username = ? AND date = ?";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(startAct));

            rows = preparedStatement.executeUpdate();

            Connect.closeConnection();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rows > 0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "bad Db writing");
    }
}
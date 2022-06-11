package Persistence;
import Communication.DTOs.ActivityDTO;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

@Repository
public class HolidaysQueries {

    private static class CreateSafeThreadSingleton {
        private static final HolidaysQueries INSTANCE = new HolidaysQueries();
    }

    public static HolidaysQueries getInstance() {
        return HolidaysQueries.CreateSafeThreadSingleton.INSTANCE;
    }

    public Response<Boolean> insertHolidaysDates(ArrayList<String[]> info){
        Connect.createConnection();
        int rows = 0;
        String sql = "INSERT INTO \"Holidays\" (title, date) VALUES (?, ?)";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            for (String[] entry : info) {
                preparedStatement.setString(1, entry[0]);
                preparedStatement.setTimestamp(2, Timestamp.valueOf(StringToDate(entry[1])));
                rows += preparedStatement.executeUpdate();
            }

            Connect.closeConnection();
        } catch (SQLException e) {e.printStackTrace();}

        return rows>0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "bad Db writing");

    }

    public ArrayList<String[]> getHolidaysDates (int year) {
        Connect.createConnection();
        String sql = "SELECT * FROM \"Holidays\" WHERE EXTRACT(YEAR FROM date) = ?";
        PreparedStatement statement;
        ArrayList<String[]> output = new ArrayList<>();
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setInt(1, year);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String title = result.getString("title");
                LocalDateTime date = result.getTimestamp("date").toInstant().atZone(TimeZone.getTimeZone("Asia/Jerusalem").toZoneId()).toLocalDateTime();
                String [] arr = {title, date.toString(), date.getYear()+""};
                output.add(arr);
            }

            Connect.closeConnection();

        } catch (SQLException e) {e.printStackTrace();}
        return output;
    }

    public boolean holidaysForYearExists (int year){
        Connect.createConnection();
        String sql = "SELECT exists (SELECT 1 FROM \"Holidays\" WHERE EXTRACT(YEAR FROM date) = ? LIMIT 1)";
        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setInt(1, year);
            ResultSet result = statement.executeQuery();
            if(result.next()) {
                boolean found = result.getBoolean(1);
                Connect.closeConnection();
                return found;
            }
            Connect.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public Response<List<Pair<LocalDateTime, ActivityDTO>>> getHolidaysAsActivity(int year, int month){
        Connect.createConnection();
        String sql = "SELECT * FROM \"Holidays\" WHERE EXTRACT(YEAR FROM date) = ?";
        PreparedStatement statement;
        List<Pair<LocalDateTime, ActivityDTO>> output = new Vector<>();
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setInt(1, year);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String title = result.getString("title");
                LocalDateTime date = result.getTimestamp("date").toInstant().atZone(TimeZone.getTimeZone("Asia/Jerusalem").toZoneId()).toLocalDateTime();
                if(date.getMonthValue()==month){
                    ActivityDTO activityDTO = new ActivityDTO("Holiday", 0, title, date.plusHours(16)); //to 22:00
                    Pair<LocalDateTime, ActivityDTO> pair = new Pair<>(date, activityDTO);
                    output.add(pair);
                }
            }
            Connect.closeConnection();
            return new Response<>(output, false, "successfully acquired holidays");
        } catch (SQLException e) {e.printStackTrace();}

        return new Response<>(null, true, "failed to acquire holidays");
    }

    public void clearHolidays(){
        Connect.createConnection();
        String sql = "TRUNCATE \"Holidays\"";

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

    private LocalDateTime StringToDate(String date){
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date.substring(0,10));
        return LocalDateTime.of(localDate, LocalTime.of(6,0));
    }

}

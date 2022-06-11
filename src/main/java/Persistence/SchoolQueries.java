package Persistence;

import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Persistence.DbDtos.SchoolDBDTO;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

@Repository
public class SchoolQueries {

    private static class CreateSafeThreadSingleton {
        private static final SchoolQueries INSTANCE = new SchoolQueries();
    }

    public static SchoolQueries getInstance() {
        return SchoolQueries.CreateSafeThreadSingleton.INSTANCE;
    }

    public Response<Boolean> insertSchool(SchoolDBDTO school){
        Connect.createConnection();
        int rows = 0;
        String sql = "INSERT INTO \"Schools\" (symbol, name, city, city_mail, address, school_address, principal," +
                " manager, supervisor, phone, mail, zipcode, education_stage, education_type, supervisor_type, spector," +
                " num_of_students ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, school.getSymbol());
            preparedStatement.setString(2, school.getName());
            preparedStatement.setString(3, school.getCity());
            preparedStatement.setString(4, school.getCity_mail());
            preparedStatement.setString(5, school.getAddress());
            preparedStatement.setString(6, school.getSchool_address());
            preparedStatement.setString(7, school.getPrincipal());
            preparedStatement.setString(8, school.getManager());
            preparedStatement.setString(9, school.getSupervisor());
            preparedStatement.setString(10, school.getPhone());
            preparedStatement.setString(11, school.getMail());
            preparedStatement.setInt(12, school.getZipcode());
            preparedStatement.setString(13, school.getEducation_stage());
            preparedStatement.setString(14, school.getEducation_type());
            preparedStatement.setString(15, school.getSupervisor_type());
            preparedStatement.setString(16, school.getSpector());
            preparedStatement.setInt(17, school.getNum_of_students());

            rows = preparedStatement.executeUpdate();
            Connect.closeConnection();
        } catch (SQLException e) {e.printStackTrace();}

        return rows>0 ? new Response<>(true, false, "") :
                new Response<>(false, true, "bad Db writing");

    }

    public Response<Boolean> removeSchool(String symbol){
        Connect.createConnection();
        String sql = "DELETE FROM \"Schools\" WHERE symbol = ?";
        try (PreparedStatement pstmt = Connect.conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.executeUpdate();
            Connect.closeConnection();
            return new Response<>(true, false, "removed school successfully");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());

            return new Response<>(false, true, "school removal failed");
        }
    }

    public Response<Boolean> updateSchool (String symbol, SchoolDBDTO school){
        Connect.createConnection();
        String sql = "UPDATE \"Schools\" SET symbol = ?, name = ?, city = ?, city_mail = ?," +
                "address = ?, school_address = ?, principal = ?, manager = ?, supervisor = ?, " +
                "phone = ?, mail = ?, zipcode = ?, education_stage = ?, education_type = ?, " +
                "supervisor_type = ?, spector = ?, num_of_students WHERE symbol = ?";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = Connect.conn.prepareStatement(sql);
            preparedStatement.setString(1, school.getSymbol());
            preparedStatement.setString(2, school.getName());
            preparedStatement.setString(3, school.getCity());
            preparedStatement.setString(4, school.getCity_mail());
            preparedStatement.setString(5, school.getAddress());
            preparedStatement.setString(6, school.getSchool_address());
            preparedStatement.setString(7, school.getPrincipal());
            preparedStatement.setString(8, school.getManager());
            preparedStatement.setString(9, school.getSupervisor());
            preparedStatement.setString(10, school.getPhone());
            preparedStatement.setString(11, school.getMail());
            preparedStatement.setInt(12, school.getZipcode());
            preparedStatement.setString(13, school.getEducation_stage());
            preparedStatement.setString(14, school.getEducation_type());
            preparedStatement.setString(15, school.getSupervisor_type());
            preparedStatement.setString(16, school.getSpector());
            preparedStatement.setInt(17, school.getNum_of_students());
            preparedStatement.setInt(18, Integer.getInteger(symbol));

            preparedStatement.executeUpdate();
            Connect.closeConnection();
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
            return new Response<>(false, true, "update school failed");
        }

        return new Response<>(true, false, "updated school successfully");
    }

    public boolean schoolSymbolExists(String symbol){
        Connect.createConnection();
        String sql = "SELECT exists (SELECT 1 FROM \"Schools\" WHERE symbol = ? LIMIT 1)";
        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setString(1, symbol);
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

    public boolean schoolNameExists (String name){
        Connect.createConnection();
        String sql = "SELECT exists (SELECT 1 FROM \"Schools\" WHERE name = ? LIMIT 1)";
        PreparedStatement statement;
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setString(1, name);
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

    public SchoolDBDTO getSchool(String symbol) {
        Connect.createConnection();
        String sql = "SELECT * FROM \"Schools\" WHERE symbol = ?";
        PreparedStatement statement;
        SchoolDBDTO schoolDBDTO = null;
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setString(1, symbol);
            ResultSet resultSchool = statement.executeQuery();

            if (resultSchool.next()) {
                String name = resultSchool.getString("name");
                String city = resultSchool.getString("city");
                String city_mail = resultSchool.getString("city_mail");
                String address = resultSchool.getString("address");
                String school_address = resultSchool.getString("school_address");
                String principal = resultSchool.getString("principal");
                String manager = resultSchool.getString("manager");
                String supervisor = resultSchool.getString("supervisor");
                String phone = resultSchool.getString("phone");
                String mail = resultSchool.getString("mail");
                int zipcode = resultSchool.getInt("zipcode");
                String education_stage = resultSchool.getString("education_stage");
                String education_type = resultSchool.getString("education_type");
                String supervisor_type = resultSchool.getString("supervisor_type");
                String spector = resultSchool.getString("spector");
                int num_of_students = resultSchool.getInt("num_of_students");
                schoolDBDTO = new SchoolDBDTO(symbol, name, city, city_mail, address, school_address, principal, manager, supervisor, phone, mail, zipcode, education_stage, education_type, supervisor_type, spector, num_of_students);
                Connect.closeConnection();
            } else {
                Connect.closeConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schoolDBDTO;
    }

    public Response<Pair<String, String>> getSchoolNameAndCity(String symbol) {
        Connect.createConnection();
        String sql = "SELECT name, city FROM \"Schools\" WHERE symbol = ?";
        PreparedStatement statement;
        Pair<String, String> nameAndCity = null;
        try {
            statement = Connect.conn.prepareStatement(sql);
            statement.setString(1, symbol);
            ResultSet resultSchool = statement.executeQuery();

            if (resultSchool.next()) {
                String name = resultSchool.getString("name");
                String city = resultSchool.getString("city");
                return new Response<>(new Pair<>(name, city), false, "acquired name and city");
            }
            Connect.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response<>(null, true, "failed to acquire name and city");
    }

    public Response<List<Pair<String, String>>> getSchoolNameAndSymbol(List<String> schools) {
        Connect.createConnection();
        String sql;
        PreparedStatement statement;
        List<Pair<String, String>> nameAndSymbol = new Vector<>();
        if(schools == null){
            sql = "SELECT name, symbol FROM \"Schools\"";
            try {
                statement = Connect.conn.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    nameAndSymbol.add(new Pair<>(resultSet.getString("name"), Integer.toString(resultSet.getInt("symbol"))));
                }
                Connect.closeConnection();
                return new Response<>(nameAndSymbol, false, "successfully acquired schools");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else{
            sql = "SELECT name FROM \"Schools\" WHERE symbol = ?";
            /*sql = "BEGIN;\n";
            for(String symbol: schools){
                sql+="SELECT name, symbol FROM \"Schools\" WHERE symbol = ?;\n";
            }
            sql+= "COMMIT;\n";*/
            try {
                statement = Connect.conn.prepareStatement(sql);
                /*int index = 1;
                for(String symbol : schools){
                    statement.setString(index++, symbol);
                }
                ResultSet resultSet = statement.executeQuery();
                while(resultSet.next()) {
                    nameAndSymbol.add(new Pair<>(resultSet.getString("name"), resultSet.getString("symbol")));
                }*/
                for(String school: schools){
                    statement.setString(1, school);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        nameAndSymbol.add(new Pair<>(resultSet.getString("name"), school));
                    }
                }
                Connect.closeConnection();
                return new Response<>(nameAndSymbol, false, "successfully acquired schools");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new Response<>(null, true, "failed to acquire info");
    }

    //for test purposes only
    public void deleteSchools(){
        Connect.createConnection();
        String sql = "TRUNCATE \"Schools\"";

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

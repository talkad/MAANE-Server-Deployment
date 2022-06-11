package Persistence;

import Domain.CommonClasses.Response;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExcelFormatter {

    private static class CreateSafeThreadSingleton {
        private static final ExcelFormatter INSTANCE = new ExcelFormatter();
    }

    public static ExcelFormatter getInstance() {
        return ExcelFormatter.CreateSafeThreadSingleton.INSTANCE;
    }

    private int getRowCount() throws IOException {
        String projDir = System.getProperty("user.dir");
        String exelPath = projDir + "\\Mosad.xlsx";
        XSSFWorkbook workbook = new XSSFWorkbook(exelPath);
        XSSFSheet sheet = workbook.getSheet("Mosad");
        workbook.close();
        return sheet.getPhysicalNumberOfRows();
    }

    public Response<Boolean> SchoolExcelToDb() {
        int rowCount;
        String projDir = System.getProperty("user.dir");
        String exelPath = projDir + "\\Mosad.xlsx";
        XSSFSheet sheet;
        XSSFWorkbook workbook;

        try {
            workbook = new XSSFWorkbook(exelPath);
            sheet = workbook.getSheet("Mosad");
            rowCount = getRowCount();
        } catch(IOException e) {
            return new Response<>(false, true, e.getMessage());
        }

        try {
            for (int i = 1; i < rowCount; i++) {
                String symbol = (int) sheet.getRow(i).getCell(0).getNumericCellValue() + "";
                String name = sheet.getRow(i).getCell(1).getStringCellValue();
                String city = sheet.getRow(i).getCell(2).getStringCellValue();
                String city_mail = sheet.getRow(i).getCell(3).getStringCellValue();
                String address = sheet.getRow(i).getCell(4).getStringCellValue();
                String school_address = sheet.getRow(i).getCell(5).getStringCellValue();
                String principal = sheet.getRow(i).getCell(6).getStringCellValue();
                String manager = sheet.getRow(i).getCell(7).getStringCellValue();
                String supervisor = sheet.getRow(i).getCell(8).getStringCellValue();
                String phone = sheet.getRow(i).getCell(9).getStringCellValue();
                String mail = sheet.getRow(i).getCell(10).getStringCellValue();
                int zipcode = (int) sheet.getRow(i).getCell(11).getNumericCellValue();
                String education_stage = sheet.getRow(i).getCell(12).getStringCellValue();
                String education_type = sheet.getRow(i).getCell(13).getStringCellValue();
                String supervisor_type = sheet.getRow(i).getCell(14).getStringCellValue();
                String spector = sheet.getRow(i).getCell(15).getStringCellValue();
                int num_of_students = (int) sheet.getRow(i).getCell(16).getNumericCellValue();
                insertToDb(symbol, name, city, city_mail, address, school_address, principal, manager, supervisor,
                        phone, mail, zipcode, education_stage, education_type, supervisor_type, spector, num_of_students);
            }
        } catch (SQLException e) {
            return new Response<>(false, true, e.getMessage());
        }

        try {
            workbook.close();
        } catch(IOException e) {
            return new Response<>(false, true, "Failed to close workbook");
        }

        return new Response<>(true, false, "Schools loaded successfully");
    }

    private void insertToDb (String symbol, String name, String city, String city_mail, String address,
                                   String school_address, String principal, String manager, String supervisor,
                                   String phone, String mail, int zipcode, String education_stage, String education_type,
                                   String supervisor_type, String spector, int num_of_students) throws SQLException {
        Connect.createConnection();
        String sql = "INSERT INTO \"Schools\" (symbol, name, city, city_mail, address, school_address, principal," +
                " manager, supervisor, phone, mail, zipcode, education_stage, education_type, supervisor_type, spector," +
                " num_of_students ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = Connect.conn.prepareStatement(sql);
        preparedStatement.setString(1, symbol);
        preparedStatement.setString(2, name);
        preparedStatement.setString(3, city);
        preparedStatement.setString(4, city_mail);
        preparedStatement.setString(5, address);
        preparedStatement.setString(6, school_address);
        preparedStatement.setString(7, principal);
        preparedStatement.setString(8, manager);
        preparedStatement.setString(9, supervisor);
        preparedStatement.setString(10, phone);
        preparedStatement.setString(11, mail);
        preparedStatement.setInt(12, zipcode);
        preparedStatement.setString(13, education_stage);
        preparedStatement.setString(14, education_type);
        preparedStatement.setString(15, supervisor_type);
        preparedStatement.setString(16, spector);
        preparedStatement.setInt(17, num_of_students);

        preparedStatement.executeUpdate();
        Connect.closeConnection();
    }

    /**
     * check whether the table is empty or not
     * @return positive response if the Schools table is empty, neg. otherwise.
     */
    public Response<Boolean> isEmpty() {

        String sql = "SELECT  count(1) WHERE EXISTS (SELECT * FROM  \"Schools\")";
        PreparedStatement statement;

        try {
            Connect.createConnection();
            statement = Connect.conn.prepareStatement(sql);

            ResultSet result = statement.executeQuery();
            if(result.next()) {
                if(result.getInt(1) == 1)
                    return new Response<>(false, false, "Schools table is not empty");
            }

            Connect.closeConnection();

        } catch (SQLException e) {
            return new Response<>(true, false, "Schools table empty");
        }

        return new Response<>(true, false, "Schools table empty");
    }

}

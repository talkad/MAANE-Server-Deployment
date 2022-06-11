package Communication.Service.Interfaces;

import Domain.CommonClasses.Response;

public interface MonthlyReportGenerator {

    Response<byte[]> generateMonthlyReport(String username, int year, int month);

}

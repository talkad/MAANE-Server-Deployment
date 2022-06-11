package Communication.Service;

import Communication.Service.Interfaces.MonthlyReportGenerator;
import Domain.CommonClasses.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class MonthlyReportGeneratorImpl implements MonthlyReportGenerator {


    private static class CreateSafeThreadSingleton {
        private static final MonthlyReportGeneratorImpl INSTANCE = new MonthlyReportGeneratorImpl();
    }

    public static MonthlyReportGeneratorImpl getInstance() {
        return CreateSafeThreadSingleton.INSTANCE;
    }


    @Override
    public Response<byte[]> generateMonthlyReport(String username, int year, int month) {
        Response<byte[]> res = Domain.MonthlyReport.MonthlyReportGenerator.getInstance().generateMonthlyReport(username, year, month);

        if(res.isFailure())
            log.error("{} failed to generate monthly report", username);
        else
            log.info("{} successfully generated monthly report", username);

        return res;
    }

}

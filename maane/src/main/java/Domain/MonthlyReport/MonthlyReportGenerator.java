package Domain.MonthlyReport;


import Domain.CommonClasses.Response;
import Domain.UsersManagment.APIs.DTOs.UserActivityInfoDTO;
import Domain.UsersManagment.APIs.DTOs.UserInfoDTO;
import Domain.UsersManagment.APIs.UserInfoRetriever;
import Domain.UsersManagment.APIs.UserInfoService;
import Domain.UsersManagment.UserController;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class MonthlyReportGenerator {

    private UserInfoService infoService;
    private MSWordWriterService writerService;

    private static class CreateSafeThreadSingleton {
        private static final MonthlyReportGenerator INSTANCE = new MonthlyReportGenerator();
    }

    public static MonthlyReportGenerator getInstance() {
        return MonthlyReportGenerator.CreateSafeThreadSingleton.INSTANCE;
    }

    public MonthlyReportGenerator() {
        this.infoService = new UserInfoRetriever();
        this.writerService = new MSWordWriterService();
    }

    /**
     * main function for generating the montly report.
     * receive data from specified API and build the doc accordingly
     * @param username the user wish to generate the document
     * @return response of binary document representation on success
     */
    public Response<byte[]> generateMonthlyReport(String username, int year, int month) {

        Response<Boolean> legalRes = infoService.canGenerateReport(username);
        LocalDateTime now = LocalDateTime.now();
        Response<UserInfoDTO> userInfo;
        Response<List<UserActivityInfoDTO>> activities;

        // check if the given user can generate a monthly report
        if(!legalRes.getResult())
            return new Response<>(null, true, legalRes.getErrMsg());

        // get data
        userInfo = infoService.getUserInfo(username);
        if(userInfo.isFailure())
            return new Response<>(null, true, userInfo.getErrMsg());

        activities = infoService.getUserActivities(username, year, month);
        if(activities.isFailure())
            return new Response<>(null, true, userInfo.getErrMsg());

        return generateReportDoc(userInfo.getResult(), activities.getResult(), now, year, month);

    }

    /**
     * the actual document builder
     * @param userInfo the user information for first table
     * @param activities the activities for the second table
     * @return response of binary document representation on success
     */
    private synchronized Response<byte[]> generateReportDoc(UserInfoDTO userInfo, List<UserActivityInfoDTO> activities, LocalDateTime date, int year, int month) {
        Response<File> reportRes;
        byte[] binaryFile = null;
        FileInputStream fileInputStream;
        File file;

        // update user city
        for(UserActivityInfoDTO activity: activities)
            activity.setUserCity(userInfo.getCity());

        // create the document
        reportRes = writerService.createDoc("maane\\src\\main\\resources\\monthlyReport.docx", userInfo, activities, date, year, month);
        file = reportRes.getResult();

        if(!reportRes.isFailure()) {
            binaryFile = new byte[(int)file.length()];

            try {
                fileInputStream = new FileInputStream(file);

                fileInputStream.read(binaryFile);
                fileInputStream.close();
            } catch(Exception e){
                return new Response<>(null, true, e.getMessage());
            }
        }

        // delete file from memory
        if(file != null) {
            if (file.delete())
                log.error("failed to delete monthly report file");
        }

        return new Response<>(binaryFile, reportRes.isFailure(), reportRes.getErrMsg());
    }

//    public static void main(String[] args) {
//        MonthlyReportGenerator gen = new MonthlyReportGenerator();
//
//        gen.generateReportDoc("tal", new UserInfoDTO("a", "b", "c", 3), new LinkedList<>(), LocalDateTime.now(), 2000, 5);
//    }

}

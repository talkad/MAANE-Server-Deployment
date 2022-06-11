package Communication.Resource;

import Communication.Service.Interfaces.MonthlyReportGenerator;
import Domain.CommonClasses.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    private final MonthlyReportGenerator service;
    private final SessionHandler sessionHandler;


    @GetMapping("/getMonthlyReport/year={year}&month={month}")
    public ResponseEntity<Response<byte[]>> getMonthlyReport(@RequestHeader(value = "Authorization") String token, @PathVariable("year") Integer year, @PathVariable("month") Integer month){
        return ResponseEntity.ok()
                .body(service.generateMonthlyReport(sessionHandler.getUsernameByToken(token).getResult(), year, month));
    }



}

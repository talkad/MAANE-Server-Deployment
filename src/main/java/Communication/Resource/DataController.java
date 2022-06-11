package Communication.Resource;

import Communication.Service.Interfaces.DataService;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Persistence.DbDtos.SchoolDBDTO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/data")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DataController {

    private final Gson gson;
    private final DataService service;
    private final SessionHandler sessionHandler;

    @PostMapping(value = "/assignCoordinator")
    public ResponseEntity<Response<Boolean>> assignCoordinator(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object> body){
        return ResponseEntity.ok()
                .body(service.assignCoordinator(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("workField"), (String)body.get("firstName"), (String)body.get("lastName"), (String)body.get("email"), (String)body.get("phoneNumber"), (String)body.get("school")));
    }

    @PostMapping(value = "/removeCoordinator")
    public ResponseEntity<Response<Boolean>> removeCoordinator(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object> body){
        return ResponseEntity.ok()
                .body(service.removeCoordinator(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("workField"), (String)body.get("school")));
    }

    @GetMapping("/getSchool/symbol={symbol}")
    public ResponseEntity<Response<SchoolDBDTO>> getSchool(@RequestHeader(value = "Authorization") String token, @PathVariable("symbol") String symbol){
        return ResponseEntity.ok()

                .body(service.getSchool(sessionHandler.getUsernameByToken(token).getResult(), symbol));
    }

    @GetMapping("/getUserSchools")
    public ResponseEntity<Response<List<Pair<String, String>>>> getUserSchools(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.getUserSchools(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @PostMapping(value = "/resetDB")
    public ResponseEntity<Response<Boolean>> resetDB(){
        return ResponseEntity.ok()
                .body(service.resetDB());
    }

    @PostMapping(value = "/removeCoordinatorTester")
    public ResponseEntity<Response<Boolean>> removeCoordinatorTester(@RequestBody Map<String, Object> body){
        return ResponseEntity.ok()
                .body(service.removeCoordinatorTester((String)body.get("school")));
    }

    @PostMapping(value = "/assignCoordinatorTester")
    public ResponseEntity<Response<Boolean>> assignCoordinatorTester(@RequestBody Map<String, Object> body){
        return ResponseEntity.ok()
                .body(service.assignCoordinatorTester((String)body.get("school")));
    }

//    @PostMapping(value = "/insertSchool")
//    public ResponseEntity<Response<Boolean>> insertSchool(@RequestBody SchoolDBDTO school){
//        return ResponseEntity.ok()
//                .body(service.insertSchool(school));
//    }
//
//    @PostMapping(value = "/removeSchool")
//    public ResponseEntity<Response<Boolean>> removeSchool(@RequestBody String symbol){
//        return ResponseEntity.ok()
//                .body(service.removeSchool(symbol));
//    }
//
//    @PostMapping(value = "/updateSchool")
//    public ResponseEntity<Response<Boolean>> updateSchool(@RequestBody Map<String, Object> body){
//        SchoolDBDTO schoolDBDTO = gson.fromJson((String)body.get("school"), SchoolDBDTO.class);;
//
//        return ResponseEntity.ok()
//                .body(service.updateSchool((String)body.get("symbol"), schoolDBDTO));
//    }

}


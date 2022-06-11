package Communication.Resource;

import Communication.DTOs.*;
import Communication.Security.KeyLoader;
import Communication.Service.AnnualScheduleGeneratorServiceImpl;
import Communication.Service.UserServiceImpl;
import Domain.CommonClasses.Response;
import Domain.UsersManagment.User;
import Persistence.DbDtos.UserDBDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final ObjectMapper objectMapper;
    private final Gson gson;
    private final UserServiceImpl service;
    private final AnnualScheduleGeneratorServiceImpl scheduleGeneratorService;
    private final SessionHandler sessionHandler;


    @GetMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){

            try{
                String refreshToken = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256(KeyLoader.getInstance().getEncryptionKey("auth_key"));
                JWTVerifier verifier = JWT.require(algorithm).build();

                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                String username = decodedJWT.getSubject();

                Response<User> user = Domain.UsersManagment.UserController.getInstance().getUserRes(username);
                List<String> authorities = new LinkedList<>();
                authorities.add(user.getResult().getState().getStateEnum().getState());

                String accessToken = JWT.create()
                        .withSubject(username)
                        .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles", authorities)
                        .sign(algorithm);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", accessToken);
                tokens.put("refresh_token", refreshToken);

                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);

            }catch(Exception e){

                response.setHeader("error", e.getMessage());
                response.setStatus(FORBIDDEN.value());

                Map<String, String> error = new HashMap<>();
                error.put("error_message",  e.getMessage());

                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        }
        else {
            throw new RuntimeException("Refresh token is missing");
        }

    }

    @GetMapping("/test")
    public ResponseEntity<String> testSSL(@RequestHeader(value = "Authorization") String token){
        System.out.println("the user is belong to " + sessionHandler.getUsernameByToken(token));
        return ResponseEntity.ok().body("hello world");
    }

    @GetMapping("/test2")
    public ResponseEntity<User> test2(){
        return ResponseEntity.ok().body(Domain.UsersManagment.UserController.getInstance().getUser("admin"));
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<Response<String>> logout(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.logout(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @PostMapping(value = "/registerUser")
    public ResponseEntity<Response<String>> registerUser(@RequestHeader(value = "Authorization") String token, @RequestBody UserDTO user) {
        return ResponseEntity.ok()
                .body(service.registerUser(sessionHandler.getUsernameByToken(token).getResult(), user));
    }

    @PostMapping(value = "/registerUserBySystemManager")
    public ResponseEntity<Response<String>> registerUserBySystemManager(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body) {
        String user = "";

        try {
            user = objectMapper.writeValueAsString(body.get("user"));
        }catch(Exception e){
            System.out.println("This exception shouldn't occur");
        }

        return ResponseEntity.ok()
                .body(service.registerUserBySystemManager(sessionHandler.getUsernameByToken(token).getResult(), gson.fromJson(user, UserDTO.class), (String)body.get("optionalSupervisor")));
    }

    @PostMapping(value = "/removeUser")
    public ResponseEntity<Response<Boolean>> removeUser(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.removeUser(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("userToRemove")));
    }

    @GetMapping(value = "/viewWorkPlan/year={year}&month={month}")
    public ResponseEntity<Response<WorkPlanDTO>> viewWorkPlan(@RequestHeader(value = "Authorization") String token, @PathVariable("year") Integer year, @PathVariable("month") Integer month){
        return ResponseEntity.ok()
                .body(service.viewWorkPlan(sessionHandler.getUsernameByToken(token).getResult(), year, month));
    }

    @GetMapping(value = "/viewInstructorWorkPlan/instructor={instructor}&year={year}&month={month}")
    public ResponseEntity<Response<WorkPlanDTO>> viewInstructorWorkPlan(@RequestHeader(value = "Authorization") String token, @PathVariable("instructor") String instructor, @PathVariable("year") Integer year, @PathVariable("month") Integer month){
        return ResponseEntity.ok()
                .body(service.viewInstructorWorkPlan(sessionHandler.getUsernameByToken(token).getResult(), instructor, year, month));
    }

    @PostMapping(value = "/authenticatePassword")
    public ResponseEntity<Response<Boolean>> verifyUser(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, String>  body){
        return ResponseEntity.ok()
                .body(service.verifyUser(sessionHandler.getUsernameByToken(token).getResult(), body.get("password")));
    }

    @GetMapping(value = "/getAppointedUsers")
    public ResponseEntity<Response<List<UserDTO>>> getAppointedUsers(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.getAppointedUsers(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @RequestMapping(value = "/generateSchedule", method = RequestMethod.POST)
    public ResponseEntity<Response<Boolean>> generateSchedule(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(scheduleGeneratorService.generateSchedule(sessionHandler.getUsernameByToken(token).getResult(), (String) body.get("surveyID")));
    }

    @PostMapping(value = "/addGoal")
    public ResponseEntity<Response<Boolean>> addGoal(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        String goal = "";

        try {
            goal = objectMapper.writeValueAsString(body.get("goalDTO"));
        }catch(Exception e){
            System.out.println("This exception shouldn't occur");
        }

        return ResponseEntity.ok()
                .body(service.addGoal(sessionHandler.getUsernameByToken(token).getResult(), gson.fromJson(goal, GoalDTO.class), (Integer) body.get("year")));
    }

    @PostMapping(value = "/removeGoal")
    public ResponseEntity<Response<Boolean>> removeGoal(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.removeGoal(sessionHandler.getUsernameByToken(token).getResult(), (Integer) body.get("year"), (Integer)body.get("goalId")));
    }

    @GetMapping(value = "/getGoals/year={year}")
    public ResponseEntity<Response<List<GoalDTO>>> getGoals(@RequestHeader(value = "Authorization") String token, @PathVariable("year") Integer year){
        return ResponseEntity.ok()
                .body(service.getGoals(sessionHandler.getUsernameByToken(token).getResult(), year));
    }

   @PostMapping(value = "/updateInfo")
    public ResponseEntity<Response<Boolean>> updateInfo(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.updateInfo(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("firstName"), (String)body.get("lastName"), (String)body.get("email"), (String)body.get("phoneNumber"), (String)body.get("city")));
    }

    @PostMapping(value = "/changePasswordToUser")
    public ResponseEntity<Response<Boolean>> changePasswordToUser(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.changePasswordToUser(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("userToChangePassword"), (String)body.get("newPassword"), (String)body.get("confirmPassword")));
    }

    @PostMapping(value = "/changePassword")
    public ResponseEntity<Response<Boolean>> changePassword(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.changePassword(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("currPassword"), (String)body.get("newPassword"), (String)body.get("confirmPassword")));
    }

    @GetMapping(value = "/getAllUsers")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.getAllUsers(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @PostMapping(value = "/assignSchoolToUser")
    public ResponseEntity<Response<Boolean>> assignSchoolToUser(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){//todo aviad
        return ResponseEntity.ok()
                .body(service.assignSchoolToUser(sessionHandler.getUsernameByToken(token).getResult(), (String) body.get("affectedUser"), (String) body.get("school")));
    }

    @PostMapping(value = "/removeSchoolFromUser")
    public ResponseEntity<Response<Boolean>> removeSchoolFromUser(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){//todo aviad
        return ResponseEntity.ok()
                .body(service.removeSchoolFromUser(sessionHandler.getUsernameByToken(token).getResult(), (String) body.get("affectedUser"), (String) body.get("school")));
    }

    @GetMapping(value = "/getUserInfo")
    public ResponseEntity<Response<UserDTO>> getUserInfo(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.getUserInfo(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @PostMapping(value = "/sendCoordinatorEmails")
    public ResponseEntity<Response<Boolean>> sendCoordinatorEmails(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body) {
        return ResponseEntity.ok()
                .body(service.sendCoordinatorEmails(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("surveyLink"), (String)body.get("surveyToken")));
    }

    @PostMapping(value = "/transferSupervision")
    public ResponseEntity<Response<Boolean>> transferSupervision(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.transferSupervision(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("currSupervisor"), (String)body.get("newSupervisor"), (String)body.get("password"), (String)body.get("firstName"), (String)body.get("lastName"), (String)body.get("email"), (String)body.get("phoneNumber"), (String)body.get("city")));
    }

    @GetMapping(value = "/getSupervisors")
    public ResponseEntity<Response<List<UserDTO>>> getSupervisors(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.getSupervisors(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @PostMapping(value = "/transferSupervisionToExistingUser")
    public ResponseEntity<Response<Boolean>> transferSupervisionToExistingUser(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.transferSupervisionToExistingUser(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("currSupervisor"), (String)body.get("newSupervisor")));
    }

    @GetMapping(value="/getCoordinator/workfield={workfield}&symbol={symbol}")
    public ResponseEntity<Response<UserDBDTO>> getCoordinator(@RequestHeader(value = "Authorization") String token, @PathVariable("workfield") String workfield, @PathVariable("symbol") String symbol){
        return ResponseEntity.ok()
                .body(service.getCoordinator(sessionHandler.getUsernameByToken(token).getResult(), workfield, symbol));
    }

    @GetMapping(value="/allWorkFields")
    public ResponseEntity<Response<List<String>>> allWorkFields(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.allWorkFields(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @PostMapping(value = "/setWorkingTime")
    public ResponseEntity<Response<Boolean>> setWorkingTime(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.setWorkingTime(sessionHandler.getUsernameByToken(token).getResult(), (int)body.get("workDay"), (String) body.get("act1Start"), (String) body.get("act1End"), (String) body.get("act2Start"), (String) body.get("act2End")));
    }

    @GetMapping(value="/getWorkHours")
    public ResponseEntity<Response<UserDBDTO>> getWorkHours(@RequestHeader(value = "Authorization") String token){
        return ResponseEntity.ok()
                .body(service.getWorkHours(sessionHandler.getUsernameByToken(token).getResult()));
    }

    @PostMapping(value = "/editActivity")
    public ResponseEntity<Response<Boolean>> editActivity(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object> body){
        return ResponseEntity.ok()
                .body(service.editActivity(sessionHandler.getUsernameByToken(token).getResult(), (String)body.get("currActStart"), (String) body.get("newActStart"), (String) body.get("newActEnd")));
    }

    @PostMapping(value = "/addActivity")
    public ResponseEntity<Response<Boolean>> addActivity(@RequestHeader(value = "Authorization") String token, @RequestBody ActivityManageDTO activityManageDTO){
        return ResponseEntity.ok()
                .body(service.addActivity(sessionHandler.getUsernameByToken(token).getResult(), activityManageDTO.getStartActivity(), activityManageDTO.getSchoolId(), activityManageDTO.getGoalId(), activityManageDTO.getTitle(), activityManageDTO.getEndActivity()));
    }

    @PostMapping(value = "/removeActivity")
    public ResponseEntity<Response<Boolean>> removeActivity(@RequestHeader(value = "Authorization") String token, @RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.removeActivity(sessionHandler.getUsernameByToken(token).getResult(), (String) body.get("startAct")));
    }

    @PostMapping(value = "/changePasswordTester") //todo aviad
    public ResponseEntity<Response<Boolean>> changePasswordTester(@RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.changePasswordTester((String)body.get("currUser"), (String)body.get("newPassword")));
    }

    @PostMapping(value = "/removeUserTester")
    public ResponseEntity<Response<Boolean>> removeUserTester(@RequestBody Map<String, Object>  body){
        return ResponseEntity.ok()
                .body(service.removeUserTester((String)body.get("currUser"), (String)body.get("userToRemove")));
    }

}
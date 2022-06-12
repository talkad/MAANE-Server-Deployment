package Domain.UsersManagment;

import Communication.DTOs.ActivityDTO;
import Communication.DTOs.GoalDTO;
import Communication.DTOs.UserDTO;
import Communication.DTOs.WorkPlanDTO;
import Communication.Initializer.ServerContextInitializer;
import Communication.Security.KeyLoader;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.EmailManagement.EmailController;
import Domain.UsersManagment.APIs.DTOs.UserActivityInfoDTO;
import Domain.UsersManagment.APIs.DTOs.UserInfoDTO;
import Domain.WorkPlan.GoalsManagement;
import Domain.WorkPlan.HolidaysHandler;
import Persistence.DbDtos.UserDBDTO;
import Persistence.SurveyDAO;
import Persistence.UserQueries;
import Persistence.WorkPlanQueries;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserController {
    private Map<String, User> connectedUsers;
    private PasswordEncoder passwordEncoder;
    private GoalsManagement goalsManagement;
    private EmailController emailController;
    private UserQueries userDAO;
    private WorkPlanQueries workPlanDAO;
    private final SecureRandom secureRandom;
    private final Base64.Encoder base64Encoder;


    private UserController() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.connectedUsers = new ConcurrentHashMap<>();
        this.goalsManagement = GoalsManagement.getInstance();
        this.emailController = EmailController.getInstance();
        this.userDAO = UserQueries.getInstance();
        this.workPlanDAO = WorkPlanQueries.getInstance();
        secureRandom = new SecureRandom();
        base64Encoder = Base64.getUrlEncoder();
        if(!userDAO.userExists("admin")){
            adminBoot("admin", KeyLoader.getInstance().getAdminPassword());
        }
    }

    private static class CreateSafeThreadSingleton {
        private static final UserController INSTANCE = new UserController();
    }

    public static UserController getInstance() {
        return CreateSafeThreadSingleton.INSTANCE;
    }

    public Map<String, User> getConnectedUsers() {
        return this.connectedUsers;
    }

    /**
     * login user into system
     * @param username the original username
     * @return username
     */
    public Response<String> login(String username){
        Response<UserDBDTO> userRes = userDAO.getFullUser(username);
        if(!userRes.isFailure()){
            User user = new User(userRes.getResult());
            connectedUsers.put(username, user);

            return new Response<>(username, false, "successfully Logged in");
        }
        else{
            System.out.println(username + " failed to login successfully");
            return new Response<>(null, true, "failed to login");
        }
    }

    /**
     * logout a user from the system
     * @param name user to be logged out username
     * @return a response of the successfulness of the action containing @name upon success
     */
    public Response<String> logout(String name) {
        Response<String> response;
        if(connectedUsers.containsKey(name)) {
            if (!connectedUsers.get(name).logout().isFailure()) {
                connectedUsers.remove(name);
                response = new Response<>(name, false, "successfully  logged out");
            }
            else {
                response = new Response<>(name, true, "User not permitted to logout");
            }
            return response;
        }
        return new Response<>(null, true, "User not connected");
    }

    /**
     * allow user to register another user (if user is supervisor)
     * @param currUser the user that is trying to register @userToRegister
     * @param userToRegister the user to be registered
     * @param password its future password
     * @param userStateEnum the role of the registered user
     * @param firstName first name
     * @param lastName last name
     * @param email email address
     * @param phoneNumber phone number
     * @param city city
     * @return User object of the new user upon success
     */
    public Response<String> registerUser(String currUser, String userToRegister, String password, UserStateEnum userStateEnum, String firstName, String lastName, String email, String phoneNumber, String city){
        if(email.length() != 0 && !isValidEmailAddress(email))
            return new Response<>("", true, "invalid email address");

        if(phoneNumber.length() != 0 && !isValidPhoneNumber(phoneNumber))
            return new Response<>("", true, "invalid phone number");

        if(!ServerContextInitializer.getInstance().isTestMode() && !isValidPassword(password))
            return new Response<>("", true, "The password isn't strong enough");

        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if (!userDAO.userExists(userToRegister)){
                Response<User> result = user.registerUser(userToRegister, userStateEnum, firstName, lastName, email, phoneNumber, city);
                if (!result.isFailure()) {
                    userDAO.insertUser(new UserDBDTO(result.getResult(), passwordEncoder.encode(password)));
                    userDAO.addAppointment(currUser, userToRegister);
                    return new Response<>(result.getResult().getUsername(), false, "Registration occurred");
                }
                return new Response<>(null, result.isFailure(), result.getErrMsg());
            }
            else {
                return new Response<>(null, true, "username already exists"); // null may be a problem
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * validate email address at user registration
     * @param email the email address
     * @return true if the email address is valid, false otherwise.
     */
    private boolean isValidEmailAddress(String email) {
        boolean result = true;

        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }

        return result;
    }

    /**
     * validate phone number at user registration
     * @param phoneNumber the phone number
     * @return true if the phone number is valid, false otherwise.
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber israeliNumberProto;

        try {
             israeliNumberProto = phoneUtil.parse(phoneNumber, "IL");
        } catch (NumberParseException e) {
            return false;
        }

        if(israeliNumberProto == null)
            return false;

        return phoneUtil.isValidNumber(israeliNumberProto);
    }

    /**
     * validate the password's strength must be at least 8 chars long and contain at least one letter and one number
     * @param password the password
     * @return true if the password is valid, false otherwise.
     */
    private boolean isValidPassword(String password) {
//        return password.length() >= 8 && password.matches("([A-Za-z]+[0-9]|[0-9]+[A-Za-z])[A-Za-z0-9]*");
        return password.length() >= 8 && password.matches("(.*)[a-zA-Z](.*)") && password.matches("(.*)[0-9](.*)");
    }

    /**
     * register a user in the system by the admin
     * @param currUser admin's username
     * @param userToRegister the new user's username
     * @param password the new user's password
     * @param userStateEnum the new user's state
     * @param optionalSupervisor the new user's assigned supervisor given @userStateEnum isn't SUPERVISOR
     * @param workField the new user's work field
     * @param firstName the new user's first name
     * @param lastName the new user's last name
     * @param email the new user's email
     * @param phoneNumber the new user's phone number
     * @param city the new user's city
     * @return a response of the successfulness of the action containing @userToRegister upon success
     */
    public Response<String> registerUserBySystemManager(String currUser, String userToRegister, String password, UserStateEnum userStateEnum, String optionalSupervisor, String workField, String firstName, String lastName, String email, String phoneNumber, String city){
        if(!ServerContextInitializer.getInstance().isTestMode() && !isValidPassword(password))
            return new Response<>("", true, "The password isn't strong enough");

        if(email.length() != 0 && !isValidEmailAddress(email))
            return new Response<>("", true, "invalid email address");

        if(phoneNumber.length() != 0 && !isValidPhoneNumber(phoneNumber))
            return new Response<>("", true, "invalid phone number");

        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if (!userDAO.userExists(userToRegister)){
                if(userStateEnum == UserStateEnum.SUPERVISOR) {
                    if (onlyOneSupervisorPerWorkField(user, workField)) {
                        Response<User> result = user.registerSupervisor(userToRegister, userStateEnum, workField, firstName, lastName, email, phoneNumber, city);
                        if (!result.isFailure()) {
                            userDAO.insertUser(new UserDBDTO(result.getResult(), passwordEncoder.encode(password)));
                            userDAO.addAppointment(currUser, userToRegister);
                            return new Response<>(result.getResult().getUsername(), false, "Registration occurred");
                        }
                        return new Response<>(null, result.isFailure(), result.getErrMsg());
                    }
                    else{
                        return new Response<>(null, true, "a supervisor was already appointed to the work field");
                    }
                }
                else {
                    if(user.appointments.contains(optionalSupervisor)) {
                        Response<UserDBDTO> supervisorRes = userDAO.getFullUser(optionalSupervisor);
                        if(!supervisorRes.isFailure()){
                            User supervisor = new User(supervisorRes.getResult());
                        if (supervisor.isSupervisor().getResult()) {
                            Response<User> result = user.registerUserBySystemManager(userToRegister, userStateEnum, supervisor.getWorkField(), firstName, lastName, email, phoneNumber, city);
                            if (!result.isFailure()) {
                                Response<Boolean> appointmentRes = supervisor.addAppointment(userToRegister);
                                if (appointmentRes.getResult()) {
                                    userDAO.insertUser(new UserDBDTO(result.getResult(), passwordEncoder.encode(password)));
                                    userDAO.addAppointment(supervisor.getUsername(), userToRegister);
                                    return new Response<>(result.getResult().getUsername(), false, "Registration occurred");
                                } else {
                                    return new Response<>(null, true, appointmentRes.getErrMsg());
                                }
                            }
                            return new Response<>(null, result.isFailure(), result.getErrMsg());
                        } else {
                            return new Response<>(null, true, "optional supervisor isn't a supervisor");
                        }
                    }
                        else{
                            return new Response<>(null, true, supervisorRes.getErrMsg());
                        }
                    }
                    else{
                        return new Response<>(null, true, "optional supervisor doesn't exist");
                    }
                }
            }
            else {
                return new Response<>(null, true, "username already exists"); // null may be a problem
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    private boolean onlyOneSupervisorPerWorkField(User user, String workField) {
        for (String appointee: user.getAppointments()) {
            Response<UserDBDTO> uRes = userDAO.getFullUser(appointee);
            if(!uRes.isFailure()){
                User u = new User(uRes.getResult());
                if(u.getWorkField().equals(workField)){
                    return false;
                }
            }
            else{
                //todo some error related to bad db reading
            }
        }
        return true;
    }

    /**
     * view all supervisors in the system by the admin
     * @param currUser the admin's username
     * @return a list of objects representing all the supervisors in the system wrapped in a response
     */
    public Response<List<UserDTO>> getSupervisors(String currUser){
        UserDBDTO userDB;
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if(user.isSystemManager().getResult()){
                Response<List<String>> appointeesRes = user.getAppointees();
                if (!appointeesRes.isFailure()) {
                    List<UserDTO> supervisorsDTOs = new Vector<>();
                    for (String username : appointeesRes.getResult()) {
                        userDB = userDAO.getFullUser(username).getResult();

                        supervisorsDTOs.add(new UserDTO(userDB));
                    }
                    return new Response<>(supervisorsDTOs, false, "");
                }
                else {
                    return new Response<>(null, appointeesRes.isFailure(), appointeesRes.getErrMsg());
                }
            }
            else{
                return new Response<>(null, true, "user is not allowed to view supervisors");
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * update the user's details
     * @param currUser the user's username
     * @param firstName the user's new first name
     * @param lastName the user's new last name
     * @param email the user's new email
     * @param phoneNumber the user's new phone number
     * @param city the user's new city
     * @return successful response upon success. failure otherwise
     */
    public Response<Boolean> updateInfo(String currUser, String firstName, String lastName, String email, String phoneNumber, String city){

        if(email.length() != 0 && !isValidEmailAddress(email))
            return new Response<>(false, true, "invalid email address");

        if(phoneNumber.length() != 0 && !isValidPhoneNumber(phoneNumber))
            return new Response<>(false, true, "invalid phone number");

        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<User> response = user.updateInfo(firstName, lastName, email, phoneNumber, city);
            if(!response.isFailure()){
                UserDBDTO userDBDTO = new UserDBDTO();
                userDBDTO.setUsername(currUser);
                userDBDTO.setFirstName(firstName);
                userDBDTO.setLastName(lastName);
                userDBDTO.setEmail(email);
                userDBDTO.setPhoneNumber(phoneNumber);
                userDBDTO.setCity(city);

                return userDAO.updateUserInfo(userDBDTO);
            }
            else {
                return new Response<>(false, true, response.getErrMsg());
            }
        }
        else{
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * remove user from the system
     * @param currUser the user that trying to remove another user
     * @param userToRemove the user to be removed
     * @return successful response upon success. failure otherwise
     */
    public Response<Boolean> removeUser(String currUser, String userToRemove) {
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<Boolean> response = user.removeUser(userToRemove);//todo verify connected user is dao updated
            if(!response.isFailure()){
                if(userDAO.userExists(userToRemove)){
                    if(response.getResult()){
                        findSupervisorAndRemoveAppointment(user, userToRemove);
                    }
                    else{
                        userDAO.removeAppointment(currUser, userToRemove);
                    }
                    response = userDAO.removeUser(userToRemove);
                    if(!response.isFailure()){
                        connectedUsers.remove(userToRemove);
                    }
                    return response;
                }
                else{
                    return new Response<>(null, true, "User is not in the system");
                }
            }
            return response;
        }
        else{
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * find and remove the @userToRemove from the supervisor that appointed him appointments
     * @param user the supervisor that appointed the user @userToRemove
     * @param userToRemove the user to be removed username
     */
    private void findSupervisorAndRemoveAppointment(User user, String userToRemove) {//todo maybe return bool and check in removeUser
        Response<List<String>> appointeesRes = user.getAppointees();
        String userToRemoveWorkField = userDAO.getFullUser(userToRemove).getResult().getWorkField();
        if(!appointeesRes.isFailure()){
            for (String appointee: appointeesRes.getResult()) {
                if(userDAO.userExists(appointee)){
                    User u = new User(userDAO.getFullUser(appointee).getResult());
                    if(u.getWorkField().equals(userToRemoveWorkField)){
                        u.removeAppointment(userToRemove);
                        userDAO.removeAppointment(u.getUsername(), userToRemove);
                        if(connectedUsers.containsKey(u.getUsername())){
                            connectedUsers.get(u.getUsername()).removeAppointment(userToRemove);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
        successful response on success. failure otherwise.
     *///todo like this or otherwise? ^

    /**
     * assign schools to a user
     * @param currUser the user's username
     * @param userToAssign the user to be assigned schools username
     * @param school the school to be assigned to @userToAssign
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> assignSchoolToUser(String currUser, String userToAssign, String school){
        Response<Boolean> response;
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if(userDAO.userExists(userToAssign)) {
                response = user.assignSchoolsToUser(userToAssign);
                if(!response.isFailure()){
                    User userToAssignSchools = new User(userDAO.getFullUser(userToAssign).getResult());
                    Response<Boolean> hasSchoolRes = userToAssignSchools.canAddSchool(school);
                    if(!hasSchoolRes.isFailure()) {
                        userDAO.assignSchoolToUser(userToAssign, school);
                        if (connectedUsers.containsKey(userToAssign)) {
                            userToAssignSchools = connectedUsers.get(userToAssign);
                            userToAssignSchools.addSchool(school);
                        }
                    }
                    else {
                        return hasSchoolRes;
                    }
                }
                return response;
            }
            else{
                return new Response<>(false, true, "User is not in the system");
            }
        }
        else{
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * remove schools assigned to the user
     * @param currUser user's username
     * @param userToRemoveSchoolsName the user to have his schools removed username
     * @param school a the school symbol to be removed from the user @userToRemoveSchoolsName
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> removeSchoolFromUser(String currUser, String userToRemoveSchoolsName, String school){
        Response<Boolean> response;
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if(userDAO.userExists(userToRemoveSchoolsName)) {
                response = user.allowedToRemoveSchool(userToRemoveSchoolsName);
                if(!response.isFailure()){
                   User userToRemoveSchools = new User(userDAO.getFullUser(userToRemoveSchoolsName).getResult());
                   Response<Boolean> removeSchoolRes = userToRemoveSchools.removeSchool(school);
                   if(!removeSchoolRes.isFailure()){
                       userDAO.removeSchoolsFromUser(userToRemoveSchoolsName, school);
                       if(connectedUsers.containsKey(userToRemoveSchoolsName)){
                           userToRemoveSchools = connectedUsers.get(userToRemoveSchoolsName);
                           userToRemoveSchools.schools.remove(school);
                       }
                   }
                   else{
                       return removeSchoolRes;
                   }
                }
                return response;
            }
            else{
                return new Response<>(false, true, "User is not in the system");
            }
        }
        else{
            return new Response<>(null, true, "User not connected");
        }
    }

    public Response<List<String>> getSchools(String currUser){
        User user = new User(userDAO.getFullUser(currUser).getResult());
        return new Response<>(user.getSchools(), false, "");
    }

    public Response<List<String>> getUserSchools(String currUser){
        User user = new User(userDAO.getFullUser(currUser).getResult());
        return user.getUserSchools();
    }

    public Response<String> hasSchool(String username, String symbol) {
        return new User(userDAO.getFullUser(username).getResult()).hasSchool(symbol);
    }

    /**
     * get the instructor's usernames appointed by the supervisor @currUser
     * @param currUser supervisor's username
     * @return a list of all the instructors usernames appointed by @currUser wrapped in a response
     */
    public Response<List<String>> getAppointedInstructors(String currUser){
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<List<String>> appointeesRes = user.getAppointees();
            if(!appointeesRes.isFailure()){
                List<String> instructors = new Vector<>();
                for (String appointee: appointeesRes.getResult()) {
                    User u = new User(userDAO.getFullUser(appointee).getResult());
                    if(u.isInstructor()){
                        instructors.add(u.username);
                    }
                }
                return new Response<>(instructors, false, "");
            }
            else {
                return appointeesRes;
            }
        }
        else{
            return new Response<>(null, true, "User is not connected");
        }
    }

    /**
     * get all the users appointed by @currUser
     * @param currUser the user's username
     * @return a list of all the users appointed by @currUser wrapped in a response
     */
    public Response<List<UserDTO>> getAppointedUsers(String currUser){
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<List<String>> appointeesRes = user.getAppointees();
            if (!appointeesRes.isFailure()) {
                List<UserDTO> appointeesDTOs = new Vector<>();
                for (String appointee : appointeesRes.getResult()) {
                    appointeesDTOs.add(createUserDTOS(appointee));
                }
                return new Response<>(appointeesDTOs, false, "");
            }
            else {
                return new Response<>(null, appointeesRes.isFailure(), appointeesRes.getErrMsg());
            }
        }
        else{
            return new Response<>(null, true, "User is not connected");
        }
    }

    /**
     * given a username generate the appropriate DTO
     * @param username user's username
     * @return the DTO object representing the user with the username @username
     */
    private UserDTO createUserDTOS(String username){
        UserDTO userDTO = new UserDTO();
        User user = new User(userDAO.getFullUser(username).getResult());
        userDTO.setUsername(username);
        userDTO.setWorkField(user.getWorkField());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setUserStateEnum(user.getState().getStateEnum());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setCity(user.getCity());
        userDTO.setSchools(user.getSchools());
        return userDTO;
    }

    /**
     * get all the users in the system
     * @param currUser admin's username
     * @return a list of all the users in the system wrapped in a response
     */
    public Response<List<UserDTO>> getAllUsers(String currUser){
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<Boolean> viewAllUsersRes = user.viewAllUsers();
            if (viewAllUsersRes.getResult()) {
                List<UserDTO> users = new Vector<>();
                for (String username : userDAO.getUsers()) {//todo check not null maybe
                    users.add(createUserDTOS(username));
                }
                return new Response<>(users, false, "");
            }
            else {
                return new Response<>(null, true, viewAllUsersRes.getErrMsg());
            }
        }
        else{
            return new Response<>(null, true, "User is not connected");
        }
    }

    /**
     * change password to another user (given that @currUser is an admin or a supervisor that appointed @userToChangePassword
     * @param currUser the user wishing to change password to @userToChangePassword username
     * @param userToChangePassword username of the user to have his password changed
     * @param newPassword the user - @userToChangePassword new password
     * @param confirmPassword the user - @userToChangePassword new password again
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> changePasswordToUser(String currUser, String userToChangePassword, String newPassword, String confirmPassword){
        if(!ServerContextInitializer.getInstance().isTestMode() && !isValidPassword(newPassword))
            return new Response<>(false, true, "The password isn't strong enough");

        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if(newPassword.equals(confirmPassword)) {
                if (userDAO.userExists(userToChangePassword)) {
                    Response<Boolean> res = user.changePasswordToUser(userToChangePassword);
                    if(res.getResult()){
                        userDAO.updateUserPassword(userToChangePassword, passwordEncoder.encode(newPassword));
                    }
                    return res;
                }
                else {
                    return new Response<>(false, true, "cannot change a password to a user not in the system");
                }
            }
            else {
                return new Response<>(false, true, "new password does not match the confirmed password");
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * allows to change your own password
     * @param currUser the user's username
     * @param currPassword the user's current password
     * @param newPassword the user's new password
     * @param confirmPassword the user's new password again password
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> changePassword(String currUser, String currPassword, String newPassword, String confirmPassword){
        if(!isValidPassword(newPassword) && !ServerContextInitializer.getInstance().isTestMode())
            return new Response<>(false, true, "The password isn't strong enough");

        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if (passwordEncoder.matches(currPassword, userDAO.getPassword(currUser).getResult()))
            {
                if (newPassword.equals(confirmPassword)) {
                    Response<Boolean> res = user.changePassword();
                    if (res.getResult()) {
                        userDAO.updateUserPassword(currUser, passwordEncoder.encode(newPassword));
                    }
                    return res;
                } else {
                    return new Response<>(false, true, "new password does not match the confirmed password");
                }
            }
            else {
                return new Response<>(false, true, "current password is incorrect");
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * verifies that the provided password matches the user
     * @param currUser username
     * @param password password
     * @return a response whether the user's password is correct
     */
    public Response<Boolean> verifyUser(String currUser, String password){
        if(connectedUsers.containsKey(currUser)) {
            boolean verify = passwordEncoder.matches(password, userDAO.getPassword(currUser).getResult());
            return new Response<>(verify, !verify, "");
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    public void adminBoot(String username, String password) {
        User user = new User(username, UserStateEnum.SYSTEM_MANAGER);
        userDAO.insertUser(new UserDBDTO(user, passwordEncoder.encode(password)));
    }

    /**
     * allows to view the user's details
     * @param currUser the username of the user requesting the info
     * @return an object representation of the user wrapped in a response
     */
    public Response<UserDTO> getUserInfo(String currUser){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.getInfo();
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    public Response<List<String>> allWorkFields(String currUser){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<List<String>> response = user.getAllWorkFields();
            if(!response.isFailure()){
                return userDAO.getAllWorkFields(response.getResult());
            }
            else{
                return new Response<>(null, true, response.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * transfer supervision to a new user
     * @param currUser the admin's username
     * @param currSupervisor the current supervisor to be replaced
     * @param newSupervisor the new supervisor's username
     * @param password the new supervisor's password
     * @param firstName the new supervisor's first name
     * @param lastName the new supervisor's last name
     * @param email the new supervisor's email
     * @param phoneNumber the new supervisor's phone number
     * @param city the new supervisor's city
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> transferSupervision(String currUser, String currSupervisor, String newSupervisor, String password, String firstName, String lastName, String email, String phoneNumber, String city){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<Boolean> transferSupervisionRes = user.transferSupervision(currSupervisor, newSupervisor);
            if(!transferSupervisionRes.isFailure()){
                if(!userDAO.userExists(newSupervisor)){
                    User supervisor = new User(userDAO.getFullUser(currSupervisor).getResult());
                    Response<User> result = user.registerSupervisor(newSupervisor, UserStateEnum.SUPERVISOR, supervisor.getWorkField(), firstName, lastName, email, phoneNumber, city);
                    if(!result.isFailure()){
                        result.getResult().setAppointments(supervisor.getAppointments());
                        for (String appointee: supervisor.getAppointments()) {
                            userDAO.addAppointment(result.getResult().getUsername(), appointee);
                        }

                        result.getResult().setSurveys(supervisor.getSurveys().getResult());
                        userDAO.insertUser(new UserDBDTO(result.getResult(),passwordEncoder.encode(password)));

                        userDAO.removeUser(currSupervisor);
                        userDAO.addAppointment(currUser, newSupervisor);
                        userDAO.removeWorkPlan(newSupervisor);
                        connectedUsers.remove(currSupervisor);
                        return transferSupervisionRes;
                    }
                    else{
                        return new Response<>(false, true, result.getErrMsg());
                    }
                }
                else{
                    return new Response<>(false, true, "chosen supervisor doesn't exist");
                }
            }
            else{
                return transferSupervisionRes;
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * transfer supervision to an existing user
     * @param currUser the admin's username
     * @param currSupervisor the current supervisor to be replaced
     * @param newSupervisor the new supervisor's username
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> transferSupervisionToExistingUser(String currUser, String currSupervisor, String newSupervisor){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<Boolean> transferSupervisionRes = user.transferSupervision(currSupervisor, newSupervisor);
            if(!transferSupervisionRes.isFailure()){
                if(userDAO.userExists(newSupervisor) && userDAO.userExists(currSupervisor)){
                    User currSup = new User(userDAO.getFullUser(currSupervisor).getResult());
                    User newSup = new User(userDAO.getFullUser(newSupervisor).getResult());
                    newSup.setState(UserStateEnum.SUPERVISOR);
                    newSup.setAppointments(currSup.getAppointments());
                    for (String appointee: currSup.getAppointments()) {
                        userDAO.addAppointment(newSup.getUsername(), appointee);
                    }
                    newSup.removeAppointment(newSupervisor);//remove yourself from your own appointment
                    userDAO.removeAppointment(newSup.getUsername(), newSup.getUsername());
                    newSup.setSurveys(currSup.getSurveys().getResult());
                    newSup.setSchools(new Vector<>());
                    userDAO.resetSchools(newSup.getUsername());
                    userDAO.updateUserState(newSup.getUsername(), newSup.getState().getStateEnum().getState());
                    userDAO.updateSurveys(newSup.getUsername(), newSup.getSurveys().getResult());
                    userDAO.removeUser(currSupervisor);
                    userDAO.removeWorkPlan(newSupervisor);
                    connectedUsers.remove(currSupervisor);
                    if(connectedUsers.containsKey(newSupervisor)){
                        newSup = connectedUsers.get(newSupervisor);
                        newSup.setState(UserStateEnum.SUPERVISOR);
                        newSup.setAppointments(currSup.getAppointments());
                        newSup.removeAppointment(newSupervisor);//remove yourself from your own appointment
                        newSup.setSurveys(currSup.getSurveys().getResult());
                        newSup.setSchools(new Vector<>());
                    }
                    userDAO.addAppointment(currUser, newSupervisor);
                    return transferSupervisionRes;
                }
                else{
                    return new Response<>(false, true, "chosen supervisor doesn't exist");
                }
            }
            else{
                return transferSupervisionRes;
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    //Basket Start
    public Response<Boolean> hasCreatedBasket(String currUser, String basketId) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.hasCreatedBasket(basketId);
        }
        else {
            return new Response<>(false, true, "User not connected");
        }
    }

    public Response<String> createBasket(String currUser, String basketId) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.createBasket(basketId);
        }
        else {
            return new Response<>("", true, "User not connected");
        }
    }

    public Response<String> removeBasket(String currUser, String basketId) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.removeBasket(basketId);
        }
        else {
            return new Response<>("", true, "User not connected");
        }
    }
    //Basket end

    //Survey start

    /**
     *
     * @param currUser supervisor's username
     * @param surveyId the survey's id
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> hasCreatedSurvey(String currUser, String surveyId) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.hasCreatedSurvey(surveyId);
        }
        else {
            return new Response<>(false, true, "User not connected");
        }
    }

    /**
     * adds the survey id to the supervisor's surveys list
     * @param currUser the supervisor creating the survey
     * @param surveyId the survey id
     * @return a response of the successfulness of the action containing the created survey id if successful
     */
    public Response<String> createSurvey(String currUser, String surveyId) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<String> surveyCreation = user.createSurvey(surveyId);
            if(!surveyCreation.isFailure()){
                return userDAO.addSurvey(currUser, surveyId);
            }
            else {
                return surveyCreation;
            }
        }
        else {
            return new Response<>("", true, "User not connected");
        }
    }

    /**
     * removes the survey id from the supervisor's surveys list
     * @param currUser the supervisor's username
     * @param surveyId the survey id of the survey
     * @return a response of the successfulness of the action containing the removed survey id if successful
     */
    public Response<String> removeSurvey(String currUser, String surveyId) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.removeSurvey(surveyId);
        }
        else {
            return new Response<>("", true, "User not connected");
        }
    }

    /**
     * verifies if the user is allows to publish the survey and provides the survey link
     * @param username the supervisor's username
     * @param surveyToken the token for the survey link
     */
    public void notifySurveyCreation(String username, String surveyToken) {
        if(connectedUsers.containsKey(username)) {
            User user = connectedUsers.get(username);
            Response<String> response = user.publishSurvey();
            if(!response.isFailure()){
                emailController.sendEmail(response.getResult(), "https://maane-server.herokuapp.com/survey/getSurvey?surveyID=" + surveyToken);
            }
        }
/*        else {
            return new Response<>(null, true, "User not connected");
        }*/
        // the username is the name of the supervisor created a survey,
        // email all relevant coordinator and send them this link (for now):
        // http://localhot:8080/survey/getSurvey/surveyID={surveyToken}
    }

    /**
     * get the surveys created by the user
     * @param currUser the supervisor's username
     * @return all the surveys submitted by the supervisor
     */
    public Response<List<String>> getSurveys(String currUser){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.getSurveys();
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }
    //Survey end

    //Goals start
    /**
     * get all the goals of the provided year from the supervisor's work field
     * @param currUser the supervisor's username
     * @param year the goals year
     * @return all goals from the supervisor's work field of that year wrapped in a response
     */
    public Response<List<GoalDTO>> getGoals(String currUser, Integer year){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<String> res = user.getGoals();
            if(!res.isFailure()){
                return goalsManagement.getGoalsDTO(res.getResult(), year);
            }
            else{
                return new Response<>(null, true, res.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * add a goal to the work field in a provided year
     * @param currUser the supervisor wishing to add the goal
     * @param goalDTO an object representing the goal
     * @param year the goal assigned year
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> addGoal(String currUser, GoalDTO goalDTO, Integer year){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<String> res = user.addGoals();
            if(!res.isFailure()){
                return goalsManagement.addGoalToField(res.getResult(), goalDTO, year);
            }
            else{
                return new Response<>(null, true, res.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * remove a goal from the work field in a provided year
     * @param currUser the supervisor wishing to remove the goal
     * @param year the goal year
     * @param goalId the goal's id
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> removeGoal(String currUser, Integer year, int goalId) {
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<String> res = user.removeGoal();
            if (!res.isFailure()) {
                return goalsManagement.removeGoal(user.workField, year, goalId);
            } else {
                return new Response<>(null, true, res.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }
    //Goals end

    //Coordinator start
    private String createToken(){
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * register and assign a coordinator to a school
     * @param currUser the user calling the function
     * @param workField the coordinator's work field in case of an admin otherwise irrelevant
     * @param firstName coordinator's first name
     * @param lastName coordinator's last name
     * @param email coordinator's email
     * @param phoneNumber coordinator's phone number
     * @param school coordinator's school symbol
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> assignCoordinator(String currUser, String workField, String firstName, String lastName, String email, String phoneNumber, String school){
        if(email.length() != 0 && !isValidEmailAddress(email))
            return new Response<>(false, true, "invalid email address");

        if(phoneNumber.length() != 0 && !isValidPhoneNumber(phoneNumber))
            return new Response<>(false, true, "invalid phone number");

        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<User> result = user.assignCoordinator(createToken(), workField, school, firstName, lastName, email, phoneNumber);
            if (!result.isFailure()) {
                Response<UserDBDTO> isCoordinatorAssignedRes = userDAO.getCoordinator(school, result.getResult().getWorkField());
                if(!isCoordinatorAssignedRes.isFailure() && isCoordinatorAssignedRes.getResult() == null){
                    userDAO.insertUser(new UserDBDTO(result.getResult(), null));
                    userDAO.assignSchoolToUser(result.getResult().getUsername(), result.getResult().getSchools().get(0));
                    return new Response<>(true, false, "assigned coordinator");
                }
                else{
                    return new Response<>(false, true, "a coordinator is already assigned to the school");
                }
            }
            else{
                return new Response<>(null, result.isFailure(), result.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * get coordinator's info
     * @param currUser the user calling the function
     * @param workField the coordinator's work field in case of an admin otherwise irrelevant
     * @param symbol the school symbol of the coordinator
     * @return an object representing the coordinator
     */
    public Response<UserDBDTO> getCoordinator(String currUser, String workField, String symbol){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<String> workFieldRes = user.getCoordinator();
            if (!workFieldRes.isFailure()) {
                if(workFieldRes.getResult().equals("")){
                    return userDAO.getCoordinator(symbol, workField);
                }
                else{
                    return userDAO.getCoordinator(symbol, workFieldRes.getResult());
                }
            }
            else{
                return new Response<>(null, workFieldRes.isFailure(), workFieldRes.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * remove a coordinator from the system and from the school
     * @param currUser the user calling the function
     * @param workField the coordinator's work field in case of an admin otherwise irrelevant
     * @param school the school symbol of the coordinator
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> removeCoordinator(String currUser, String workField, String school){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<String> response = user.removeCoordinator(school, workField);
            if(!response.isFailure()){
                return userDAO.removeCoordinator(response.getResult(), school);
            }
            else{
                return new Response<>(false, true, response.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     *
     * @param currUser the supervisor sending the emails
     * @param surveyLink a link to the survey published
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> sendCoordinatorEmails(String currUser, String surveyLink) {//todo probably remove this and keep survey notification
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            if(user.isSupervisor().getResult()){
                return emailController.sendEmail(user.getWorkField(), surveyLink);//todo verify existence of the survey link
            }
            else{
                return new Response<>(null, true, "user isn't supervisor");
            }
        }
        else {
            return new Response<>(null, true, "user is not logged in");
        }
    }
    //Coordinator end

    //WorkPlan start
    public void assignWorkPlanYear(String instructor, Integer year) {
        if(connectedUsers.containsKey(instructor)){
            User user = connectedUsers.get(instructor);
            user.assignWorkPlanYear(year);
        }
    }

    /**
     * view the work plan on that month and year
     * @param currUser the instructor wishing to see the his work plan
     * @param year the year of the work plan
     * @param month the month of the work plan
     * @return an object representing the work plan
     */
    public Response<WorkPlanDTO> viewWorkPlan(String currUser, Integer year, Integer month){
        if(connectedUsers.containsKey(currUser)) {
            User user = new User(userDAO.getFullUser(currUser).getResult());
            Response<Integer> workPlanResponse = user.getWorkPlanByYear(year, month);
            if(!workPlanResponse.isFailure()){
                Response<WorkPlanDTO> workPlanDTOResponse = workPlanDAO.getUserWorkPlanByYearAndMonth(currUser, workPlanResponse.getResult(), month);
                if(!workPlanDTOResponse.isFailure()){
                    Response<List<Pair<LocalDateTime, ActivityDTO>>> holidaysRes = new HolidaysHandler(year).getHolidaysAsActivity(year, month);
                    if(!holidaysRes.isFailure()){
                        workPlanDTOResponse.getResult().getCalendar().addAll(holidaysRes.getResult());
                        return workPlanDTOResponse;
                    }
                    else{
                        return new Response<>(null, holidaysRes.isFailure(), holidaysRes.getErrMsg());
                    }
                }
                else{
                    return workPlanDTOResponse;
                }
            }
            else{
                return new Response<>(null, true, workPlanResponse.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * view an instructor's work plan by his supervisor on that month and year
     * @param currUser the supervisor wishing to see the @instructor's work plan
     * @param instructor the instructor's username
     * @param year the year of the work plan
     * @param month the month of the work plan
     * @return an object representing the work plan
     */
    public Response<WorkPlanDTO> viewInstructorWorkPlan(String currUser, String instructor, Integer year, Integer month) {//todo test it but should be fine
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            Response<Integer> workPlanResponse = user.getInstructorWorkPlan(instructor, year, month);
            if(!workPlanResponse.isFailure()){
                Response<WorkPlanDTO> workPlanDTOResponse = workPlanDAO.getUserWorkPlanByYearAndMonth(instructor, workPlanResponse.getResult(), month);
                if(!workPlanDTOResponse.isFailure()){
                    Response<List<Pair<LocalDateTime, ActivityDTO>>> holidaysRes = new HolidaysHandler(year).getHolidaysAsActivity(year, month);
                    if(!holidaysRes.isFailure()){
                        workPlanDTOResponse.getResult().getCalendar().addAll(holidaysRes.getResult());
                        return workPlanDTOResponse;
                    }
                    else{
                        return new Response<>(null, holidaysRes.isFailure(), holidaysRes.getErrMsg());
                    }
                }
                else{
                    return workPlanDTOResponse;
                }
            }
            else{
                return new Response<>(null, true, workPlanResponse.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    private LocalDateTime stringToDate(String dateString){
        String[] dateArr = dateString.split("T");
        return LocalDate.parse(dateArr[0]).atTime(LocalTime.parse(dateArr[1]));
    }

    /**
     * edit the activity's schedule
     * @param currUser the instructor wishing to change the activity date
     * @param currActStart current activity starting date
     * @param newActStart updated activity starting date
     * @param newActEnd updated activity ending date
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> editActivity(String currUser, String currActStart, String newActStart, String newActEnd){ //todo test it
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);//todo maybe verify the dao was generated
            LocalDateTime currActStartDate = stringToDate(currActStart);
            LocalDateTime newActStartDate = stringToDate(newActStart);
            LocalDateTime newActEndDate = stringToDate(newActEnd);

            Response<Boolean> editActivityRes = user.editActivity(currActStartDate);//todo change active user info
            if(!editActivityRes.isFailure()){
                return workPlanDAO.updateActivity(currUser, currActStartDate, newActStartDate, newActEndDate);
            }
            else{
                return new Response<>(null, true, editActivityRes.getErrMsg() + " / colliding activity hours");
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * allows an instructor to add an activity to his schedule
     * @param currUser the user wishing to add the activity
     * @param startAct the activity beginning date
     * @param schoolId the school symbol in which the activity will occur
     * @param goalId the goal's id
     * @param title the goal's title
     * @param endAct the activity ending date
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> addActivity(String currUser, String startAct, String schoolId, int goalId, String title, String endAct) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);//todo maybe verify the dao was generated

            LocalDateTime startActDate = stringToDate(startAct);
            LocalDateTime endActDate = stringToDate(endAct);

            ActivityDTO activity = new ActivityDTO(schoolId, goalId, title, endActDate);
            Response<Integer> addActivityRes = user.addActivity(startActDate, activity.getSchoolId());//todo change active user info
            if(!addActivityRes.isFailure()){
                return workPlanDAO.addActivity(currUser, startActDate, activity, addActivityRes.getResult());
            }
            else{
                return new Response<>(null, true, addActivityRes.getErrMsg() + " / colliding activity hours");
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }    }

    /**
     * allows an instructor to remove an activity from his schedule
     * @param currUser the user wishing to remove the activity
     * @param startAct the activity's beginning date
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> removeActivity(String currUser, String startAct) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);//todo maybe verify the dao was generated
            LocalDateTime startActDate = stringToDate(startAct);
            Response<Boolean> removeActivityRes = user.removeActivity(startActDate);//todo change active user info
            if(!removeActivityRes.isFailure()){
                return workPlanDAO.removeActivity(currUser, startActDate);
            }
            else{
                return new Response<>(null, true, removeActivityRes.getErrMsg() + " / colliding activity hours");
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * verify if the user is allowed to generate a schedules
     * @param currUser the supervisor wishing to generate schedules
     * @return response containing the work field of current user
     */
    public Response<String> generateSchedule(String currUser) {
        if (connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);
            return user.generateSchedule();
        }
        else {
            return new Response<>("", true, "user is not logged in");
        }
    }
    //WorkPlan end

    //Monthly Report start

    /**
     * verify if the user is allowed to generate a monthly report
     * @param currUser username
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> canGenerateReport(String currUser) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);//todo maybe verify the dao was generated
            return user.canGenerateReport();
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * get user details for the monthly report
     * @param currUser user wishing to generate a monthly report
     * @return an object representing the user's monthly report details wrapped in a response
     */
    public Response<UserInfoDTO> getUserReportInfo(String currUser) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);//todo maybe verify the dao was generated
            Response<Boolean> canGenerateReportRes = user.canGenerateReport();
            if(!canGenerateReportRes.isFailure()){
                return userDAO.getUserReportInfo(currUser);
            }
            else{
                return new Response<>(null, true, canGenerateReportRes.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    /**
     * get the user's activities for the monthly report
     * @param currUser user's username
     * @param year the requested year
     * @param month the requested year
     * @return a list of all the user's activities from that month wrapped in a response
     */
    public Response<List<UserActivityInfoDTO>> getUserActivities(String currUser, int year, int month) {
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);//todo maybe verify the dao was generated
            Response<Boolean> canGenerateReportRes = user.canGenerateReport();
            if(!canGenerateReportRes.isFailure()){
                return userDAO.getUserActivities(currUser, year, month);
            }
            else{
                return new Response<>(null, true, canGenerateReportRes.getErrMsg());
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }
    //Monthly Report end

    /**
     * edit the user's work time
     * @param currUser the user setting work time
     * @param workDay the work day of the week
     * @param act1Start the date and time of the beginning of the first activity
     * @param act1End the date and time of the end of the first activity
     * @param act2Start the date and time of the beginning of the second activity
     * @param act2End the date and time of the end of the second activity
     * @return a response of the successfulness of the action
     */
    public Response<Boolean> setWorkingTime(String currUser, int workDay, String act1Start, String act1End, String act2Start, String act2End){
        if(connectedUsers.containsKey(currUser)) {
            User user = connectedUsers.get(currUser);//todo maybe verify the dao was generated
            LocalTime act1StartTime = LocalTime.parse(act1Start);
            LocalTime act1EndTime = LocalTime.parse(act1End);
            LocalTime act2StartTime = LocalTime.parse(act2Start);
            LocalTime act2EndTime = LocalTime.parse(act2End);

            Response<Boolean> changeWorkTime = user.canSetWorkingTime();
            if(workDay >= 0 && workDay <= 6 && noActivityCollision(act1StartTime, act1EndTime, act2StartTime, act2EndTime) && !changeWorkTime.isFailure()){
                Response<Boolean> setWorkingTimeRes = userDAO.setWorkingTime(currUser, workDay, act1StartTime, act1EndTime, act2StartTime, act2EndTime);
                if(!setWorkingTimeRes.isFailure()){
                    user.setWorkDay(workDay);
                    user.setAct1Start(act1StartTime);
                    user.setAct1End(act1EndTime);
                    user.setAct2Start(act2StartTime);
                    user.setAct2End(act2EndTime);
                }
                return setWorkingTimeRes;
            }
            else{
                return new Response<>(null, true, changeWorkTime.getErrMsg() + " / colliding activity hours");
            }
        }
        else {
            return new Response<>(null, true, "User not connected");
        }
    }

    private boolean noActivityCollision(LocalTime act1Start, LocalTime act1End, LocalTime act2Start, LocalTime act2End) {
        return act1Start.isBefore(act1End) &&
                (act1End.isBefore(act2Start) || act1End.equals(act2Start))
                && act2Start.isBefore(act2End);
    }

    /**
     * get the instructors work time related details (workDay, act1Start, act1End, act2Start, act2End)
     * @param instructor instructor
     * @return an object representing the user's work time related fields wrapped by a response
     */
    public Response<UserDBDTO> getWorkHours(String instructor) {
        return userDAO.getWorkingTime(instructor);
    }

    /**
     * check if coordinator assigned to school {@param symbol} can answer to given survey
     * @param symbol the symbol of school
     * @param surveyID identifier of survey
     * @return positive result if the answers made by suitable coordinator
     */
    public Response<Boolean> isValidAnswer(String symbol, String surveyID) {
        Response<String> supervisorRes = userDAO.getSurveyCreator(surveyID);
        if(!supervisorRes.isFailure()){
            Response<String> userWorkFieldResponse = userDAO.getUserWorkField(supervisorRes.getResult());
            if (!userWorkFieldResponse.isFailure()) {
                Response<UserDBDTO> coordinatorRes = userDAO.getCoordinator(symbol, userWorkFieldResponse.getResult());
                if(!coordinatorRes.isFailure() && !(coordinatorRes.getResult() == null)){
                    return new Response<>(true, false, coordinatorRes.getErrMsg());
                }
                else{
                    return new Response<>(false, true, coordinatorRes.getErrMsg());
                }
            }
        }
        return new Response<>(false, true,"not allowed to answer");
    }


    //For test purposes only start

    public User getUser(String user){
        return new User(userDAO.getFullUser(user).getResult());
    }

    public Response<User> getUserRes(String user){
        if(userDAO.userExists(user)){
            return new Response<>(new User(userDAO.getFullUser(user).getResult()), false, "user found");
        }
        return new Response<>(null, true, "user not found");
    }

    public void clearUsers(){
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.connectedUsers = new ConcurrentHashMap<>();
        this.userDAO.deleteUsers();
        this.goalsManagement = GoalsManagement.getInstance();
        SurveyDAO.getInstance().clearCache();
        adminBoot("admin", "admin123");
    }

    public void resetDB(){
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.connectedUsers = new ConcurrentHashMap<>();
        this.userDAO.clearDB();
        this.goalsManagement = GoalsManagement.getInstance();
        adminBoot("admin", "admin123");
    }

    public Response<Boolean> removeCoordinatorTester(String currUser, String workField, String school){
        User user = new User(userDAO.getFullUser(currUser).getResult());
        Response<String> response = user.removeCoordinator(school, workField);
        if(!response.isFailure()){
            return userDAO.removeCoordinator(response.getResult(), school);//todo check not failed
        }
        else{
            return new Response<>(false, true, response.getErrMsg());
        }
    }

    public Response<Boolean> assignCoordinatorTester(String currUser, String workField, String firstName, String lastName, String email, String phoneNumber, String school){
        User user = new User(userDAO.getFullUser(currUser).getResult());
        Response<User> result = user.assignCoordinator(createToken(), workField, school, firstName, lastName, email, phoneNumber);
        if (!result.isFailure()) {
            Response<UserDBDTO> isCoordinatorAssignedRes = userDAO.getCoordinator(school, result.getResult().getWorkField());
            if(!isCoordinatorAssignedRes.isFailure() && isCoordinatorAssignedRes.getResult() == null){
                userDAO.insertUser(new UserDBDTO(result.getResult(), null));
                userDAO.assignSchoolToUser(result.getResult().getUsername(), school);
                return new Response<>(true, false, "assigned coordinator");
            }
            else{
                return new Response<>(false, true, "a coordinator is already assigned to the school");
            }
        }
        else{
            return new Response<>(null, result.isFailure(), result.getErrMsg());
        }
    }

    public Response<Boolean> changePasswordTester(String currUser, String newPassword){
        if(!ServerContextInitializer.getInstance().isTestMode() && !isValidPassword(newPassword)){
            return new Response<>(false, true, "The password isn't strong enough");
        }
        User user = new User(userDAO.getFullUser(currUser).getResult());
        Response<Boolean> res = user.changePassword();
        if (res.getResult()) {
            userDAO.updateUserPassword(currUser, passwordEncoder.encode(newPassword));
        }
        return res;
    }

    public Response<Boolean> removeUserTester(String currUser, String userToRemove) {
        User user = new User(userDAO.getFullUser(currUser).getResult());
        Response<Boolean> response = user.removeUser(userToRemove);//todo verify connected user is dao updated
            if(!response.isFailure()){
                if(userDAO.userExists(userToRemove)){
                    if(response.getResult()){
                        findSupervisorAndRemoveAppointment(user, userToRemove);
                    }
                    else{
                        userDAO.removeAppointment(currUser, userToRemove);
                    }
                    response = userDAO.removeUser(userToRemove);
                    if(!response.isFailure()){
                        connectedUsers.remove(userToRemove);
                    }
                    return response;
                }
                else{
                    return new Response<>(null, true, "User is not in the system");
                }
            }
            return response;
    }
    //For test purposes only end
}
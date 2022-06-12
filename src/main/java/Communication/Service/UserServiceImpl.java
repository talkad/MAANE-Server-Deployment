package Communication.Service;

import Communication.DTOs.GoalDTO;
import Communication.DTOs.UserDTO;
import Communication.DTOs.WorkPlanDTO;
import Communication.Service.Interfaces.UserService;
import Domain.CommonClasses.Response;
import Domain.UsersManagment.User;
import Domain.UsersManagment.UserController;
import Persistence.DbDtos.UserDBDTO;
import Persistence.UserQueries;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Response<String> pwdRes;
        Response<User> userRes = UserController.getInstance().getUserRes(username);


        if(userRes.isFailure()){
            log.error("user not exist");
            throw new UsernameNotFoundException("user not found");
        }
        else{
            log.info("user {} found", username);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userRes.getResult().getState().getStateEnum().getState()));

        pwdRes = UserQueries.getInstance().getPassword(username);

        System.out.println("the password is: " + pwdRes);

        return new org.springframework.security.core.userdetails.User(username, pwdRes.isFailure()? "" : pwdRes.getResult(), authorities);
    }

    @Override
    public Response<String> login(String username) {
        Response<String> res = UserController.getInstance().login(username);

        if (res.isFailure())
            log.error("failed to login with user {}", username);
        else
            log.info("login successful for {}", username);

        return res;
    }

    @Override
    public Response<String> logout(String name) {
        Response<String> res = UserController.getInstance().logout(name);

        if (res.isFailure())
            log.error("failed to logout user {}", name);
        else
            log.info("logged out for user {}", name);

        return res;
    }

    @Override
    public Response<String> registerUser(String currUser, UserDTO user) {
        Response<String> res = UserController.getInstance().registerUser(currUser,
                user.getUserToRegister(), user.getPassword(), user.getUserStateEnum(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getPhoneNumber(), user.getCity());

        if (res.isFailure())
            log.error("failed to register user {}", user.getUserToRegister());
        else
            log.info("user {} registered successfully", user.getUserToRegister());

        return res;
    }

    @Override
    public Response<String> registerUserBySystemManager(String currUser, UserDTO user, String optionalSupervisor) {
        Response<String> res = UserController.getInstance().registerUserBySystemManager(currUser, user.getUserToRegister(), user.getPassword(), user.getUserStateEnum(), optionalSupervisor, user.getWorkField(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhoneNumber(), user.getCity());
        if (res.isFailure())
            log.error("failed to register user {}", user.getUserToRegister());
        else
            log.info("user {} registered successfully", user.getUserToRegister());

        return res;
    }

    @Override
    public Response<Boolean> removeUser(String currUser, String userToRemove) {
        Response<Boolean> res = UserController.getInstance().removeUser(currUser, userToRemove);

        if (res.isFailure())
            log.error("failed to remove user {}", userToRemove);
        else
            log.info("removed user {}", userToRemove);

        return res;
    }

    @Override
    public Response<WorkPlanDTO> viewWorkPlan(String currUser, Integer year, Integer month) {
        Response<WorkPlanDTO> res = UserController.getInstance().viewWorkPlan(currUser, year, month);

        if (res.isFailure())
            log.error("user {} cannot view plan", currUser);
        else
            log.info("user {} viewed plan", currUser);

        return res;
    }

    @Override
    public Response<WorkPlanDTO> viewInstructorWorkPlan(String currUser, String instructor, Integer year, Integer month) {
        Response<WorkPlanDTO> res = UserController.getInstance().viewInstructorWorkPlan(currUser, instructor, year, month);

        if (res.isFailure())
            log.error("user {} cannot view {}'s work plan", currUser, instructor);
        else
            log.info("user {} viewed {}'s work plan", currUser, instructor);

        return res;
    }

    @Override
    public Response<List<UserDTO>> getAppointedUsers(String currUser) {
        Response<List<UserDTO>> res = UserController.getInstance().getAppointedUsers(currUser);

        if (res.isFailure())
            log.error("failed to get Appointed users for the user {}", currUser);
        else
            log.info("user {} successfully received appointed users", currUser);
        return res;
    }

    //for testing purposes only
    @Override
    public Response<User> getUserRes(String username) {
        return UserController.getInstance().getUserRes(username);
    }

    @Override
    public Response<Boolean> assignSchoolToUser(String currUser, String userToAssignName, String school) {
        Response<Boolean> res = UserController.getInstance().assignSchoolToUser(currUser, userToAssignName, school);

        if (res.isFailure())
            log.error("failed to assign schools for the user {} by the user {}", userToAssignName, currUser);
        else
            log.info("user {} was successfully assigned to the schools by {}", userToAssignName, currUser);
        return res;
    }

    @Override
    public Response<Boolean> verifyUser(String currUser, String password) {
        Response<Boolean> res = UserController.getInstance().verifyUser(currUser, password);

        if (res.isFailure())
            log.error("failed to verify the user {}", currUser);
        else
            log.info("successfully verified the user {}", currUser);

        return res;
    }

    public Response<Boolean> addGoal(String currUser, GoalDTO goalDTO, Integer year) {
        Response<Boolean> res = UserController.getInstance().addGoal(currUser, goalDTO, year);

        if (res.isFailure())
            log.error("failed to add goals by {}", currUser);
        else
            log.info("user {} successfully added goals", currUser);
        return res;
    }

    public Response<Boolean> removeGoal(String currUser, Integer year, int goalId) {
        Response<Boolean> res = UserController.getInstance().removeGoal(currUser, year, goalId);

        if (res.isFailure())
            log.error("failed to remove goal by {}", currUser);
        else
            log.info("user {} successfully removed goal", currUser);
        return res;
    }

    public Response<List<GoalDTO>> getGoals(String currUser, Integer year) {
        Response<List<GoalDTO>> res = UserController.getInstance().getGoals(currUser, year);

        if (res.isFailure())
            log.error("failed to view goal by {}", currUser);
        else
            log.info("the user {} successfully accessed goals", currUser);
        return res;
    }

    public Response<Boolean> updateInfo(String currUser, String firstName, String lastName, String email, String phoneNumber, String city) {
        Response<Boolean> res = UserController.getInstance().updateInfo(currUser, firstName, lastName, email, phoneNumber, city);
        if (res.isFailure())
            log.error("failed to update {}'s info", currUser);
        else
            log.info("successfully updated {}'s info", currUser);
        return res;
    }

    public Response<UserDTO> getUserInfo(String currUser) {
        Response<UserDTO> res = UserController.getInstance().getUserInfo(currUser);
        if (res.isFailure())
            log.error("failed to get {}'s info", currUser);
        else
            log.info("successfully acquired {}'s info", currUser);
        return res;
    }

        public Response<Boolean> changePasswordToUser(String currUser, String userToChangePassword, String newPassword, String confirmPassword) {
        Response<Boolean> res = UserController.getInstance().changePasswordToUser(currUser, userToChangePassword, newPassword, confirmPassword);
        if (res.isFailure())
            log.error("failed to update {}'s password", userToChangePassword);
        else
            log.info("successfully updated {}'s password", userToChangePassword);
        return res;
    }

    public Response<Boolean> changePassword(String currUser, String currPassword, String newPassword, String confirmPassword) {
        Response<Boolean> res = UserController.getInstance().changePassword(currUser, currPassword, newPassword, confirmPassword);
        if (res.isFailure())
            log.error("failed to update {}'s password", currUser);
        else
            log.info("successfully updated {}'s password", currUser);
        return res;
    }

    public Response<List<UserDTO>> getAllUsers(String currUser) {
        Response<List<UserDTO>> res = UserController.getInstance().getAllUsers(currUser);
        if (res.isFailure())
            log.error("failed to acquire all users by {}", currUser);
        else
            log.info("successfully to acquired all users by {}", currUser);
        return res;
    }

    @Override
    public Response<List<UserDTO>> getSupervisors(String currUser) {
        Response<List<UserDTO>> res = UserController.getInstance().getSupervisors(currUser);
        if (res.isFailure())
            log.error("failed to acquire supervisors by the user {}", currUser);
        else
            log.info("successfully acquired supervisors by the user {}", currUser);
        return res;
    }

    public Response<Boolean> removeSchoolFromUser(String currUser, String userToRemoveSchoolsName, String school) {
        Response<Boolean> res = UserController.getInstance().removeSchoolFromUser(currUser, userToRemoveSchoolsName, school);
        if (res.isFailure())
            log.error("failed to remove school from user by {}", currUser);
        else
            log.info("successfully removed schools from the user {} by {}", userToRemoveSchoolsName, currUser);
        return res;
    }

    @Override
    public Response<Boolean> sendCoordinatorEmails(String currUser, String surveyLink, String surveyToken) { //todo: shaked - exception handler
        Response<Boolean> res = UserController.getInstance().sendCoordinatorEmails(currUser, surveyLink);
        if (res.isFailure())
            log.error("failed send emails by {}", currUser);
        else
            log.info("{} successfully sent the emails", currUser);
        return res;
    }

    @Override
    public Response<Boolean> transferSupervision(String currUser, String currSupervisor, String newSupervisor, String password, String firstName, String lastName, String email, String phoneNumber, String city) {
        Response<Boolean> res = UserController.getInstance().transferSupervision(currUser, currSupervisor, newSupervisor, password, firstName, lastName, email, phoneNumber, city);
        if (res.isFailure())
            log.error("failed to transfer supervision from {} to {} by {}", currSupervisor,  newSupervisor, currUser);
        else
            log.info("successfully transferred supervision from {} to {} by {}", currSupervisor,  newSupervisor, currUser);
        return res;
    }

    @Override
    public Response<Boolean> transferSupervisionToExistingUser(String currUser, String currSupervisor, String newSupervisor) {
        Response<Boolean> res = UserController.getInstance().transferSupervisionToExistingUser(currUser, currSupervisor, newSupervisor);
        if (res.isFailure())
            log.error("failed to transfer supervision from {} to {} by {}", currSupervisor,  newSupervisor, currUser);
        else
            log.info("successfully transferred supervision from {} to {} by {}", currSupervisor,  newSupervisor, currUser);
        return res;
    }

    @Override
    public Response<UserDBDTO> getCoordinator(String currUser, String workField, String symbol) {
        Response<UserDBDTO> res = UserController.getInstance().getCoordinator(currUser, workField, symbol);

        if (res.isFailure())
            log.error("{} failed to receive coordinator of school {}", currUser, symbol);
        else
            log.info("{} received coordinator of school {}", currUser, symbol);

        return res;
    }

    @Override
    public Response<List<String>> allWorkFields(String currUser) {
        Response<List<String>> res = UserController.getInstance().allWorkFields(currUser);

        if (res.isFailure())
            log.error("{} failed to receive work fields", currUser);
        else
            log.info("{} received work fields", currUser);

        return res;
    }

    @Override
    public Response<Boolean> setWorkingTime(String currUser, int workDay, String act1Start, String act1End, String act2Start, String act2End){
        Response<Boolean> res = UserController.getInstance().setWorkingTime(currUser, workDay, act1Start, act1End, act2Start, act2End);

        if (res.isFailure())
            log.error("{} failed to change working hours", currUser);
        else
            log.info("{} changed working hours", currUser);

        return res;
    }

    @Override
    public Response<UserDBDTO> getWorkHours(String instructor) {
        Response<UserDBDTO> res = UserController.getInstance().getWorkHours(instructor);

        if (res.isFailure())
            log.error("failed to view {}'s working hours ", instructor);
        else
            log.info("got {}'s working hours", instructor);

        return res;
    }

    @Override
    public Response<Boolean> editActivity(String currUser, String currActStart, String newActStart, String newActEnd) {
        Response<Boolean> res = UserController.getInstance().editActivity(currUser, currActStart, newActStart, newActEnd);

        if (res.isFailure())
            log.error("failed to edit {}'s activity ", currUser);
        else
            log.info("edited {}'s activity", currUser);

        return res;
    }

    @Override
    public Response<Boolean> addActivity(String currUser, String startAct, String schoolId, int goalId, String title, String endAct) {
        Response<Boolean> res = UserController.getInstance().addActivity(currUser, startAct, schoolId, goalId, title, endAct);

        if (res.isFailure()){
            log.error("{} failed to add the activity because {}", currUser, res.getErrMsg());
        }
        else
            log.info("{} successfully added activity", currUser);

        return res;
    }

    @Override
    public Response<Boolean> removeActivity(String currUser, String startAct) {
        Response<Boolean> res = UserController.getInstance().removeActivity(currUser, startAct);

        if (res.isFailure())
            log.error("{} failed to remove the activity", currUser);
        else
            log.info("{} successfully removed activity", currUser);

        return res;
    }

    @Override
    public Response<Boolean> changePasswordTester(String username, String newPassword) {
        Response<Boolean> res = UserController.getInstance().changePasswordTester(username, newPassword);

        if (res.isFailure())
            log.error("failed to edit {}'s password ", username);
        else
            log.info("edited {}'s password", username);

        return res;
    }

    @Override
    public void resetDB() {
        UserController.getInstance().resetDB();
    }

    @Override
    public Response<Boolean> removeUserTester(String currUser, String userToRemove) {
        Response<Boolean> res = UserController.getInstance().removeUserTester(currUser, userToRemove);

        if (res.isFailure())
            log.error("failed to remove {}'s  ", userToRemove);
        else
            log.info("removed {}'s", userToRemove);

        return res;
    }
}
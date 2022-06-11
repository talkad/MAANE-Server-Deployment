package Domain.UsersManagment;

import Communication.DTOs.UserDTO;
import Domain.CommonClasses.Response;
import Persistence.DbDtos.UserDBDTO;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class User {

    protected UserState state;
    protected String username;
    protected String workField;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String phoneNumber;
    protected String city;
    protected List<String> schools;
    protected List<String> appointments;
    protected List<String> surveys;
    protected List<String> baskets;
    protected int workDay;
    protected LocalTime act1Start;
    protected LocalTime act1End;
    protected LocalTime act2Start;
    protected LocalTime act2End;
    protected List<Integer> workPlanYears;


    public User() {
        this.state = new Guest();
        this.appointments = new Vector<>();
        this.schools = new Vector<>();
        this.surveys = new Vector<>();
        this.baskets = new Vector<>();
    }

    public User(String username, UserStateEnum userStateEnum) { //constructor for admin only
        this.state = inferUserType(userStateEnum);
        this.username = username;
        this.firstName = "admin";
        this.lastName = "admin";
        this.appointments = new Vector<>();
        this.schools = new Vector<>();
        this.surveys = new Vector<>();
        this.baskets = new Vector<>();
    }

    public User(String username, UserStateEnum userStateEnum, String workField, String firstName, String lastName, String email, String phoneNumber, String city) {
        this.state = inferUserType(userStateEnum);
        this.username = username;
        this.workField = workField;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.appointments = new Vector<>();
        this.schools = new Vector<>();
        this.surveys = new Vector<>();
        this.baskets = new Vector<>();
        if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR){
            this.workPlanYears = new Vector<>();
            this.workDay = 0;
            this.act1Start = LocalTime.of(8, 0);
            this.act1End = LocalTime.of(10, 0);
            this.act2Start = LocalTime.of(10, 0);
            this.act2End = LocalTime.of(12, 0);
        }
    }

    public User(String username, UserStateEnum userStateEnum, String workField, String firstName, String lastName, String email, String phoneNumber, List<String> school) {
        this.state = inferUserType(userStateEnum);
        this.username = username;
        this.workField = workField;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.schools = school;
    }

    public User(UserDBDTO userDBDTO) {
        this.state = inferUserType(userDBDTO.getStateEnum());
        this.username = userDBDTO.getUsername();
        this.workField = userDBDTO.getWorkField();
        this.firstName = userDBDTO.getFirstName();
        this.lastName = userDBDTO.getLastName();
        this.email = userDBDTO.getEmail();
        this.phoneNumber = userDBDTO.getPhoneNumber();
        this.city = userDBDTO.getCity();
        this.appointments = userDBDTO.getAppointments();
        this.schools = userDBDTO.getSchools();
        this.surveys = userDBDTO.getSurveys();
        this.baskets = userDBDTO.getBaskets();
        if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR){
            this.workDay = userDBDTO.getWorkDay();
            this.act1Start = userDBDTO.getAct1Start();
            this.act1End = userDBDTO.getAct1End();
            this.act2Start = userDBDTO.getAct2Start();
            this.act2End = userDBDTO.getAct2End();
            this.workPlanYears = userDBDTO.getWorkPlanYears();
        }
    }

    private UserDTO getUserDTO(){
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(this.username);
        userDTO.setWorkField(this.workField);
        userDTO.setFirstName(this.firstName);
        userDTO.setLastName(this.lastName);
        userDTO.setEmail(this.email);
        userDTO.setUserStateEnum(this.state.getStateEnum());
        userDTO.setPhoneNumber(this.phoneNumber);
        userDTO.setCity(this.city);
        userDTO.setSchools(this.schools);
        return userDTO;
    }

    private UserState inferUserType(UserStateEnum userStateEnum) {
        UserState state;

        switch (userStateEnum) {
            case INSTRUCTOR:
                state = new Instructor();
                break;
            case SUPERVISOR:
                state = new Supervisor();
                break;
            case GENERAL_SUPERVISOR:
                state = new GeneralSupervisor();
                break;
            case SYSTEM_MANAGER:
                state = new SystemManager();
                break;
            case COORDINATOR:
                state = new Coordinator();
                break;
            default:
                state = new Registered(); //this is a problem
                break;
        }
        return state;
    }

    public int getWorkDay() {
        return workDay;
    }

    public void setWorkDay(int workDay) {
        this.workDay = workDay;
    }

    public LocalTime getAct1Start() {
        return act1Start;
    }

    public void setAct1Start(LocalTime act1Start) {
        this.act1Start = act1Start;
    }

    public LocalTime getAct1End() {
        return act1End;
    }

    public void setAct1End(LocalTime act1End) {
        this.act1End = act1End;
    }

    public LocalTime getAct2Start() {
        return act2Start;
    }

    public void setAct2Start(LocalTime act2Start) {
        this.act2Start = act2Start;
    }

    public LocalTime getAct2End() {
        return act2End;
    }

    public void setAct2End(LocalTime act2End) {
        this.act2End = act2End;
    }

    public Response<Boolean> logout() {
        if(this.state.allowed(Permissions.LOGOUT, this))
            return new Response<>(true, false, "logged out successfully");
        return new Response<>(false, true, "Cannot logout without being logged in");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username){ this.username = username;}

    public List<String> getSchools() {
        return schools;
    }

    public void setSchools(List<String> schools) {
        this.schools = schools;
    }

    public void addSchool(String school) {
        if(!this.schools.contains(school)){
            this.schools.add(school);
        }
    }

    public Response<String> hasSchool(String symbol) {
        if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR) {
            return new Response<>(this.workField, false, "all schools access");
        }
        else if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER){
            return new Response<>("", false, "all schools access");
        }
        else if(this.schools != null && !this.schools.isEmpty() && this.schools.contains(symbol)){
            return new Response<>(this.workField, false, "has school");
        }
        else{
            return new Response<>(null, true, "user cannot access this school");
        }
    }

    public Response<List<String>> getUserSchools() {
        if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR || this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER) {
            return new Response<>(null, false, "all schools access");
        }
        else if(this.schools != null && !this.schools.isEmpty()){
            return new Response<>(this.schools, false, "own schools access");
        }
        else{
            return new Response<>(null, true, "user cannot access schools");
        }
    }

    public List<String> getAppointments(){
        return this.appointments;
    }

    public void setAppointments(List<String> appointments) {
        this.appointments = appointments;
    }

    public Response<Boolean> assignSchoolsToUser(String userToAssign) {
        if(this.state.allowed(Permissions.ASSIGN_SCHOOLS_TO_USER, this)) {
            if (appointments.contains(userToAssign)) {
                return new Response<>(true, false, "successfully assigned the schools to the user " + userToAssign);
            }
            else {
                return new Response<>(false, true, " the user " + userToAssign + " was not assigned by you");
            }
        }
        else return new Response<>(false, true, "user not allowed to assign schools to users");
    }

    public Response<Boolean> removeUser(String username) {
        if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER && (!this.username.equals(username))){
            if (!appointments.contains(username)) {
                return new Response<>(true, false, "removing a user by system manager");
            }
            else {
                return new Response<>(false, true, "cannot delete supervisor, try transferring supervision");
            }
        }
        else if(this.state.allowed(Permissions.REMOVE_USER, this) && !this.username.equals(username)) {
            if (appointments.contains(username)) {
                boolean res = appointments.remove(username);
                if (res) {
                    return new Response<>(false, false, "successfully removed the user " + username);
                }
                return new Response<>(false, true, "Tried removing a nonexistent appointment");
            } else {
                return new Response<>(false, true, " the user " + username + " was not assigned by you");
            }
        }
        else {
            return new Response<>(false, true, "user not allowed to remove users");
        }
    }

    public Response<User> registerUser(String username, UserStateEnum registerUserStateEnum, String firstName, String lastName, String email, String phoneNumber, String city) {
        if(this.state.allowed(Permissions.REGISTER_USER, this) && (registerUserStateEnum == UserStateEnum.INSTRUCTOR
                || registerUserStateEnum == UserStateEnum.GENERAL_SUPERVISOR) && !appointments.contains(username)) {
            if(!this.appointments.contains(username)) {
                this.appointments.add(username);
                return new Response<>(new User(username, registerUserStateEnum, this.workField, firstName, lastName, email, phoneNumber, city), false, "user successfully assigned");
            }
            else{
                return new Response<>(null, true, "appointment already exists");
            }
        }
        else{
            return new Response<>(null, true, "user not allowed to register users");
        }
    }

    public Response<User> registerUserBySystemManager(String username, UserStateEnum registerUserStateEnum, String workField, String firstName, String lastName, String email, String phoneNumber, String city) {
        if(this.state.allowed(Permissions.REGISTER_BY_ADMIN, this) && (registerUserStateEnum == UserStateEnum.INSTRUCTOR
                || registerUserStateEnum == UserStateEnum.GENERAL_SUPERVISOR)) {
            return new Response<>(new User(username, registerUserStateEnum, workField, firstName, lastName, email, phoneNumber, city), false, "user successfully assigned");
        }
        else{
            return new Response<>(null, true, "user not allowed to register users");
        }
    }

    public Response<User> registerSupervisor(String username, UserStateEnum registerUserStateEnum, String workField, String firstName, String lastName, String email, String phoneNumber, String city) {
        if(this.state.allowed(Permissions.REGISTER_SUPERVISOR, this) && !appointments.contains(username)){
            appointments.add(username);

            return new Response<>(new User(username, registerUserStateEnum, workField, firstName, lastName, email, phoneNumber, city), false, "supervisor successfully assigned");
        }
        else{
            return new Response<>(null, true, "user not allowed to register users");
        }
    }

    public Response<Boolean> addAppointment(String appointee){
        return new Response<>(appointments.add(appointee), false, "successfully added appointment");
    }

    public Response<Boolean> changePasswordToUser(String userToChangePassword) {
        if (this.state.allowed(Permissions.CHANGE_PASSWORD_TO_USER, this)) {
            if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR && this.appointments.contains(userToChangePassword)){
                return new Response<>(true, false,"successfully password changed");
            }
            else if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER){
                return new Response<>(true, false,"successfully password changed");
            }
            return new Response<>(false, false, "user not allowed to change password to this user");
        }
        else {
            return new Response<>(false, true, "user not allowed to change password");
        }
    }

    public Response<Boolean> changePassword() {
        if (this.state.allowed(Permissions.CHANGE_PASSWORD, this)) {
            return new Response<>(true, false, "successfully password changed");
        }
        else {
            return new Response<>(false, true, "user not allowed to change password");
        }
    }

    public Response<String> createSurvey(String surveyId) {
        if(this.state.allowed(Permissions.SURVEY_MANAGEMENT, this)){

            this.surveys.add(surveyId);

            return new Response<>(surveyId, false, "user is allowed to create survey");
        }
        else {

            return new Response<>("", true, "user not allowed to create survey");
        }
    }

    public Response<String> createBasket(String basketId) {
        if(this.state.allowed(Permissions.ADD_BASKET, this)){
            this.baskets.add(basketId);
            return new Response<>(basketId, false, "user successfully added basket");
        }
        else {
            return new Response<>(null, true, "user not allowed to add baskets");
        }
    }

    public Response<String> removeSurvey(String surveyId) {
        if(this.state.allowed(Permissions.SURVEY_MANAGEMENT, this)){
            this.surveys.remove(surveyId);
            return new Response<>(surveyId, false, "user is allowed to remove survey");
        }
        else {
            return new Response<>("", true, "user not allowed to remove survey");
        }
    }

    public Response<String> removeBasket(String basketId) {
        if(this.state.allowed(Permissions.REMOVE_BASKET, this)) {
            if (!hasCreatedBasket(basketId).isFailure()) {
                this.baskets.remove(basketId);
                return new Response<>(basketId, false, "successfully removed basket");
            }
            else {
                return new Response<>("", true, "user isn't allowed to remove a basket he didn't create");
            }
        }
        else {
            return new Response<>(null, true, "user not allowed to remove basket");
        }
    }

    public Response<List<String>> getSurveys() {
        if(this.state.allowed(Permissions.SURVEY_MANAGEMENT, this)){
            return new Response<>(this.surveys, false, "");
        }
        else {
            return new Response<>(new LinkedList<>(), true, "user not allowed to view surveys");
        }
    }

    public void setSurveys(List<String> surveys) {
        this.surveys = surveys;
    }

    public String getWorkField(){
        return this.workField;
    }

    public void setWorkField(String workField) {
        this.workField = workField;
    }

    public Response<Boolean> allowedToRemoveSchool(String userToRemoveSchools) {
        if(this.state.allowed(Permissions.REMOVE_SCHOOLS_FROM_USER, this)) {
            if (appointments.contains(userToRemoveSchools)) {
                return new Response<>(true, false, "successfully removed school assignment");
            }
            else {
                return new Response<>(false, true, " the user " + userToRemoveSchools + " was not assigned by you");
            }
        }
        else return new Response<>(false, true, "user not allowed to remove schools from users");
    }

    public Response<Boolean> removeSchool(String school) {
        if(this.schools != null){
            boolean removed = schools.remove(school);
            return new Response<>(removed, !removed , removed ? "successfully removed the school" : "tried to remove school that wasn't assigned to the user");
        }
        return new Response<>(null, true, "bad initialization of schools");
    }

    public Response<Boolean> canAddSchool(String school) {
        if(this.schools.contains(school)){
            return new Response<>(false, true, "user was already assigned to the school");
        }
        else{
            return new Response<>(true, false, "user was allowed to be assigned to the school");
        }
    }



    public Response<String> getGoals() {
        if(this.state.allowed(Permissions.GET_GOALS, this)){
            return new Response<>(this.workField, false, "user allowed to view goals");
        }
        else {
            return new Response<>("", true, "user not allowed to get goals");
        }
    }

    public Response<String> addGoals() {
        if(this.state.allowed(Permissions.ADD_GOALS, this)){
            return new Response<>(this.workField, false, "user allowed to add goals");
        }
        else {
            return new Response<>("", true, "user not allowed to add goals");
        }
    }

    public Response<Boolean> isSupervisor() {
        if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR)
        {
            return new Response<>(true, false, "user is supervisor");
        }
        else {
            return new Response<>(false, false, "user is not a supervisor");
        }
    }

    public Response<Boolean> hasCreatedSurvey(String surveyId) {
        if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR)
        {
            return new Response<>(this.surveys.contains(surveyId), false, "user is supervisor");
        }
        else {
            return new Response<>(false, true, "user is not a supervisor");
        }
    }

    public Response<Boolean> hasCreatedBasket(String basketId) {
        if(this.baskets.contains(basketId)) {
            return new Response<>(true, false, "user created this basket");
        }

        return new Response<>(false, true, "user didn't create this basket");
    }

    public Response<User> updateInfo(String firstName, String lastName, String email, String phoneNumber, String city) {
        if(this.state.allowed(Permissions.UPDATE_INFO, this)){
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.city = city;
            return new Response<>(this, false, "successfully updated information");
        }
        else {
            return new Response<>(null, true, "user not allowed to update information");
        }
    }

    public Response<String> publishSurvey() {
        if(this.state.allowed(Permissions.SURVEY_MANAGEMENT, this)){
            return new Response<>(this.workField, false, "successfully published survey");
        }
        else {
            return new Response<>(null, true, "user not allowed to publish survey");
        }
    }

    public Response<String> generateSchedule() {
        if (this.state.allowed(Permissions.GENERATE_WORK_PLAN, this)) {
            return new Response<>(this.workField, false, "");
        }
        else {
            return new Response<>("", true, "user not allowed to generate work plan");
        }
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserStateEnum state) {
        this.state = inferUserType(state);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setBaskets(List<String> baskets) {
        this.baskets = baskets;
    }

    public Response<List<String>> getAppointees() {
        if (this.state.allowed(Permissions.VIEW_USERS_INFO, this)) {
            return new Response<>(this.appointments, false, "successfully acquired appointments");
        }
        else {
            return new Response<>(null, true, "user not allowed to view appointed users");
        }
    }

    public boolean isInstructor() {
        return this.getState().getStateEnum() == UserStateEnum.INSTRUCTOR;
    }


    public Response<Integer> getWorkPlanByYear(Integer year , Integer month) {
        if (this.state.allowed(Permissions.VIEW_WORK_PLAN, this)) {//todo fix this
            //return new Response<>(this.workPlanYears.contains(year), !this.workPlanYears.contains(year), "");
            return new Response<>(extractProperYear(LocalDateTime.of(year, month, 1, 0, 0)), false, "");
        }
        else {
            return new Response<>(null, true, "user not allowed to view work plan");
        }
    }

    public Response<Boolean> viewAllUsers() {
        if (this.state.allowed(Permissions.VIEW_ALL_USERS_INFO, this)) {
            return new Response<>(true, false, "");
        }
        else {
            return new Response<>(false, false, "user not allowed to view all users");
        }
    }

    public Response<Boolean> isSystemManager() {
        if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER)
        {
            return new Response<>(true, false, "user is system manager");
        }
        else {
            return new Response<>(false, false, "user is not the system manager");
        }
    }

    public Response<String> removeGoal() {
        if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR)
        {
            return new Response<>(this.workField, false, "acquired work field");
        }
        else {
            return new Response<>(null, true, "not allowed to acquire work field");
        }
    }

    public Response<UserDTO> getInfo() {
        if (this.state.allowed(Permissions.VIEW_INFO, this))
        {
            return new Response<>(getUserDTO(), false, "user dto successfully acquired");
        }
        else {
            return new Response<>(null, true, "user not allowed to view info");
        }
    }

    public Response<User> assignCoordinator(String username, String workField, String school, String firstName, String lastName, String email, String phoneNumber) {
        if (this.state.allowed(Permissions.REGISTER_COORDINATOR, this))
        {
            List<String> schoolList = new Vector<>();
            schoolList.add(school);
            if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR) {
                return new Response<>(new User(username, UserStateEnum.COORDINATOR, this.workField, firstName, lastName, email, phoneNumber, schoolList), false, "successfully assigned coordinator");
            }
            else if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR) {
                if(this.schools.contains(school)){
                    return new Response<>(new User(username,UserStateEnum.COORDINATOR, this.workField, firstName, lastName, email, phoneNumber, schoolList), false, "successfully assigned coordinator");
                }
                else{
                    return new Response<>(null, true, "user not allowed to assign coordinator to the given school");
                }
            }
            else if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER){
                return new Response<>(new User(username,UserStateEnum.COORDINATOR, workField, firstName, lastName, email, phoneNumber, schoolList), false, "successfully assigned coordinator");
            }
        }
        return new Response<>(null, true, "user not allowed to assign coordinator");
    }

    public Response<String> removeCoordinator(String school, String workField) {
        if (this.state.allowed(Permissions.REMOVE_COORDINATOR, this))
        {
            if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR) {
                return new Response<>(this.workField, false, "successfully removed coordinator");
            }
            else if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR) {
                if(this.schools.contains(school)){
                    return new Response<>(this.workField, false, "successfully removed coordinator");
                }
                else{
                    return new Response<>(null, true, "user not allowed to remove coordinator from the given school");
                }
            }
            else if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER){
                return new Response<>(workField, false, "successfully removed coordinator");
            }
        }
        return new Response<>(null, true, "user not allowed to remove coordinator");
    }

    public void removeAppointment(String userToRemove) {
        this.appointments.remove(userToRemove);
    }

    public Response<Boolean> transferSupervision(String currSupervisor, String newSupervisor) {
        if (this.state.allowed(Permissions.TRANSFER_SUPERVISION, this) && this.appointments.contains(currSupervisor) && !this.appointments.contains(newSupervisor))
        {
            return new Response<>(true, false, "user allowed to transfer supervision");
        }
        else {
            return new Response<>(false, true, "user not allowed to transfer supervision");
        }
    }

    public List<Integer> getWorkPlanYears() {
        return this.workPlanYears;
    }

    public Response<String> getCoordinator() {
        if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER){
            return new Response<>("", false, "user allowed to get coordinator");
        }
        else if (this.state.allowed(Permissions.GET_COORDINATOR, this))
        {
            return new Response<>(this.workField, false, "user allowed to get coordinator");
        }
        else {
            return new Response<>(null, true, "user not allowed to get coordinator");
        }
    }

    public Response<List<String>> getAllWorkFields() {
        if(this.state.getStateEnum() == UserStateEnum.SYSTEM_MANAGER){
            return new Response<>(this.appointments, false, "user allowed to view all work fields");
        }
        else {
            return new Response<>(null, true, "user not allowed to view all work fields");
        }
    }

    public Response<Boolean> canGenerateReport() {
        if(this.state.allowed(Permissions.FILL_MONTHLY_REPORT, this)){
            return new Response<>(true, false, "user allowed to generate report");
        }
        else {
            return new Response<>(null, true, "user not allowed to generate report");
        }
    }

    public Response<Boolean> canSetWorkingTime() {
        if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR){
            return new Response<>(true, false, "user allowed to update working hours");
        }
        else {
            return new Response<>(null, true, "user not allowed to update working hours");
        }
    }

    public Response<Boolean> editActivity(LocalDateTime date) {
        if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR && this.workPlanYears.contains(extractProperYear(date))){//todo verify it works
            return new Response<>(true, false, "user allowed to update working hours");
        }
        else {
            return new Response<>(null, true, "user not allowed to update working hours");
        }
    }

    public Response<Integer> getInstructorWorkPlan(String instructor, int year, int month) {
        if(this.state.getStateEnum() == UserStateEnum.SUPERVISOR && this.appointments.contains(instructor)){
            return new Response<>(extractProperYear(LocalDateTime.of(year, month, 1, 0, 0)), false, "user allowed to view " + instructor + "'s work plan");
        }
        else {
            return new Response<>(null, true, "user not allowed to view " + instructor + "'s work plan");
        }
    }

    public void assignWorkPlanYear(Integer year) {
        if(!this.workPlanYears.contains(year)){
            workPlanYears.add(year);
        }
    }

    public Response<Integer> addActivity(LocalDateTime startAct, String school) {
        int extractedYear = extractProperYear(startAct);
        if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR && this.workPlanYears.contains(extractedYear) && this.schools.contains(school)){
            return new Response<>(extractedYear, false, "user allowed to add activity");
        }
        else {
            return new Response<>(null, true, "user not allowed to add activity");
        }
    }

    public Response<Boolean> removeActivity(LocalDateTime startAct) {
        if(this.state.getStateEnum() == UserStateEnum.INSTRUCTOR && this.workPlanYears.contains(extractProperYear(startAct))){
            return new Response<>(true, false, "user allowed to add activity");
        }
        else {
            return new Response<>(null, true, "user not allowed to add activity");
        }
    }

    private int extractProperYear(LocalDateTime date){ /* 5/9/2022 --- 4/4/2023 */
        if(date.getMonth().getValue() <= 6){
            return date.getYear() - 1;
        }
        else{
            return date.getYear();
        }
    }
}
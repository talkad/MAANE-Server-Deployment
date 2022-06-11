package Persistence.DbDtos;

import Domain.UsersManagment.User;
import Domain.UsersManagment.UserStateEnum;

import java.time.LocalTime;
import java.util.List;
import java.util.Vector;

public class UserDBDTO {
    protected String username;
    protected UserStateEnum userStateEnum;
    protected String workField;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String phoneNumber;
    protected String city;
    protected String password;
    protected List<String> schools;
    protected List<String> appointments;
    protected List<String> surveys;
    protected List<String> baskets;
    protected int workDay;
    protected LocalTime act1Start;
    protected LocalTime act1End;
    protected LocalTime act2Start;
    protected LocalTime act2End;
    //    private MonthlyReport monthlyReport; //todo monthly reports history??
    protected List<Integer> workPlanYears;

    public UserDBDTO() {}

    public UserDBDTO(User user, String password) {
        this.username = user.getUsername();
        this.userStateEnum = user.getState().getStateEnum();
        this.workField = user.getWorkField();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.city = user.getCity();
        this.password = password;
        this.surveys = user.getSurveys().getResult();
        if(user.getState().getStateEnum() == UserStateEnum.INSTRUCTOR){
            this.workPlanYears = user.getWorkPlanYears();
            this.workDay = user.getWorkDay();
            this.act1Start = user.getAct1Start();
            this.act1End = user.getAct1End();
            this.act2Start = user.getAct2Start();
            this.act2End = user.getAct2End();
        }
    }

    public UserDBDTO(String name, String password, UserStateEnum userStateEnum, String workField){
        this.username = name;
        this.password = password;
        this.userStateEnum = userStateEnum;
        this.appointments = new Vector<>();
        this.schools = new Vector<>();
        this.surveys = new Vector<>();
        this.workField = workField;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStateEnum getStateEnum() {
        return userStateEnum;
    }

    public void setStateEnum(UserStateEnum stateEnum) {
        this.userStateEnum = stateEnum;
    }

    public String getWorkField() {
        return workField;
    }

    public void setWorkField(String workField) {
        this.workField = workField;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getSchools() {
        return schools;
    }

    public void setSchools(List<String> schools) {
        this.schools = schools;
    }

    public List<String> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<String> appointments) {
        this.appointments = appointments;
    }

    public List<String> getSurveys() {
        return surveys;
    }

    public void setSurveys(List<String> surveys) {
        this.surveys = surveys;
    }

    public List<String> getBaskets() {
        return baskets;
    }

    public void setBaskets(List<String> baskets) {
        this.baskets = baskets;
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

    public List<Integer> getWorkPlanYears() {
        return this.workPlanYears;
    }

    public void setWorkPlanYears(List<Integer> years) {
        this.workPlanYears = years;
    }
}

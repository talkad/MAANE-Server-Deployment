package Persistence.DbDtos;

public class SchoolDBDTO {
    String symbol;
    String name;
    String city;
    String city_mail;
    String address;
    String school_address;
    String principal;
    String manager;
    String supervisor;
    String phone;
    String mail;
    int zipcode;
    String education_stage;
    String education_type;
    String supervisor_type;
    String spector;
    int num_of_students;
    String coordinatorFirstName;
    String coordinatorLastName;
    String coordinatorPhone;
    String coordinatorEmail;

    public SchoolDBDTO(String symbol, String name, String city, String city_mail, String address, String school_address, String principal, String manager, String supervisor, String phone, String mail, int zipcode, String education_stage, String education_type, String supervisor_type, String spector, int num_of_students) {
        this.symbol = symbol;
        this.name = name;
        this.city = city;
        this.city_mail = city_mail;
        this.address = address;
        this.school_address = school_address;
        this.principal = principal;
        this.manager = manager;
        this.supervisor = supervisor;
        this.phone = phone;
        this.mail = mail;
        this.zipcode = zipcode;
        this.education_stage = education_stage;
        this.education_type = education_type;
        this.supervisor_type = supervisor_type;
        this.spector = spector;
        this.num_of_students = num_of_students;
    }

    public SchoolDBDTO(){
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCity_mail() {
        return city_mail;
    }

    public String getAddress() {
        return address;
    }

    public String getSchool_address() {
        return school_address;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getManager() {
        return manager;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public String getPhone() {
        return phone;
    }

    public String getMail() {
        return mail;
    }

    public int getZipcode() {
        return zipcode;
    }

    public String getEducation_stage() {
        return education_stage;
    }

    public String getEducation_type() {
        return education_type;
    }

    public String getSupervisor_type() {
        return supervisor_type;
    }

    public String getSpector() {
        return spector;
    }

    public int getNum_of_students() {
        return num_of_students;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCity_mail(String city_mail) {
        this.city_mail = city_mail;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSchool_address(String school_address) {
        this.school_address = school_address;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setZipcode(int zipcode) {
        this.zipcode = zipcode;
    }

    public void setEducation_stage(String education_stage) {
        this.education_stage = education_stage;
    }

    public void setEducation_type(String education_type) {
        this.education_type = education_type;
    }

    public void setSupervisor_type(String supervisor_type) {
        this.supervisor_type = supervisor_type;
    }

    public void setSpector(String spector) {
        this.spector = spector;
    }

    public void setNum_of_students(int num_of_students) {
        this.num_of_students = num_of_students;
    }

    public String getCoordinatorFirstName() {
        return coordinatorFirstName;
    }

    public void setCoordinatorFirstName(String coordinatorFirstName) {
        this.coordinatorFirstName = coordinatorFirstName;
    }

    public String getCoordinatorLastName() {
        return coordinatorLastName;
    }

    public void setCoordinatorLastName(String coordinatorLastName) {
        this.coordinatorLastName = coordinatorLastName;
    }

    public String getCoordinatorPhone() {
        return coordinatorPhone;
    }

    public void setCoordinatorPhone(String coordinatorPhone) {
        this.coordinatorPhone = coordinatorPhone;
    }

    public String getCoordinatorEmail() {
        return coordinatorEmail;
    }

    public void setCoordinatorEmail(String coordinatorEmail) {
        this.coordinatorEmail = coordinatorEmail;
    }
}

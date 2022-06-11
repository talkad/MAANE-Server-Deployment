package Domain.UsersManagment;

public enum UserStateEnum {
    GUEST("GUEST"),
    REGISTERED("REGISTERED"),
    INSTRUCTOR("INSTRUCTOR"),
    SUPERVISOR("SUPERVISOR"),
    COORDINATOR("COORDINATOR"),
    GENERAL_SUPERVISOR("GENERAL_SUPERVISOR"),
    SYSTEM_MANAGER("SYSTEM_MANAGER");

    private final String state;

    UserStateEnum(String type){
        this.state = type;
    }

    public String getState(){
        return this.state;
    }
}

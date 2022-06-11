package Domain.UsersManagment;


public class SystemManager extends Registered{
    public SystemManager() {
        super();
        allowedFunctions.add(Permissions.REGISTER_SUPERVISOR);
        allowedFunctions.add(Permissions.REMOVE_USER);
        allowedFunctions.add(Permissions.REGISTER_USER);
        allowedFunctions.add(Permissions.CHANGE_PASSWORD_TO_USER);
        allowedFunctions.add(Permissions.VIEW_USERS_INFO);//todo maybe remove
        allowedFunctions.add(Permissions.VIEW_ALL_USERS_INFO);
        allowedFunctions.add(Permissions.REGISTER_BY_ADMIN);//todo maybe useless
        allowedFunctions.add(Permissions.REGISTER_COORDINATOR);
        allowedFunctions.add(Permissions.REMOVE_COORDINATOR);
        allowedFunctions.add(Permissions.TRANSFER_SUPERVISION);
    }

    @Override
    public boolean allowed(Permissions func, User user) {
        return this.allowedFunctions.contains(func);
    }

    @Override
    public boolean allowed(Permissions permission, User user, String schoolId) {
        return user.getSchools().contains(schoolId);
    }

    @Override
    public UserStateEnum getStateEnum(){
        return UserStateEnum.SYSTEM_MANAGER;
    }

}

package Domain.UsersManagment;


public class Instructor extends Registered{

    public Instructor() {
        super();
        this.allowedFunctions.add(Permissions.VIEW_WORK_PLAN);
        this.allowedFunctions.add(Permissions.ADD_BASKET);
        this.allowedFunctions.add(Permissions.REMOVE_BASKET);
        this.allowedFunctions.add(Permissions.REGISTER_COORDINATOR);
        this.allowedFunctions.add(Permissions.REMOVE_COORDINATOR);
        this.allowedFunctions.add(Permissions.GET_COORDINATOR);
        this.allowedFunctions.add(Permissions.FILL_MONTHLY_REPORT);
        this.allowedFunctions.add(Permissions.GET_GOALS);
    }


    @Override
    public boolean allowed(Permissions func, User user) {
        return this.allowedFunctions.contains(func);
    }

    @Override
    public boolean allowed(Permissions func, User user, String schoolId) {
        return this.allowedFunctions.contains(func);
    }

    @Override
    public UserStateEnum getStateEnum() {
        return UserStateEnum.INSTRUCTOR;
    }
}

package Domain.UsersManagment;

public abstract class UserState {

    public abstract boolean allowed(Permissions func, User user);

    public abstract boolean allowed(Permissions func, User user, String schoolId);

    public abstract UserStateEnum getStateEnum();

}

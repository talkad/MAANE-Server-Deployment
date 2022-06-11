package Domain.UsersManagment;

public class Coordinator extends UserState {

    @Override
    public boolean allowed(Permissions func, User user) {
        return false;
    }

    @Override
    public boolean allowed(Permissions func, User user, String schoolId) {
        return false;
    }

    @Override
    public UserStateEnum getStateEnum() {
        return UserStateEnum.COORDINATOR;
    }
}

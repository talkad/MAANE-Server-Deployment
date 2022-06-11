package Domain.UsersManagment;


import java.util.List;
import java.util.Vector;

public class Guest extends UserState{
    private final List<Permissions> allowedFunctions;

    public Guest(){
        this.allowedFunctions = new Vector<>();
        this.allowedFunctions.add(Permissions.LOGIN);
    }

    @Override
    public boolean allowed(Permissions func, User user) {
        return this.allowedFunctions.contains(func);
    }

    @Override
    public boolean allowed(Permissions func, User user, String schoolId) {
        return false;
    }

    @Override
    public UserStateEnum getStateEnum() {
        return UserStateEnum.GUEST;
    }
}

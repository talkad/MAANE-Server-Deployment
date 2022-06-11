package Domain.UsersManagment;

import java.util.List;
import java.util.Vector;

public class Registered extends UserState {

    protected final List<Permissions> allowedFunctions;

    public Registered() {
        this.allowedFunctions = new Vector<>();
        this.allowedFunctions.add(Permissions.LOGOUT);
        this.allowedFunctions.add(Permissions.CHANGE_PASSWORD);
        this.allowedFunctions.add(Permissions.UPDATE_INFO);
        this.allowedFunctions.add(Permissions.VIEW_INFO);
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
        return UserStateEnum.REGISTERED;
    }
}

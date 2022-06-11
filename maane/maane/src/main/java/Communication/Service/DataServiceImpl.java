package Communication.Service;

import Communication.Service.Interfaces.DataService;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.DataController;
import Persistence.DbDtos.SchoolDBDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class DataServiceImpl implements DataService {

    private static class CreateSafeThreadSingleton {
        private static final DataServiceImpl INSTANCE = new DataServiceImpl();
    }

    public static DataServiceImpl getInstance() {
        return CreateSafeThreadSingleton.INSTANCE;
    }

    @Override
    public Response<Boolean> assignCoordinator(String currUser, String workField, String firstName, String lastName, String email, String phoneNumber, String school) {
        Response<Boolean> res = DataController.getInstance().assignCoordinator(currUser, workField, firstName, lastName, email, phoneNumber, school);

        if (res.isFailure())
            log.error("failed to assign the coordinator {} to the school {} by {}", firstName + " " + lastName, school, currUser);
        else
            log.info("successfully assigned the coordinator {} to the school {} by {}", firstName + " " + lastName, school, currUser);
        return res;
    }

    @Override
    public Response<Boolean> removeCoordinator(String currUser, String workField, String school) {
        Response<Boolean> res = DataController.getInstance().removeCoordinator(currUser, workField, school);

        if (res.isFailure())
            log.error("failed to remove the {} coordinator from the school {} by {}", workField, school, currUser);
        else
            log.info("successfully removed the {} coordinator from the school {} by {}", workField, school, currUser);
        return res;
    }

    @Override
    public Response<Boolean> insertSchool(SchoolDBDTO school) {
        Response<Boolean> res = DataController.getInstance().insertSchool(school);

        if (res.isFailure())
            log.error("failed to insert school {} ", school.getName());
        else
            log.info("inserted school {} successfully",school.getName());

        return res;
    }

    @Override
    public Response<Boolean> removeSchool(String symbol) {
        Response<Boolean> res = DataController.getInstance().removeSchool(symbol);

        if (res.isFailure())
            log.error("failed to remove the school associated with symbol {} ", symbol);
        else
            log.info("removed school {} successfully",symbol);

        return res;
    }

    @Override
    public Response<Boolean> updateSchool(String symbol, SchoolDBDTO school) {
        Response<Boolean> res = DataController.getInstance().updateSchool(symbol, school);

        if (res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<SchoolDBDTO> getSchool(String username, String symbol) {
        Response<SchoolDBDTO> res = DataController.getInstance().getSchool(username, symbol);

        if (res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<List<Pair<String, String>>> getUserSchools(String username) {
        Response<List<Pair<String, String>>> res = DataController.getInstance().getUserSchools(username);

        if (res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<Boolean> resetDB() {
        Response<Boolean> res = DataController.getInstance().resetDB();

        if (res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<Boolean> removeCoordinatorTester(String school) {
        Response<Boolean> res = DataController.getInstance().removeCoordinatorTester(school);

        if (res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<Boolean> assignCoordinatorTester(String school) {
        Response<Boolean> res = DataController.getInstance().assignCoordinatorTester(school);

        if (res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

    @Override
    public Response<Boolean> loadSchoolsToDB() {
        Response<Boolean> res = DataController.getInstance().loadSchoolsToDB();

        if (res.isFailure())
            log.error(res.getErrMsg());
        else
            log.info(res.getErrMsg());

        return res;
    }

}
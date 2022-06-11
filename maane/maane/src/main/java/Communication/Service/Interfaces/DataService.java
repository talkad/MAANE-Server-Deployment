package Communication.Service.Interfaces;

import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Persistence.DbDtos.SchoolDBDTO;

import java.util.List;

public interface DataService {

    Response<Boolean> assignCoordinator(String currUser, String workField, String firstName, String lastName, String email, String phoneNumber, String school);

    Response<Boolean> removeCoordinator(String currUser, String workField, String school);

    Response<Boolean> insertSchool (SchoolDBDTO school);

    Response<Boolean> removeSchool (String symbol);

    Response<Boolean> updateSchool (String symbol, SchoolDBDTO school);

    Response<SchoolDBDTO> getSchool(String username, String symbol);

    Response<List<Pair<String, String>>> getUserSchools(String username);

    Response<Boolean> resetDB();

    Response<Boolean> removeCoordinatorTester(String school);

    Response<Boolean> assignCoordinatorTester(String school);

    Response<Boolean> loadSchoolsToDB();

}
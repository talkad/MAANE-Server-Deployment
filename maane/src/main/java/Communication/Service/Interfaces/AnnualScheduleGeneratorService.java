package Communication.Service.Interfaces;

import Domain.CommonClasses.Response;

public interface AnnualScheduleGeneratorService {
    public Response<Boolean> generateSchedule(String supervisor, String surveyId);

}

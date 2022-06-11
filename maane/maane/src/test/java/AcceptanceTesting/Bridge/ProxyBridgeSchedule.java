package AcceptanceTesting.Bridge;

import Communication.Service.Interfaces.AnnualScheduleGeneratorService;
import Domain.CommonClasses.Response;

public class ProxyBridgeSchedule implements AnnualScheduleGeneratorService {

    private AnnualScheduleGeneratorService real;

    public ProxyBridgeSchedule(){
        real = null;
    }

    public void setRealBridge(AnnualScheduleGeneratorService implementation) {
        if(real == null){
            real = implementation;
        }
    }
    @Override
    public Response<Boolean> generateSchedule(String supervisor, String surveyId) {

        if (real != null){
            return real.generateSchedule(supervisor, surveyId);
        }

        return new Response<>(null, true, "not implemented");
    }
}

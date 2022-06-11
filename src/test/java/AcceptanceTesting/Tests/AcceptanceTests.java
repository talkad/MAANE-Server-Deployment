package AcceptanceTesting.Tests;

import AcceptanceTesting.Bridge.Driver;
import Communication.Service.Interfaces.AnnualScheduleGeneratorService;
import Communication.Service.Interfaces.DataService;
import Communication.Service.Interfaces.SurveyService;
import Communication.Service.Interfaces.UserService;

public abstract class AcceptanceTests {

    protected static UserService userBridge;
    protected static SurveyService surveyBridge;
    protected static AnnualScheduleGeneratorService scheduleBridge;
    protected static DataService dataBridge;


    public void setUp(boolean toInit) {
        if (toInit) {
            userBridge = Driver.getUserBridge();
            surveyBridge = Driver.getSurveyBridge();
            scheduleBridge = Driver.getScheduleBridge();
            dataBridge = Driver.getDataBridge();

        }
    }
}

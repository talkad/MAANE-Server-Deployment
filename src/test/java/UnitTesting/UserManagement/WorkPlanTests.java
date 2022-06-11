package UnitTesting.UserManagement;

import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.WorkPlan.Activity;
import Domain.WorkPlan.Goal;
import Domain.WorkPlan.WorkPlan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class WorkPlanTests {
    WorkPlan workPlan;
    Goal goal1;
    Goal goal2;

    @Before
    public void setUp(){
        ServerContextInitializer.getInstance().setMockMode();
        ServerContextInitializer.getInstance().setTestMode();

        workPlan = new WorkPlan(2022, 4, LocalTime.of(0, 0), LocalTime.of(2, 0), LocalTime.of(2, 0), LocalTime.of(4, 0));
        goal1 = new Goal(1, "a", "aa", 1, 1, "tech", 2022);
        goal2 = new Goal(2, "b", "bb", 2, 2, "tech", 2022);
        goal2 = new Goal(3, "c", "cc", 3, 3, "tech", 2022);
    }

    @Test
    public void addOneSchoolsSuccess(){
        Response<Boolean> res = workPlan.insertActivityEveryWeek(new Pair<>("school1", goal1));
        Assert.assertFalse(res.isFailure());

        Activity activity = workPlan.getCalendar().get(LocalDateTime.of(2022, 9, 1, 0, 0));
        Assert.assertEquals("school1", activity.getSchool());
    }

    @Test
    public void addTwoSchoolsSuccess(){
        Response<Boolean> res = workPlan.insertActivityEveryWeek(new Pair<>("school2", goal1), new Pair<>("school3", goal2));
        Assert.assertFalse(res.isFailure());
    }

    @Test
    public void EveryWeeksSuccess(){
        workPlan = new WorkPlan(2022, 4, LocalTime.of(0, 0), LocalTime.of(2, 0), LocalTime.of(2, 0), LocalTime.of(4, 0));
        Goal goal = new Goal(1, "a", "a", 1, 1, "a", 2022);
        Pair<String, Goal> pair = new Pair<>("a", goal);
        workPlan.insertActivityEveryWeek(pair);

        Goal goal2 = new Goal(2, "b", "b", 2, 2, "b", 2022);
        Pair<String, Goal> pair2 = new Pair<>("b", goal2);
        workPlan.insertActivityEveryWeek(pair2);
        workPlan.printMe();
    }
}

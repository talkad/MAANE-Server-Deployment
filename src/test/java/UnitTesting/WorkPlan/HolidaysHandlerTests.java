package UnitTesting.WorkPlan;
import Communication.DTOs.ActivityDTO;
import Communication.Initializer.ServerContextInitializer;
import Domain.CommonClasses.Pair;
import Domain.WorkPlan.Goal;
import Domain.WorkPlan.HolidaysHandler;
import Domain.WorkPlan.WorkPlan;
import Persistence.HolidaysQueries;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HolidaysHandlerTests {

    private HolidaysHandler holidaysHandler;

    @Before
    public void setup(){
        ServerContextInitializer.getInstance().setMockMode();
        //HolidaysQueries.getInstance().clearHolidays();
        holidaysHandler = new HolidaysHandler(2022);
    }

    @Test
    public void checkGetAsActivity(){
        List<Pair<LocalDateTime, ActivityDTO>> check = holidaysHandler.getHolidaysAsActivity(2022, 4).getResult();
        Assert.assertEquals(8, check.size());
    }

    @Test
    public void checkInsert() {
        ArrayList<String[]> holidays = holidaysHandler.getHolidaysForYear(2022);
        for (String[] entry : holidays) {
            System.out.println(entry[0] + " on date: " + entry[1]);
        }
    }

    @Test
    public void checkHolidayExists() {
        LocalDateTime localDateTime = LocalDateTime.of(2022, 4, 18, 0, 0);
        Assert.assertTrue(holidaysHandler.isHoliday(localDateTime));

        LocalDateTime localDateTime2 = LocalDateTime.of(2022, 11, 1, 0, 0);
        Assert.assertFalse(holidaysHandler.isHoliday(localDateTime2));
    }

    @Test
    public void checkNoHolidayCollision() {
        WorkPlan workPlan = new WorkPlan(2022, 2, LocalTime.of(8, 0), LocalTime.of(10, 0),LocalTime.of(10, 0),LocalTime.of(12, 0));
        Goal goal1 = new Goal(1, "a", "aa", 1, 1, "tech", 2022);
        Goal goal2 = new Goal(2, "b", "bb", 1, 1, "tech", 2022);

        Pair<String, Goal> pair = new Pair<>("a", goal1);
        Pair<String, Goal> pair2 = new Pair<>("b", goal2);

        for (int i = 0; i < 6; i++) {
            workPlan.insertActivityEveryWeek(pair, pair2);
        }
        //workPlan.printMe();
        Assert.assertNotNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 9, 6, 8, 0)));
        Assert.assertNotNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 9, 13, 8, 0)));
        Assert.assertNotNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 9, 20, 8, 0)));

        Assert.assertNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 9, 27, 8, 0))); //Rosh Hashana
        Assert.assertNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 10, 4, 8, 0))); // Yom Kipur
        Assert.assertNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 10, 11, 8, 0))); // Sukot

        Assert.assertNotNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 10, 18, 8, 0)));
        Assert.assertNotNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 10, 25, 8, 0)));
        Assert.assertNotNull(workPlan.getCalendar().get(LocalDateTime.of(2022, 11, 1, 8, 0)));
    }

    @Test
    public void checkTimeout() { //change to bad Url at holiday handler first
        HolidaysHandler holidaysHandler = new HolidaysHandler(2027);
        HolidaysQueries holidaysQueries = new HolidaysQueries();
        Assert.assertFalse(holidaysQueries.holidaysForYearExists(2027));
    }
}

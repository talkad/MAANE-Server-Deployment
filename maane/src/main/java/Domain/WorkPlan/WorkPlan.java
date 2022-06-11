package Domain.WorkPlan;

import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;
import static java.time.temporal.TemporalAdjusters.firstInMonth;


/**
 * Represents the annual schedule of some user
 * it builds and holds the calendar with all dates and their activities
 *
 */

public class WorkPlan {
    protected TreeMap<LocalDateTime, Activity> calendar; //Date and his activities for each day of year
    protected int year;
    protected LocalDateTime currDateToInsert;
    protected String username;
    protected int workDay;
    protected LocalTime act1Start;
    protected LocalTime act1End;
    protected LocalTime act2Start;
    protected LocalTime act2End;
    protected HolidaysHandler holidaysHandler;

    public WorkPlan(int year, int workDay, LocalTime act1Start, LocalTime act1End, LocalTime act2Start, LocalTime act2End) {
        this.year = year;
        this.workDay = workDay;
        this.act1Start = act1Start;
        this.act1End = act1End;
        this.act2Start = act2Start;
        this.act2End = act2End;
        this.currDateToInsert = findFirstWorkDayOfMonth(workDay, year);//LocalDate.of(year, 9, 1).atTime(0, 0);
        this.calendar = GenerateCalendarForYear(year);
        checkFridaySaturday();
        this.holidaysHandler = new HolidaysHandler(year);
    }

    private LocalDateTime findFirstWorkDayOfMonth(int workDay, int year) {
        switch (workDay){
            case 0: {
                return LocalDate.of(year, 9, 1).with(firstInMonth(DayOfWeek.SUNDAY)).atTime(act1Start);
            }
            case 1: {
                return LocalDate.of(year, 9, 1).with(firstInMonth(DayOfWeek.MONDAY)).atTime(act1Start);
            }
            case 2: {
                return LocalDate.of(year, 9, 1).with(firstInMonth(DayOfWeek.TUESDAY)).atTime(act1Start);
            }
            case 3: {
                return LocalDate.of(year, 9, 1).with(firstInMonth(DayOfWeek.WEDNESDAY)).atTime(act1Start);
            }
            case 4: {
                return LocalDate.of(year, 9, 1).with(firstInMonth(DayOfWeek.THURSDAY)).atTime(act1Start);
            }
            case 5: {
                return LocalDate.of(year, 9, 1).with(firstInMonth(DayOfWeek.FRIDAY)).atTime(act1Start);
            }
            case 6: {
                return LocalDate.of(year, 9, 1).with(firstInMonth(DayOfWeek.SATURDAY)).atTime(act1Start);
            }
            default: return null;
        }
    }

    public TreeMap<LocalDateTime, Activity> getCalendar() {
        return this.calendar;
    }

    /*
    Inserts 2 goals from 2 schools for same date
     */
    /*public Response<Boolean> insertActivityToFirstAvailableDate(Pair<String, Goal> input1, Pair<String, Goal> input2) {
        Activity firstActivity = new Activity(input1.getFirst(), input1.getSecond().getGoalId(), input1.getSecond().getTitle());
        Activity secondActivity = new Activity(input2.getFirst(), input2.getSecond().getGoalId(), input2.getSecond().getTitle());//todo isnt it goald id?

        LocalDateTime freeDate = findDate();
        if (freeDate == null) //no free date
            return new Response<>(false, true, "no free days");//todo not actually failed check its ok
        insertActivity(freeDate, firstActivity);

        LocalDateTime freeDate2 = findDate();
        if (freeDate2 == null) //no free date
            return new Response<>(false, true, "no free days");//todo not actually failed check its ok
        insertActivity(freeDate2, secondActivity);

        return new Response<>(false, false, "Success");
    }
*/
    /*
    Gets a pair of <School name, Goal> and insert it to the first available date (not friday/saturday)
     */
/*    public Response<Boolean> insertActivityToFirstAvailableDate(Pair<String, Goal> input) {
        Activity activity = new Activity(input.getFirst(), input.getSecond().getGoalId(), input.getSecond().getTitle());
        LocalDateTime freeDate = findDate();

        if (freeDate == null) //no free date
            return new Response<>(false, true, "no free days");//todo not actually failed check its ok

        insertActivity(freeDate, activity);
        return new Response<>(false, false, "Success");
    }*/

    private void insertActivity(LocalDateTime date, Activity activity) {
        //if(!holidaysHandler.dateHasHoliday(date))
        this.calendar.put(date, activity);
    }

    // find the next avaliable date
    private LocalDateTime findDate() {
        //LocalDateTime localDateTime = calendar.keySet().iterator().next();
        LocalDateTime freeDate = null;

        for (LocalDateTime date : calendar.keySet()) {

            if (calendar.get(date) == null) {
                boolean isFridayOrSaturday = date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY;
                if (!isFridayOrSaturday) {
                    freeDate = date;
                    break;
                }
            }
        }
        return freeDate;
    }

    public void printMe() {
        for (LocalDateTime key : calendar.keySet()) {
            if(calendar.get(key) != null){
                System.out.println("Date: " + key);
                System.out.println("==> activity " + calendar.get(key).getTitle() + " scheduled for school " + calendar.get(key).getSchool());
            }
        }
    }

    private String addZeroIfNeeded(String number) {
        return number.length() == 1 ? "0" + number : number;
    }

    //Generate the dates
    private List<LocalDateTime> generateDatesForYear(int year) {

        LocalDate startDate = LocalDate.of(year, 9, 1);
        LocalDate endDate = LocalDate.of(year + 1, 6, 21);

        List<LocalDate> localDates = startDate.datesUntil(endDate).collect(Collectors.toList());
        List<LocalDateTime> localDateTimes = new Vector<>();
        for (LocalDate localDate : localDates) {
            localDateTimes.add(localDate.atTime(this.act1Start));
            localDateTimes.add(localDate.atTime(this.act2Start));
        }
        return localDateTimes;
    }

    private TreeMap<LocalDateTime, Activity> GenerateCalendarForYear(int year) {
        List<LocalDateTime> datesForYear = generateDatesForYear(year);
        calendar = new TreeMap<>();
        for (LocalDateTime day : datesForYear) {
            calendar.put(day, null);
        }
        return calendar;
    }

//    public Response<Boolean> insertActivityEveryWeek(Pair<String, Goal> input) {
//        Activity activity = new Activity(input.getFirst(), input.getSecond().getTitle());
//        LocalDateTime freeDate = findDate();
//
//        if (freeDate == null) //no free date
//            return new Response<>(false, true, "no free days");//todo not actually failed check its ok
//
//        LocalDateTime lastDay = LocalDateTime.of(this.year + 1, 6, 21, 0, 0);
//        while (freeDate.isBefore(lastDay)) {
//            insertActivity(freeDate, activity);
//            freeDate = freeDate.plusWeeks(1);
//        }
//
//        return new Response<>(false, false, "Success");
//    }
//
//    public Response<Boolean> insertActivityEveryWeek(Pair<String, Goal> input1, Pair<String, Goal> input2) {
//        Activity firstActivity = new Activity(input1.getFirst(), input1.getSecond().getTitle());
//        Activity secondActivity = new Activity(input2.getFirst(), input2.getSecond().getTitle());
//        LocalDateTime lastDay = LocalDateTime.of(this.year + 1, 6, 21, 0, 0);
//
//        LocalDateTime freeDate1 = findDate();
//        if (freeDate1 == null) //no free date
//            return new Response<>(false, true, "no free days");//todo not actually failed check its ok
//
//        while (freeDate1.isBefore(lastDay)){
//            insertActivity(freeDate1, firstActivity);
//            freeDate1 = freeDate1.plusWeeks(1);
//        }
//
//        LocalDateTime freeDate2 = findDate();
//        if (freeDate2 == null) //no free date
//            return new Response<>(false, true, "no free days");//todo not actually failed check its ok
//        freeDate2 = freeDate2.toLocalDate().atStartOfDay().plusHours(1);
//
//        while (freeDate2.isBefore(lastDay)){
//            insertActivity(freeDate2, secondActivity);
//            freeDate2 = freeDate2.plusWeeks(1);
//        }
//
//        return new Response<>(false, false, "Success");
//    }

    public Response<Boolean> insertActivityEveryWeek(Pair<String, Goal> input) {
        Activity activity = new Activity(input.getFirst(), input.getSecond().getGoalId(), input.getSecond().getTitle());
        LocalDateTime lastDay = LocalDateTime.of(this.year + 1, 6, 21, 0, 0);

        if (lastDay.isBefore(currDateToInsert)) //no free date
            return new Response<>(false, true, "no free days");//todo not actually failed check its ok

        while(holidaysHandler.isHoliday(currDateToInsert)){
            currDateToInsert = currDateToInsert.plusWeeks(1);
            if (lastDay.isBefore(currDateToInsert)) { //no free date
                return new Response<>(false, true, "no free days");//todo not actually failed check its ok
            }
        }
        activity.setEndActivity(currDateToInsert.toLocalDate().atTime(this.act1End));
        insertActivity(currDateToInsert, activity);
        currDateToInsert = currDateToInsert.plusWeeks(1);

        return new Response<>(false, false, "Success");
    }

    public Response<Boolean> insertActivityEveryWeek(Pair<String, Goal> input1, Pair<String, Goal> input2) {
        Activity firstActivity = new Activity(input1.getFirst(), input1.getSecond().getGoalId(), input1.getSecond().getTitle());
        Activity secondActivity = new Activity(input2.getFirst(), input2.getSecond().getGoalId(), input2.getSecond().getTitle());
        LocalDateTime lastDay = LocalDateTime.of(this.year + 1, 6, 21, 0, 0);
        if (lastDay.isBefore(currDateToInsert)) { //no free date
            return new Response<>(false, true, "no free days");//todo not actually failed check its ok
        }

        while(holidaysHandler.isHoliday(currDateToInsert)){
            currDateToInsert = currDateToInsert.plusWeeks(1);
            if (lastDay.isBefore(currDateToInsert)) { //no free date
                return new Response<>(false, true, "no free days");//todo not actually failed check its ok
            }
        }

        firstActivity.setEndActivity(currDateToInsert.toLocalDate().atTime(act1End));
        insertActivity(currDateToInsert, firstActivity);
        secondActivity.setEndActivity(currDateToInsert.toLocalDate().atTime(this.act2End));
        insertActivity(currDateToInsert.toLocalDate().atTime(this.act2Start), secondActivity);
        currDateToInsert = currDateToInsert.plusWeeks(1);

        return new Response<>(false, false, "Success");
    }

    private void checkFridaySaturday(){
        if(currDateToInsert.getDayOfWeek() == DayOfWeek.FRIDAY)
            currDateToInsert = currDateToInsert.plusDays(2);
        if(currDateToInsert.getDayOfWeek() == DayOfWeek.SATURDAY)
            currDateToInsert = currDateToInsert.plusDays(1);
    }
}
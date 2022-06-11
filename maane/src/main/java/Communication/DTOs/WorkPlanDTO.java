package Communication.DTOs;

import Domain.CommonClasses.Pair;
import Domain.WorkPlan.Activity;
import Domain.WorkPlan.WorkPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class WorkPlanDTO {
    private List<Pair<LocalDateTime, ActivityDTO>> calendar; //List of <Date String, List of activities for that day

    public WorkPlanDTO(WorkPlan workPlan){ //TreeMap <LocalDateTime, Activity>
        List<Pair<LocalDateTime, ActivityDTO>> calendar = new Vector<>();
        for (Map.Entry<LocalDateTime, Activity> oldEntry : workPlan.getCalendar().entrySet()) {
            if(oldEntry.getValue() != null) {
                calendar.add(new Pair<>(oldEntry.getKey(), new ActivityDTO(oldEntry.getValue().getSchool(), oldEntry.getValue().getGoalId(), oldEntry.getValue().getTitle(), oldEntry.getValue().getEndActivity())));
            }
        }
        this.calendar = calendar;
    }

    public List<Pair<LocalDateTime, ActivityDTO>> getCalendar(){
        return calendar;
    }

    public void printMe(){
        for (Pair<LocalDateTime, ActivityDTO> pair : calendar){
            System.out.println("Date: " + pair.getFirst() + " SchoolId: " + pair.getSecond().getSchoolId() + " GoalId: " + pair.getSecond().getGoalId() + " Title: " + pair.getSecond().getTitle());
        }
    }
}

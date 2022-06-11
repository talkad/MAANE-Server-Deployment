package Communication.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActivityManageDTO {
    String startActivity;
    String schoolId;
    Integer goalId;
    String title;
    String endActivity;

/*    public String toString (){
        return "School: " + schoolId + "GoalId: " + goalId + " Title: " + title;
    }*/

}

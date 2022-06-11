package Communication.DTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class GoalDTO {
    private int goalId;
    private String title;
    private String description;
    private int quarterly;
    private int weight;
    private String workField;
    private Integer year;

    public GoalDTO(int goalId, String title, String description, int quarterly, int weight) {
        this.goalId = goalId;
        this.title = title;
        this.description = description;
        this.quarterly = quarterly;
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getQuarterly() {
        return quarterly;
    }

    public void setQuarterly(int quarterly) {
        this.quarterly = quarterly;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getWorkField() {
        return workField;
    }

    public void setWorkField(String workField) {
        this.workField = workField;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}



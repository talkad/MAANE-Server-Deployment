package Domain.WorkPlan;

import Communication.DTOs.GoalDTO;

public class Goal {
    private int goalId;
    private String title;
    private String description;
    private int quarterly;
    private int weight;
    private String workField;
    private Integer year;

    public Goal(int goalId, String title, String description, int quarterly, int weight, String workField, Integer year) {
        this.goalId = goalId;
        this.title = title;
        this.description = description;
        this.quarterly = quarterly;
        this.weight = weight;
        this.workField = workField;
        this.year = year;
    }

    public Goal(GoalDTO gDTO) {
        this.goalId = gDTO.getGoalId();
        this.title = gDTO.getTitle();
        this.description = gDTO.getDescription();
        this.quarterly = gDTO.getQuarterly();
        this.weight = gDTO.getWeight();
        this.workField = gDTO.getWorkField();
        this.year = gDTO.getYear();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public int getQuarterly() {
        return quarterly;
    }

    public void setQuarterly(int quarterly) {
        this.quarterly = quarterly;
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

    public String toString(){
        return /*"goal id: " + this.goalId + */" title: " + this.title + /*" description: " + this.description +*/ " weight:  " + this.weight;
    }
}

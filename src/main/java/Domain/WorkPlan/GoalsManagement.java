package Domain.WorkPlan;


import Communication.DTOs.GoalDTO;
import Domain.CommonClasses.Response;
import Persistence.GoalsQueries;

import java.util.List;
import java.util.Vector;

public class GoalsManagement {
    private GoalsQueries goalsDAO;

    private GoalsManagement() {
        this.goalsDAO = GoalsQueries.getInstance();

    }

    private static class CreateSafeThreadSingleton {
        private static final GoalsManagement INSTANCE = new GoalsManagement();
    }

    public static GoalsManagement getInstance() {
        return CreateSafeThreadSingleton.INSTANCE;
    }

    public Response<Boolean> addGoalToField(String workField, GoalDTO goalDTO, Integer year){
        goalDTO.setWorkField(workField);
        goalDTO.setYear(year);
        return goalsDAO.insertGoal(goalDTO);
    }

    public Response<List<Goal>> getGoals(String workField, Integer year){
        Response<List<GoalDTO>> goalsDTOListRes = goalsDAO.getGoals(workField, year);
        if(!goalsDTOListRes.isFailure()){
            List<Goal> goalList = goalsDTOToGoals(goalsDTOListRes.getResult());
            return new Response<>(goalList, false, "successfully acquired goals from the work field: " + workField);
        }
        else{
            return new Response<>(null, true, goalsDTOListRes.getErrMsg());
        }
    }

    public Response<List<GoalDTO>> getGoalsDTO(String workField, Integer year){
        return goalsDAO.getGoals(workField, year);
    }

    public Response<List<Goal>> getGoalsById(List<Integer> goalsId){
        List<Goal> goalsList;
        Response<List<GoalDTO>> goalDTOListRes = goalsDAO.getGoalsById(goalsId);
        if(!goalDTOListRes.isFailure()){
            goalsList = goalsDTOToGoals(goalDTOListRes.getResult());
            return new Response<>(goalsList, false, "all goals found");
        }
        else{
            return new Response<>(null, true, "no such goal exists");
        }
    }

    public Response<Goal> getGoalTById(int goalID){
        List<Goal> goalsList;
        Response<List<GoalDTO>> goalDTOListRes = goalsDAO.getGoalsById(List.of(goalID));

        if(!goalDTOListRes.isFailure()){
            goalsList = goalsDTOToGoals(goalDTOListRes.getResult());

            if(goalsList.size() == 1)
                return new Response<>(goalsList.get(0), false, "goal found");

            return new Response<>(null, true, "more then one goal found");
        }
        else{
            return new Response<>(null, true, "no such goal exists");
        }
    }

    public Response<Boolean> removeGoal(String workField, Integer year, int goalId){
        return goalsDAO.removeGoal(goalId);
    }

    public List<Goal> goalsDTOToGoals(List<GoalDTO> goalDTOList){
        List<Goal> goalList = new Vector<>();
        for (GoalDTO g: goalDTOList) {
            goalList.add(new Goal(g));
        }
        return goalList;
    }

    public void clearGoals(){
        goalsDAO.deleteGoals();
    }
}

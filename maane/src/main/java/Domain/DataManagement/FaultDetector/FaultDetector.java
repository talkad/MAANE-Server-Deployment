package Domain.DataManagement.FaultDetector;

import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Domain.DataManagement.FaultDetector.Rules.Rule;
import Domain.DataManagement.SurveyAnswers;
import Domain.WorkPlan.Goal;
import Domain.WorkPlan.GoalsManagement;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FaultDetector {

    // the index of the rule is the index in list
    private List<Pair<Rule, Integer>> rules;

    public FaultDetector() {
        this.rules = new LinkedList<>();
    }

    public FaultDetector(List<Pair<Rule, Integer>> rules) {
        this.rules = rules;
    }

    /**
     * add rule to current detector
     * @param rule to be added
     * @param goalID the goal this rule enforcing
     * @return successful response
     */
    public Response<Boolean> addRule(Rule rule, int goalID){
        rules.add(new Pair<>(rule, goalID));
        
        return new Response<>(true, false, "rule added successfully");
    }

    /**
     * remove rule from current detector
     * @param index of the desired rule to be removed
     * @return successful response if the ruleID exists. failure otherwise
     */
    public Response<Boolean> removeRule(int index){

        if(index >= rules.size())
            return new Response<>(false, true, "index out of bounds");

        rules.remove(index);
        return new Response<>(true, false, "rule removed successfully");
    }


    /**
     * detect all faults in a given answers
     * @param answers on certain survey
     * @return list of all goals that were not consistent with the given rules
     */
    public Response<List<Integer>> detectFault(SurveyAnswers answers){
        List<Integer> faults = new LinkedList<>();

        for(Pair<Rule, Integer> rule: rules){
            if(rule.getFirst().apply(answers))
                faults.add(rule.getSecond());
        }
        return new Response<>(faults, false, "details");
    }

    /**
     * detect all faults in a given answers
     * @param answers on certain survey
     * @return list of all goals that were not consistent with the given rules
     */
    public Response<Map<Integer, List<String>>> detectFaultTitles(SurveyAnswers answers){
        Map<Integer, List<String>> faults = new ConcurrentHashMap<>();
        List<Integer> queIDs = null;

        Goal goal = null;

        for(Pair<Rule, Integer> rule: rules){
            if(rule.getFirst().apply(answers)) {
                queIDs = rule.getFirst().getQuestionIndex();
                goal = GoalsManagement.getInstance().getGoalTById(rule.getSecond()).getResult();

                if (goal != null && queIDs != null) {
                    for (Integer queID : queIDs) {

                        if (!faults.containsKey(queID))
                            faults.put(queID, new LinkedList<>());

                        List<String> goals = faults.get(queID);
                        goals.add(goal.getTitle());
                        faults.put(queID, goals);
                    }
                }
            }
        }

        return new Response<>(faults, false, "details");
    }

    /**
     * get the illegal answers IDs
     * @param answers answers on certain survey
     * @return list of booleans indicates the legality of answers
     */
    public Response<List<Boolean>> getIllegalQuestionID(SurveyAnswers answers){
        List<Boolean> ids = new ArrayList<>(Collections.nCopies(answers.getAnswers().keySet().size(), true));

        for(Pair<Rule, Integer> rule: rules){
            if(rule.getFirst().apply(answers)) {

                for(Integer questionIndex: rule.getFirst().getQuestionIndex())
                    ids.set(questionIndex, false);
            }
        }

        return new Response<>(ids, false, "details");
    }

    public List<Pair<Rule, Integer>> getRules() {
        return rules;
    }
}

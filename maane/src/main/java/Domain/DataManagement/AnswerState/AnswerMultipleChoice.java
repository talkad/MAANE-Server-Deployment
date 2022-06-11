package Domain.DataManagement.AnswerState;

import Domain.CommonClasses.Response;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Domain.DataManagement.AnswerState.AnswerType.MULTIPLE_CHOICE;

public class AnswerMultipleChoice implements Answer{

    private Map<Integer, String> answers;
    private int indexer;

    public AnswerMultipleChoice() {
        this.indexer = 0;
        this.answers = new HashMap<>();
    }

    @Override
    public Response<Boolean> addAnswer(String answer){

        if(answer.length() == 0)
            return new Response<>(false, true, "answer cannot be empty");

        this.answers.put(indexer++, answer);
        return new Response<>(true, false, "added answer successfully");
    }

    @Override
    public Response<Boolean> removeAnswer (int answerID){

        if(answers.size() <= answerID)
            return new Response<>(false, true, "answer doesn't exist");

        this.answers.remove(answerID);

        return new Response<>(true, false, "removed question successfully");
    }

    @Override
    public Response<List<String>> getAnswers() {
        return new Response<>(new LinkedList<>(answers.values()), false, "OK");
    }

    @Override
    public Response<AnswerType> getType() {
        return new Response<>(MULTIPLE_CHOICE, true, "wrong answer type");
    }

    @Override
    public Response<Boolean> defineType(AnswerType type) {
        return new Response<>(true, false, "OK");
    }

}

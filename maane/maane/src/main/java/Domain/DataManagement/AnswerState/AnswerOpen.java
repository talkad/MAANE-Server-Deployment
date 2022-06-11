package Domain.DataManagement.AnswerState;

import Domain.CommonClasses.Response;

import java.util.LinkedList;
import java.util.List;

public class AnswerOpen implements Answer{

    private AnswerType type;

    public AnswerOpen(AnswerType type) {
        this.type = type;
    }

    @Override
    public Response<Boolean> addAnswer(String answer) {
        return new Response<>(false, true, "wrong answer type");
    }

    @Override
    public Response<Boolean> removeAnswer(int answerID) {
        return new Response<>(false, true, "wrong answer type");
    }

    @Override
    public Response<List<String>> getAnswers() {
        return new Response<>(new LinkedList<>(), true, "wrong answer type");
    }

    @Override
    public Response<AnswerType> getType() {
        return new Response<>(type, false, "OK");
    }

    @Override
    public Response<Boolean> defineType(AnswerType type) {
        this.type = type;
        return new Response<>(true, false, "OK");
    }


}

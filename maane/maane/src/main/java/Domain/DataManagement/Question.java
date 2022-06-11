package Domain.DataManagement;

import Domain.CommonClasses.Response;
import Domain.DataManagement.AnswerState.Answer;
import Domain.DataManagement.AnswerState.AnswerMultipleChoice;
import Domain.DataManagement.AnswerState.AnswerOpen;
import Domain.DataManagement.AnswerState.AnswerType;

import java.util.List;

import static Domain.DataManagement.AnswerState.AnswerType.NUMERIC_ANSWER;
import static Domain.DataManagement.AnswerState.AnswerType.OPEN_ANSWER;

public class Question {

    private int id;
    private String question;
    private Answer answer;

    public Question(int id, String question, AnswerType type){
        this.id = id;
        this.question = question;

        initAnswerType(type);
    }

    private void initAnswerType(AnswerType type) {
        switch (type){
            case OPEN_ANSWER:
                answer = new AnswerOpen(OPEN_ANSWER);
                break;
            case NUMERIC_ANSWER:
                answer = new AnswerOpen(NUMERIC_ANSWER);
                break;
            case MULTIPLE_CHOICE:
                answer = new AnswerMultipleChoice();
                break;
        }
    }

    public Response<Boolean> addAnswer(String ans) {
        return answer.addAnswer(ans);
    }

    public Response<Boolean> removeAnswer(int answerID) {
        return answer.removeAnswer(answerID);
    }

    public Response<Boolean> defineType(AnswerType type) {
        return answer.defineType(type);
    }

    public Response<List<String>> getAnswers() {
        return answer.getAnswers();
    }

    public Response<AnswerType> getType() {
        return answer.getType();
    }

    public String getQuestion() {
        return question;
    }

    public int getId () { return id; }

    public void setId (int id) { this.id = id; }

    public void setQuestion (String question) { this.question = question; }
}

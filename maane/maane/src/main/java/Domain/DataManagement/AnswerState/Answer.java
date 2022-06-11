package Domain.DataManagement.AnswerState;

import Domain.CommonClasses.Response;

import java.util.List;


public interface Answer {

    Response<Boolean> addAnswer(String answer);

    Response<Boolean> removeAnswer (int answerID);

    Response<List<String>> getAnswers();

    Response<AnswerType> getType();

    Response<Boolean> defineType(AnswerType type);

}

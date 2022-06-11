package Communication.DTOs;

import Domain.DataManagement.AnswerState.AnswerType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswersDTO {

    private String id;
    private List<String> answers;
    private List<AnswerType> types;

    public List<String> getAnswers() {
        return answers;
    }

    public List<AnswerType> getTypes() {
        return types;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public void setTypes(List<AnswerType> types) {
        this.types = types;
    }

}

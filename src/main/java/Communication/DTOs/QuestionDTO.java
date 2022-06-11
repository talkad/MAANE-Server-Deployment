package Communication.DTOs;

import Domain.DataManagement.AnswerState.AnswerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestionDTO {

    private String surveyID;
    private String question;
    private List<String> answers;
    private AnswerType type;
}

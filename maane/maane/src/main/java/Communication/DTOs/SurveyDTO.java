package Communication.DTOs;

import Domain.DataManagement.AnswerState.AnswerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SurveyDTO {

    private boolean isPublished;
    private String id;
    private String title;
    private String description;
    private List<String> questions;
    private List<List<String>> answers;
    private List<AnswerType> types;
    private int year;

}

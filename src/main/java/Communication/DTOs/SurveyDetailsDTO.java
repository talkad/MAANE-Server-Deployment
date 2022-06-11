package Communication.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDetailsDTO {

        private boolean isPublished;
        private String title;
        private String description;
        private String id;
        private int year;
}

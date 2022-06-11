package Communication.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class SurveyStatsDTO {

    private List<String> symbols;
    private List<String> schoolNames;

    /**
     * for each numeric question (key), returns the average answer
     */
    private Map<Integer, Integer> numericAverage;

    /**
     * for each numeric question (key), returns a list with the number of answers for each choice
     */
    private Map<Integer, List<Integer>> multipleHistogram;

}

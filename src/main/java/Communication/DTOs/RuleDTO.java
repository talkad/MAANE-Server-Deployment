package Communication.DTOs;

import Domain.DataManagement.FaultDetector.Rules.Comparison;
import Domain.DataManagement.FaultDetector.Rules.RuleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class RuleDTO {

    private List<RuleDTO> subRules;
    private RuleType type;
    private Comparison comparison;
    private Integer questionID;
    private List<Integer> answers;

}

package Communication.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SchoolManagementDTO {
    private String affectedUser;
    private List<String> schools;
}

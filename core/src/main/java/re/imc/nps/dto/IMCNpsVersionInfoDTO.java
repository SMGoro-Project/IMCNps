package re.imc.nps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IMCNpsVersionInfoDTO {
    private int version;
    private String updateUrl;
}

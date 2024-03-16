package re.imc.nps.config;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import re.imc.nps.ClientMain;
import re.imc.nps.Info;
import re.imc.nps.dto.ResultDTO;
import re.imc.nps.utils.HttpUtils;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NpsConfig {

    private static final Gson gson = new Gson();
    ;

    private String npsUrl;
    private String key;
    private int roomId;

    public static NpsConfig generateConfig(String token, int port) {
        String result = HttpUtils.sendGet(Info.API_URL + "/imc-nps/start-nps", "token=" + token + "&localPort=" + port);

        ResultDTO resultDTO = gson.fromJson(result, ResultDTO.class);
        if (resultDTO == null || !resultDTO.isSuccess()) {
            if (resultDTO != null) {
                ClientMain.getNpsLogger().logInfo(resultDTO.getMsg());
            } else {
                return null;
            }
        }
        return gson.fromJson(gson.toJson(resultDTO.getData()), NpsConfig.class);
    }


}

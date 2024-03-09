package re.imc.nps.update;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import re.imc.nps.ClientMain;
import re.imc.nps.Info;
import re.imc.nps.dto.IMCNpsVersionInfoDTO;
import re.imc.nps.dto.ResultDTO;
import re.imc.nps.i18n.LocaleMessage;
import re.imc.nps.utils.HttpUtils;

public class UpdateChecker {


    private static final Gson gson = new Gson();
    ;

    public static void checkUpdate() {
        String result = HttpUtils.sendGet(Info.API_URL + "/imc-nps/check-update", "platform=" + ClientMain.getPlatform().getCode());
        ResultDTO<IMCNpsVersionInfoDTO> resultDTO = gson.fromJson(result, new TypeToken<ResultDTO<IMCNpsVersionInfoDTO>>() {
        }.getType());
        if (resultDTO == null || !resultDTO.isSuccess()) {

        } else {
            IMCNpsVersionInfoDTO data = resultDTO.getData();
            if (data.getVersion() > Info.VERSION) {
                ClientMain.getLogHandler().accept(LocaleMessage.message("update_check_need_update").replace("%url%", data.getUpdateUrl()));
            } else {

            }
        }
    }
}
package re.imc.nps.i18n;

import re.imc.nps.ClientMain;
import re.imc.nps.Info;

public class LocaleMessage {

    public static String message(String key) {
        return ClientMain.getProperties().getProperty(key, key)
                .replace("&", "§")
                .replace("%build_version%", String.valueOf(Info.BUILD_VERSION))
                .replace("%version%", String.valueOf(Info.VERSION));
    }
}

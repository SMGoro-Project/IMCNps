package re.imc.nps.i18n;

import re.imc.nps.ClientMain;

public class LocaleMessage {

    public static String message(String key) {
        return ClientMain.getProperties().getProperty(key, "").replace("&", "§");
    }
}

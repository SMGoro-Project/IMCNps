package re.imc.nps;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Info {

    public static final int VERSION = 1;
    public static final String NPS_PATH = "npc";
    public static final String API_URL = "http://atlasapi.smgoro.com:25563";

    public static SystemType checkSystemType() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return SystemType.WINDOWS;
        }
        return SystemType.LINUX;
    }

    public enum SystemType {
        WINDOWS,
        LINUX
    }

    @AllArgsConstructor
    public enum Platform {
        SPIGOT("spigot"),
        STANDALONE("standalone"),
        FABRIC("fabric"),
        FORGE("forge"); // TODO

        @Getter
        private String code;

    }
}

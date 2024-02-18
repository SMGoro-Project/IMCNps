package re.imc.nps;

public class Info {
    public static final String NPS_PATH = "npc";
    public static final String API_URL = "http://atlasapi.smgoro.com:25563";

    public enum SystemType {
        WINDOWS,
        LINUX
    }

    public static SystemType checkSystemType() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return SystemType.WINDOWS;
        }
        return SystemType.LINUX;

    }
}

package re.imc.nps.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface NpsLogger {

    void logNpsProcess(String msg);
    default void logInfo(String msg) {
        logInfo(LegacyComponentSerializer.legacySection().deserialize(msg));
    }
    default void logInfoConsole(String msg) {
        logInfoConsole(LegacyComponentSerializer.legacySection().deserialize(msg));
    }

    void logInfo(Component component);
    void logInfoConsole(Component component);

}

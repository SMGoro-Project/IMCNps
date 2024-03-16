package re.imc.nps.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import re.imc.nps.ClientMain;
import re.imc.nps.Info;

import java.util.function.Consumer;
import java.util.function.Function;

public class LocaleMessage {

    public static Component message(String key, Function<String, String> textProcess) {
        String result = textProcess.apply(ClientMain.getLangToml().getString(key, () -> key)
                .replace("&", "§")
                .replace("%build_version%", String.valueOf(Info.BUILD_VERSION))
                .replace("%version%", String.valueOf(Info.VERSION)));
        Component component;
        try {
            component = MiniMessage.miniMessage().deserialize(result);
        } catch (Exception e) {
            component = LegacyComponentSerializer.legacySection().deserialize(result);
        }
        return component;
    }

    public static Component message(String key) {
        return message(key, (s) -> s);
    }
}

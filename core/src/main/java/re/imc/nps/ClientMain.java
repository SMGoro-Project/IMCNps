package re.imc.nps;

import lombok.Getter;
import lombok.Setter;
import re.imc.nps.config.NpsConfig;
import re.imc.nps.i18n.LocaleMessage;
import re.imc.nps.process.NpsProcess;
import re.imc.nps.update.UpdateChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClientMain {

    public static String TOKEN;
    public static Path DATA_PATH;
    @Getter
    private static NpsProcess process;
    @Getter
    private static NpsConfig config;
    @Getter
    private static Properties properties;
    @Setter
    private static Consumer<NpsProcess> startHandler;
    @Setter
    @Getter
    private static Consumer<String> outHandler;
    @Setter
    @Getter
    private static Consumer<String> logHandler;
    @Setter
    @Getter
    private static Info.Platform platform;

    public static void start(Path path, Info.Platform platform) {
        loadLang();
        ClientMain.platform = platform;
        DATA_PATH = path;
        path.toFile().mkdirs();
        readToken();
        if (TOKEN == null) {
            return;
        }
        registerCloseHook();
        config = loadNps();
        startHandler.accept(process);

        UpdateChecker.checkUpdate();
    }

    public static void start(Path path) {
        start(path, platform);
    }

    public static void readToken() {
        TOKEN = System.getProperty("nps.accesstoken", null);

        if (TOKEN != null) {
            return;
        }
        Path file = DATA_PATH.resolve("token.txt");
        if (!file.toFile().exists()) {

            try {
                InputStream in = ClientMain.class.getClassLoader().getResourceAsStream("token.txt");

                Files.copy(in, file);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            TOKEN = Files.readAllLines(file).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static NpsConfig loadNps() {

        File file = new File(DATA_PATH.toFile(), Info.NPS_PATH);

        file.mkdirs();

        Info.SystemType type = Info.checkSystemType();

        String npsName = type == Info.SystemType.WINDOWS ? "npc.exe" : "npc";
        File npsFile = new File(file, npsName);
        InputStream in = ClientMain.class.getClassLoader().getResourceAsStream(npsName);

        if (!npsFile.exists()) {
            try {
                assert in != null;
                Files.copy(in, npsFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        npsFile.setReadable(true);
        npsFile.setExecutable(true);
        npsFile.setWritable(true);
        NpsConfig config = NpsConfig.generateConfig(TOKEN);
        if (config == null) {
            getLogHandler().accept(LocaleMessage.message("not_load_npc_config"));
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(() -> ClientMain.start(ClientMain.DATA_PATH), 3, TimeUnit.SECONDS);
            ;
            return null;
        }
        process = new NpsProcess(DATA_PATH + "/" + Info.NPS_PATH, type, config);
        process.start();
        return config;
    }

    public static void loadLang() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage() + "_" + locale.getCountry();

        properties = new Properties();
        InputStream langInput = ClientMain.class.getClassLoader()
                .getResourceAsStream("lang_" + lang + ".properties");
        if (langInput == null) {
            langInput = ClientMain.class.getClassLoader()
                    .getResourceAsStream("lang_en_US.properties");
        }

        try {
            properties.load(new InputStreamReader(langInput, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //注册关闭钩子
    public static void registerCloseHook() {

        Runtime.getRuntime().addShutdownHook((new Thread(() -> {
            try {
                process.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })));
    }
}
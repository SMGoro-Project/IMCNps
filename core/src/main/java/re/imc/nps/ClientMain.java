package re.imc.nps;

import lombok.Getter;
import lombok.Setter;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import re.imc.nps.api.NpsLogger;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClientMain {

    public static String TOKEN;
    private static ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public static Path DATA_PATH;
    @Getter
    private static NpsProcess process;
    @Getter
    private static NpsConfig config;
    @Getter
    private static TomlParseResult langToml;
    @Setter
    private static Consumer<NpsProcess> startHandler;
    @Setter
    @Getter
    private static NpsLogger npsLogger;
    @Setter
    @Getter
    private static Info.Platform platform;
    private static int port;

    public static void setup(Path path, Info.Platform platform, NpsLogger logger) {
        ClientMain.npsLogger = logger;
        loadVersion();
        loadLang();
        if (platform != Info.Platform.FABRIC) {
            logger.logInfo(LocaleMessage.message("before_start"));
        }
    }
    public static void start(Path path, Info.Platform platform, int port) {
        ClientMain.platform = platform;
        DATA_PATH = path;
        path.toFile().mkdirs();
        readToken();
        if (TOKEN == null) {
            return;
        }
        registerCloseHook();
        ClientMain.port = Integer.parseInt(System.getProperty("nps.port", String.valueOf(port)));
        config = loadNps(ClientMain.port);
        startHandler.accept(process);

        UpdateChecker.checkUpdate();
    }

    public static void start(Path path) {
        start(path, platform, ClientMain.port);
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
            TOKEN = Files.readAllLines(file).get(0).replace(" ", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static NpsConfig loadNps(int port) {

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
        NpsConfig config = NpsConfig.generateConfig(TOKEN, port);
        if (config == null) {
            npsLogger.logInfo(LocaleMessage.message("not_load_npc_config"));
            SCHEDULED_EXECUTOR.schedule(() -> ClientMain.start(ClientMain.DATA_PATH), 3, TimeUnit.SECONDS);
            if (process != null) {
                process.stop();
            }
            return null;
        }
        process = new NpsProcess(DATA_PATH + "/" + Info.NPS_PATH, type, config);
        process.start();
        return config;
    }

    public static void loadVersion() {
        Properties versionProperties = new Properties();
        InputStream input = ClientMain.class.getClassLoader()
                .getResourceAsStream("imcnps.version.properties");
        if (input == null) {
            return;
        }


        try {
            versionProperties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Info.VERSION = versionProperties.getProperty("version");
        Info.BUILD_VERSION = Integer.parseInt(versionProperties.getProperty("build_version"));
    }
    public static void loadLang() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage() + "_" + locale.getCountry();

        InputStream langInput = ClientMain.class.getClassLoader()
                .getResourceAsStream("lang_" + lang + ".toml");
        if (langInput == null) {
            langInput = ClientMain.class.getClassLoader()
                    .getResourceAsStream("lang_en_US.toml");
        }

        try {
            ClientMain.langToml = Toml.parse(langInput);
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
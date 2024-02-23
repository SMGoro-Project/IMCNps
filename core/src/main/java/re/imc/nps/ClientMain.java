package re.imc.nps;

import lombok.Getter;
import lombok.Setter;
import re.imc.nps.config.NpsConfig;
import re.imc.nps.process.NpsProcess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class ClientMain {

    @Getter
    private static NpsProcess process;
    @Getter
    private static NpsConfig config;
    public static String TOKEN;
    public static Path DATA_PATH;

    @Setter
    private static Consumer<NpsProcess> startHandler;

    public static void main(String[] args) {
        try {
            TOKEN = args[0];
        } catch (Throwable ignore) {

        }

        /*
        try {
            // 打开一个新的cmd窗口
            Runtime.getRuntime().exec("cmd /k start start.bat");
        } catch (IOException e) {
            e.printStackTrace();
        }

         */


        ClientMain.setStartHandler(process -> {
            process.setOutHandler(System.out::println);
            process.setLogHandler(System.out::println);

            if (config == null) return;
            System.out.println("=======================");
            System.out.println("房间号: " + config.getRoomId());
            System.out.println("可输入/jr " + config.getRoomId() + " 进入服务器");
            System.out.println("=======================");
        });

        start(new File(System.getProperty("user.dir")).toPath());
        registerDaemonThread();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void start(Path path) {
        DATA_PATH = path;
        path.toFile().mkdirs();
        readToken();
        if (TOKEN == null) {
            return;
        }
        registerCloseHook();
        config = loadNps();
        startHandler.accept(process);
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
        process = new NpsProcess(DATA_PATH + "/" + Info.NPS_PATH, type, config);
        process.start();
        return config;
    }



    @SuppressWarnings("BusyWait")
    public static void registerDaemonThread() {
        Thread thread = new Thread(() -> {
            try {
                registerCloseHook();
                while (true) {
                    Thread.sleep(1000);
                    System.gc();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    //注册关闭钩子
    public static void registerCloseHook() {

        Runtime.getRuntime().addShutdownHook((new Thread(() -> {
            try{
                process.stop();
            }catch (Exception e) {
                e.printStackTrace();
            }
        })));
    }
}
package re.imc.nps.process;

import lombok.Getter;
import lombok.Setter;
import re.imc.nps.Info;
import re.imc.nps.config.NpsConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Getter
public class NpsProcess {

    private final String npsPath;
    private final Info.SystemType systemType;
    private Process process;
    private Thread executeThread;
    private Thread readThread;
    private NpsConfig config;
    @Setter
    private Consumer<String> outHandler;
    public NpsProcess(String npsPath, Info.SystemType systemType, NpsConfig config) {
        this.npsPath = npsPath;
        this.systemType = systemType;
        this.config = config;
    }

    public void start() {
        executeThread = new Thread(() -> {
            try {
                Runtime run = Runtime.getRuntime();
                if (systemType == Info.SystemType.WINDOWS) {
                    process = run.exec("taskkill /im npc.exe /f");
                    BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    output.readLine();
                    output.close();
                    process = run.exec(npsPath + "/npc.exe -server=" + config.getNpsUrl() + " -vkey=" + config.getKey());
                }
                if (systemType == Info.SystemType.LINUX) {
                    process = run.exec(npsPath + "/npc -server=" + config.getNpsUrl() + " -vkey=" + config.getKey());
                }
                startRead();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Executors.newSingleThreadScheduledExecutor().execute(executeThread::start);

    }
    public void startRead() {
        readThread = new Thread(this::readNps);
        Executors.newSingleThreadScheduledExecutor().execute(readThread::start);
    }
    public void readNps() {
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader out = new BufferedReader(inputStreamReader);
        String info;
        while (true) {
            if (!process.isAlive()) {return;}
            try {
                info = out.readLine();
                if (info == null) {continue;}
                if (outHandler != null) {
                    outHandler.accept(info);
                }
            } catch (Exception e) {
                outHandler.accept(e.getMessage());
            }
        }
    }

    public void stop() {
        if (process.isAlive()) {
            executeThread.interrupt();
            process.destroyForcibly();
        }
    }
}

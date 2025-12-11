package v2.levels.stages;

import utilities.CLIUtils;
import v2.engine.SandboxBackupManager;
import v2.levels.Level;

import java.io.IOException;

public abstract class Stage {
    protected int stageNumber;
    protected Level level;

    public Stage(int stageNumber, Level level) {
        this.stageNumber = stageNumber;
        this.level = level;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(int  newStageNumber) {
        stageNumber = newStageNumber;
    }

    public void printStageHeader() {
        level.printLevelHeader();
        IO.println();
        IO.println();
        CLIUtils.repeat('━');
        IO.println(CLIUtils.center(getStageHeader()[0]));
        CLIUtils.repeat('━');
    }

    @FunctionalInterface
    public interface BeforeSetupCallback {
        void run() throws IOException;
    }

    @FunctionalInterface
    public interface AfterSetupCallback {
        void run();
    }


    public void execute(BeforeSetupCallback before, AfterSetupCallback after) {
        CLIUtils.clearScreen();

        try {
            before.run();
            setupEnvironment();
            after.run();
            printStageHeader();
            play();
            if(level.player.getStats().getHealth() == 0) return;
            onSuccessPlay();
            level.sandbox.getBackupManager().flush(SandboxBackupManager.FlushMode.EXCEPT_INVENTORY);
            level.sandbox.getExecutor().execute("cd " + level.sandbox.getSandBoxPath().toAbsolutePath());
        } catch (Exception e) {
            onFailedPlay(e);
        }
    }

    public abstract String[] getStageHeader();

    public abstract void play();

    public abstract void onSuccessPlay();

    public abstract void onFailedPlay(Exception exception);

    public abstract void setupEnvironment();
}

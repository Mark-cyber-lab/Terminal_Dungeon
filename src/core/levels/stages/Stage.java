package core.levels.stages;

import core.levels.Level;
import utilities.CLIUtils;

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

    public void execute () {
        CLIUtils.clearScreen();
        setupEnvironment();
        try {
            printStageHeader();
            play();
            onSuccessPlay();
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

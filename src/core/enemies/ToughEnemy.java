package core.enemies;

import utilities.CommandResult;

import java.nio.file.Path;

public class ToughEnemy extends Enemy {
    public ToughEnemy(String name, String id, Path enemyPath, int damagePerUnit) {
        super(name, id, enemyPath, damagePerUnit);
    }

    @Override
    protected void executeConditions(CommandResult result, boolean sameFilePath) {
        String cmd = result.command().toLowerCase();

        if (!hasBeenDefeated() && cmd.equals("sudo rm") && result.subject() != null) {
            if (sameFilePath) {
                clear();
            }
        }
    }
}

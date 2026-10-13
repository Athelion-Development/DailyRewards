package eu.athelion.dailyrewards.commandmanager.subcommand;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.commandmanager.SubCommand;
import eu.athelion.dailyrewards.data.DataManager;
import eu.athelion.dailyrewards.configuration.file.Lang;
import eu.athelion.dailyrewards.util.PermissionUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ImportCommand implements SubCommand {
    @Override
    public String getName() {
        return "import";
    }

    @Override
    public Lang getDescription() {
        return Lang.IMPORT_COMMAND_DESCRIPTION;
    }

    @Override
    public String getSyntax() {
        return "/reward import";
    }

    @Override
    public String getPermission() {
        return PermissionUtil.Permission.ADMIN_PERMISSION.get();
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        return null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        // The import waits on a latch and writes to the database, neither of which
        // is allowed on the main thread.
        DailyRewardsPlugin.get().runAsync(() -> DataManager.importToDatabase(sender));
    }
}

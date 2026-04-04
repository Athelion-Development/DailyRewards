package eu.athelion.dailyrewards.commandmanager.command;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.commandmanager.MainCommand;
import eu.athelion.dailyrewards.commandmanager.argumentmatcher.StartingWithStringArgumentMatcher;
import eu.athelion.dailyrewards.configuration.file.Lang;
import eu.athelion.dailyrewards.util.PermissionUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RewardsMainCommand extends MainCommand {
    public RewardsMainCommand() {
        super(Lang.INSUFFICIENT_PERMISSION, new StartingWithStringArgumentMatcher());
    }

    @Override
    protected void registerSubCommands() {
        // Command hasn't any subcommands
    }

    @Override
    protected void perform(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[DailyRewards] This command is only executable in-game!");
            return;
        }

        final Player player = (Player) sender;

        if (!PermissionUtil.hasPermission(player, PermissionUtil.Permission.OPENS_MAIN_REWARD_MENU)) {
            sender.sendMessage(Lang.INSUFFICIENT_PERMISSION.asColoredString(player)
                    .replace("%permission%", PermissionUtil.Permission.OPENS_MAIN_REWARD_MENU.get()));
            return;
        }

        DailyRewardsPlugin.getMenuManager().openRewardsMenu((Player) sender, true);
    }
}

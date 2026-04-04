package eu.athelion.dailyrewards.manager.reward.action;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.configuration.file.Config;
import eu.athelion.dailyrewards.configuration.file.Lang;
import eu.athelion.dailyrewards.manager.reward.action.checker.Checker;
import eu.athelion.dailyrewards.manager.reward.action.response.ActionResponse;
import eu.athelion.dailyrewards.util.PermissionUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface RewardAction<T> {
    default ActionResponse preCheck(OfflinePlayer player, T extra) {
        if (player == null) {
            getExecutor().sendMessage(Lang.UNAVAILABLE_PLAYER.asColoredString().replace("%player%", "null"));
            return ActionResponse.Type.UNAVAILABLE_PLAYER;
        }
        if (!PermissionUtil.hasPermission(getExecutor(), getPermission())) {
            getExecutor().sendMessage(Lang.INSUFFICIENT_PERMISSION.asColoredString((Player) getExecutor())
                    .replace("%permission%", getPermission().get()));
            return ActionResponse.Type.NO_PERMISSION;
        }

        ActionResponse response = execute(player, extra);

        if (menuShouldOpen() && Config.OPEN_MENU_AFTER_CLAIMING.asBoolean()) {
            DailyRewardsPlugin.getMenuManager().openRewardsMenu(player.getPlayer(), false);
        }

        return response;
    }

    ActionResponse execute(OfflinePlayer player, T extra);

    boolean menuShouldOpen();

    CommandSender getExecutor();

    PermissionUtil.Permission getPermission();

    List<Checker> getCheckers();
}

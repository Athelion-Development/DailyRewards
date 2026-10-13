package eu.athelion.dailyrewards.commandmanager.subcommand;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.commandmanager.SubCommand;
import eu.athelion.dailyrewards.configuration.file.Lang;
import eu.athelion.dailyrewards.manager.reward.Reward;
import eu.athelion.dailyrewards.manager.reward.RewardType;
import eu.athelion.dailyrewards.manager.reward.action.ClaimAction;
import eu.athelion.dailyrewards.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ClaimCommand implements SubCommand {
    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public Lang getDescription() {
        return Lang.CLAIM_COMMAND_DESCRIPTION;
    }

    @Override
    public String getSyntax() {
        return "/reward claim <daily|weekly|monthly> (<player>)";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, int index, String[] args) {
        if (args.length == 1)       return DailyRewardsPlugin.getRewardManager().getRewards().stream().map(Reward::getName).collect(Collectors.toList());
        else if (args.length == 2)  return PermissionUtil.hasPermission(sender, PermissionUtil.Permission.CLAIM_FOR_OTHERS)
                ? DailyRewardsPlugin.get().getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())
                : Collections.emptyList();
        else                        return Collections.emptyList();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(args[0].toUpperCase(Locale.ENGLISH));
        } catch (Exception exception){
            sender.sendMessage(Lang.COMMAND_USAGE.asColoredString().replace("%usage%", getSyntax()));
            return;
        }

        Player claimingPlayer;
        if (args.length == 1) {
            if (!(sender instanceof Player)){
                sender.sendMessage("[DailyRewards] Correct usage: " + getSyntax());
                return;
            }
            claimingPlayer = (Player) sender;
        } else if (args.length == 2) {
            if (!PermissionUtil.hasPermission(sender, PermissionUtil.Permission.CLAIM_FOR_OTHERS)) {
                sender.sendMessage(Lang.INSUFFICIENT_PERMISSION.asColoredString()
                        .replace("%permission%", PermissionUtil.Permission.CLAIM_FOR_OTHERS.get()));
                return;
            }
            claimingPlayer = Bukkit.getPlayerExact(args[1]);
        } else {
            sender.sendMessage(Lang.COMMAND_USAGE.asColoredString().replace("%usage%", getSyntax()));
            return;
        }

        new ClaimAction(sender)
                .disableMenuOpening()
                .preCheck(claimingPlayer, rewardType);
    }
}

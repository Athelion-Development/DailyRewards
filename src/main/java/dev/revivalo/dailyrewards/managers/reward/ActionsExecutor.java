package dev.revivalo.dailyrewards.managers.reward;

import com.google.common.base.Splitter;
import dev.revivalo.dailyrewards.DailyRewardsPlugin;
import dev.revivalo.dailyrewards.utils.PlayerUtils;
import dev.revivalo.dailyrewards.utils.TextUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionsExecutor {
    private static final Pattern placeholderPattern = Pattern.compile("%\\w+%");

    public static void executeActions(Player player, String rewardIdentifier, List<RewardAction> actions) {
        final Set<String> words = new HashSet<>();

        String[] titleText = new String[]{" ", " "};


        actions.forEach(action -> {
            String line = action.getStatement();

            Matcher matcher = placeholderPattern.matcher(line);

            while (matcher.find()) {
                words.add(matcher.group());
            }

            for (String word : words) {
                switch (word) {
                    case "%player%":
                        line = StringUtils.replace(line, word, player.getName());
                        break;
                    case "%type%":
                        line = StringUtils.replace(line, word, rewardIdentifier);
                        break;
                }
            }

            String lineWithPlaceholders = TextUtils.applyPlaceholdersToString(player, line);
            String coloredLine = TextUtils.colorize(lineWithPlaceholders);

            switch (action.getActionType()) {
                case CONSOLE:
                    DailyRewardsPlugin.get().executeCommandAsConsole(lineWithPlaceholders);
                    break;
                case PLAYER:
                    player.performCommand("/" + lineWithPlaceholders);
                    break;
                case MESSAGE:
                    TextUtils.sendListToPlayer(player, Splitter.on("|").splitToList(coloredLine));
                    break;
                case ACTIONBAR:
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(coloredLine));
                    break;
                case TITLE:
                    titleText[0] = coloredLine;
                    break;
                case SUBTITLE:
                    titleText[1] = coloredLine;
                    break;
                case SOUND:
                    PlayerUtils.playSound(player, line.toUpperCase(Locale.ENGLISH));
                    break;
//                case FIREWORK:
//                    CustomFireworkBuilder
//                            .fromString(line)
//                            .launch(player.getLocation());
//                    //PlayerUtils.spawnFirework(player.getLocation());
//                    break;
            }
        });


        player.sendTitle(titleText[0], titleText[1]);
    }
}

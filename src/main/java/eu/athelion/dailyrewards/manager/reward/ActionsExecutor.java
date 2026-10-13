package eu.athelion.dailyrewards.manager.reward;

import com.google.common.base.Splitter;
import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.util.PlayerUtil;
import eu.athelion.dailyrewards.util.TextUtil;
import eu.athelion.dailyrewards.util.builder.CustomFireworkBuilder;
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

        // Left null until an action actually sets them, so rewards without a
        // title/subtitle do not blank out whatever is on screen.
        final String[] titleText = new String[2];


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

            String lineWithPlaceholders = TextUtil.applyPlaceholdersToString(player, line);
            String coloredLine = TextUtil.colorize(lineWithPlaceholders);

            switch (action.getActionType()) {
                case CONSOLE:
                    DailyRewardsPlugin.get().runSync(() ->
                            DailyRewardsPlugin.get().executeCommandAsConsole(lineWithPlaceholders)
                    );
                    break;
                case PLAYER:
                    // performCommand expects the command without a leading slash,
                    // but configs are commonly written with one.
                    player.performCommand(lineWithPlaceholders.startsWith("/")
                            ? lineWithPlaceholders.substring(1)
                            : lineWithPlaceholders);
                    break;
                case MESSAGE:
                    TextUtil.sendListToPlayer(player, Splitter.on("|").splitToList(coloredLine));
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
                    PlayerUtil.playSound(player, line.toUpperCase(Locale.ENGLISH));
                    break;
                case FIREWORK:
                    CustomFireworkBuilder
                            .fromString(line)
                            .launch(player.getLocation());
                    break;
            }
        });


        if (titleText[0] != null || titleText[1] != null) {
            player.sendTitle(
                    titleText[0] == null ? " " : titleText[0],
                    titleText[1] == null ? " " : titleText[1]
            );
        }
    }
}

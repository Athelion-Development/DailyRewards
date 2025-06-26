package eu.athelion.dailyrewards.manager.reward;

import eu.athelion.dailyrewards.api.event.AutoClaimEvent;
import eu.athelion.dailyrewards.configuration.file.Config;
import eu.athelion.dailyrewards.configuration.file.Lang;
import eu.athelion.dailyrewards.manager.reward.action.AutoClaimAction;
import eu.athelion.dailyrewards.user.User;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RewardManager {
    private final Set<Reward> rewards;

    public RewardManager() {
        this.rewards = new HashSet<>();
        loadRewards();
    }

    public boolean processAutoClaimForUser(User user) {
//        if (!user.hasSettingEnabled(Setting.AUTO_CLAIM)) {
//            return false;
//        }

        if (user.getAvailableRewards().isEmpty()) {
            return false;
        }

        AutoClaimEvent autoClaimEvent = new AutoClaimEvent(user.getPlayer(), user.getAvailableRewards());
        Bukkit.getPluginManager().callEvent(autoClaimEvent);

        new AutoClaimAction()
                        .preCheck(user.getPlayer(), user.getAvailableRewards());

        return !autoClaimEvent.isCancelled();
    }

    public void loadRewards() {
        rewards.clear();
        if (Config.DAILY_ENABLED.asBoolean())
            rewards.add(new Reward(RewardType.DAILY,
                    Config.DAILY_AVAILABLE_AFTER_FIRST_JOIN,
                    Config.DAILY_COOLDOWN_FORMAT,
                    Config.DAILY_COOLDOWN,
                    Config.DAILY_COOLDOWN_FORMAT,
                    Config.DAILY_POSITIONS,
                    Config.DAILY_SOUND,
                    Lang.DAILY_COLLECTED,
                    Lang.DAILY_PREMIUM_COLLECTED,
                    Config.DAILY_AVAILABLE_ITEM,
                    Config.DAILY_UNAVAILABLE_ITEM,
                    Lang.DAILY_DISPLAYNAME_AVAILABLE,
                    Lang.DAILY_PREMIUM_DISPLAYNAME_AVAILABLE,
                    Lang.DAILY_DISPLAYNAME_UNAVAILABLE,
                    Lang.DAILY_PREMIUM_DISPLAYNAME_UNAVAILABLE,
                    Lang.DAILY_AVAILABLE_LORE,
                    Lang.DAILY_AVAILABLE_PREMIUM_LORE,
                    Lang.DAILY_UNAVAILABLE_LORE,
                    Lang.DAILY_UNAVAILABLE_PREMIUM_LORE,
                    Config.DAILY_REWARDS,
                    Config.DAILY_PREMIUM_REWARDS));
        if (Config.WEEKLY_ENABLED.asBoolean())
            rewards.add(new Reward(RewardType.WEEKLY,
                    Config.WEEKLY_AVAILABLE_AFTER_FIRST_JOIN,
                    Config.WEEKLY_COOLDOWN_FORMAT,
                    Config.WEEKLY_COOLDOWN,
                    Config.WEEKLY_COOLDOWN_FORMAT,
                    Config.WEEKLY_POSITIONS,
                    Config.WEEKLY_SOUND,
                    Lang.WEEKLY_COLLECTED,
                    Lang.WEEKLY_PREMIUM_COLLECTED,
                    Config.WEEKLY_AVAILABLE_ITEM,
                    Config.WEEKLY_UNAVAILABLE_ITEM,
                    Lang.WEEKLY_DISPLAYNAME_AVAILABLE,
                    Lang.WEEKLY_PREMIUM_DISPLAYNAME_AVAILABLE,
                    Lang.WEEKLY_DISPLAYNAME_UNAVAILABLE,
                    Lang.WEEKLY_PREMIUM_DISPLAYNAME_UNAVAILABLE,
                    Lang.WEEKLY_AVAILABLE_LORE,
                    Lang.WEEKLY_AVAILABLE_PREMIUM_LORE,
                    Lang.WEEKLY_UNAVAILABLE_LORE,
                    Lang.WEEKLY_UNAVAILABLE_PREMIUM_LORE,
                    Config.WEEKLY_REWARDS,
                    Config.WEEKLY_PREMIUM_REWARDS));
        if (Config.MONTHLY_ENABLED.asBoolean())
            rewards.add(new Reward(
                    RewardType.MONTHLY,
                    Config.MONTHLY_AVAILABLE_AFTER_FIRST_JOIN,
                    Config.MONTHLY_COOLDOWN_FORMAT,
                    Config.MONTHLY_COOLDOWN,
                    Config.MONTHLY_COOLDOWN_FORMAT,
                    Config.MONTHLY_POSITIONS,
                    Config.MONTHLY_SOUND,
                    Lang.MONTHLY_COLLECTED,
                    Lang.MONTHLY_PREMIUM_COLLECTED,
                    Config.MONTHLY_AVAILABLE_ITEM,
                    Config.MONTHLY_UNAVAILABLE_ITEM,
                    Lang.MONTHLY_DISPLAYNAME_AVAILABLE,
                    Lang.MONTHLY_PREMIUM_DISPLAYNAME_AVAILABLE,
                    Lang.MONTHLY_DISPLAYNAME_UNAVAILABLE,
                    Lang.MONTHLY_PREMIUM_DISPLAYNAME_UNAVAILABLE,
                    Lang.MONTHLY_AVAILABLE_LORE,
                    Lang.MONTHLY_AVAILABLE_PREMIUM_LORE,
                    Lang.MONTHLY_UNAVAILABLE_LORE,
                    Lang.MONTHLY_UNAVAILABLE_PREMIUM_LORE,
                    Config.MONTHLY_REWARDS,
                    Config.MONTHLY_PREMIUM_REWARDS));
    }

    public Optional<Reward> getRewardByType(RewardType rewardType) {
        return rewards.stream().filter(reward -> reward.getType() == rewardType).findFirst();
    }

    public Set<Reward> getRewards() {
        return rewards;
    }
}
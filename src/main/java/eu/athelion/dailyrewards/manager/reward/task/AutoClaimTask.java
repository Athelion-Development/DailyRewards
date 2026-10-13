package eu.athelion.dailyrewards.manager.reward.task;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.configuration.file.Config;
import eu.athelion.dailyrewards.manager.Setting;
import eu.athelion.dailyrewards.user.User;
import eu.athelion.dailyrewards.util.PermissionUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AutoClaimTask implements Task{
    private final Map<User, Long> playerRewardCheckTimes = new ConcurrentHashMap<>();
    private final Map<UUID, User> usersHashMap;

    public AutoClaimTask(Map<UUID, User> usersHashMap) {
        this.usersHashMap = usersHashMap;
    }

    public void addUser(User user) {
        if (!PermissionUtil.hasPermission(user.getPlayer(), PermissionUtil.Permission.AUTO_CLAIM_SETTING)) {
            return;
        }

        if (!user.hasSettingEnabled(Setting.AUTO_CLAIM)) {
            return;
        }

        long checkTime = System.currentTimeMillis() + (Config.JOIN_AUTO_CLAIM_DELAY.asInt() * 1000L);

        playerRewardCheckTimes.put(user, checkTime);
    }

    @Override
    public BukkitRunnable get() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                for (User user : usersHashMap.values()) {
                    if (!playerRewardCheckTimes.containsKey(user)) continue;

                    long checkTime = playerRewardCheckTimes.get(user);
                    if (currentTime >= checkTime) {
                        playerRewardCheckTimes.remove(user);
                        DailyRewardsPlugin.get().runAtEntity(user.getPlayer(),
                                () -> DailyRewardsPlugin.getRewardManager().processAutoClaimForUser(user));
                    }
                }
            }
        };
    }

    public Map<User, Long> getPlayerRewardCheckTimes() {
        return playerRewardCheckTimes;
    }
}
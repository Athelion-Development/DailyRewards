package eu.athelion.dailyrewards.user;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.data.DataManager;
import eu.athelion.dailyrewards.manager.Setting;
import eu.athelion.dailyrewards.manager.cooldown.Cooldown;
import eu.athelion.dailyrewards.manager.reward.RewardType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User {
    private final Player player;
    private Map<String, Object> data;

    public User(Player player, Map<String, Object> data) {
        this.player = player;
        this.data = data;
    }

    public Cooldown getCooldown(RewardType rewardType) {
        return new Cooldown(readLong(rewardType.toString(), data.get(rewardType.toString()), Long.MAX_VALUE));
    }

    public Set<RewardType> getAvailableRewards(){
        Set<RewardType> availableRewards = new HashSet<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            final RewardType rewardType = RewardType.findByName(entry.getKey());
            if (rewardType == null) continue;
            if (!rewardType.isEnabled()) continue;
            if (new Cooldown(readLong(entry.getKey(), entry.getValue(), Long.MAX_VALUE)).isClaimable()) availableRewards.add(rewardType);
        }
        return availableRewards;
    }

    /**
     * Reads a numeric value out of the loaded data. A failed database read hands
     * back an empty map, so missing or malformed values have to fall back instead
     * of throwing halfway through a join or a menu refresh. Cooldowns fall back to
     * "not claimable" so a broken read never hands out free rewards.
     */
    private long readLong(String key, Object value, long fallback) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value == null) {
            // A failed read is already reported by the backend, so stay quiet here;
            // this runs on every menu refresh and would spam the console.
            return fallback;
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            DailyRewardsPlugin.get().getLogger().warning(String.format(
                    "Unreadable value '%s' for %s of %s, using %d instead.",
                    value, key, player.getName(), fallback));
            return fallback;
        }
    }

    public boolean isOnline() {
        return player.isOnline();
    }

    /**
     *
     * @return True if the setting was enabled
     */
    public boolean toggleSetting(Setting setting, boolean set) {
        data.put(setting.getTag(), set ? "1" : "0");

        DailyRewardsPlugin.get().runAsync(() -> {
            DataManager.updateValues(
                    player.getUniqueId(),
                    this,
                    new HashMap<String, Object>() {{
                        put(setting.getTag(), hasSettingEnabled(setting) ? 1L : 0);
                    }}
            );
        });

        return set;
    }

    public boolean hasSettingEnabled(Setting setting) {
        return 1 == readLong(setting.getTag(), data.get(setting.getTag()), 0);
    }

    public void updateData(Map<String, Object> changes) {
        data.putAll(changes);
    }

    public Player getPlayer() {
        return player;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

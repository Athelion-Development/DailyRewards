package eu.athelion.dailyrewards.data;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.configuration.file.Config;
import eu.athelion.dailyrewards.manager.backend.MySQLManager;
import eu.athelion.dailyrewards.manager.reward.Reward;
import eu.athelion.dailyrewards.manager.reward.RewardType;
import eu.athelion.dailyrewards.user.User;
import eu.athelion.dailyrewards.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class DataManager {

	private static boolean usingMysql;

	public static boolean updateValues(final UUID id, User user, Map<String, Object> data) {
		if (user != null && user.isOnline()) {
			user.updateData(data);
		}

		if (isUsingMysql()) {
			return MySQLManager.updatePlayer(id, data);
		} else {
			final PlayerData playerData = PlayerData.getConfig(id);
			final ConfigurationSection rewardsSection = playerData.getConfigurationSection("rewards");

			try {
				for (Map.Entry<String, Object> entry : data.entrySet())
					rewardsSection.set(entry.getKey(), entry.getValue());
				playerData.save();
			} catch (NullPointerException ignored) {
				return false;
			}
		}

		return true;
	}

	public static void importToDatabase(CommandSender sender) {
		if (!usingMysql) {
			sender.sendMessage(TextUtil.colorize("&cYou need to have MySQL setup first!"));
			return;
		}

		sender.sendMessage(TextUtil.colorize("&aStarting import from files..."));

		CountDownLatch latch = new CountDownLatch(PlayerData.getFiles().size());

		PlayerData.getFiles().parallelStream().forEach(file -> {
			String fileName = file.getName();
			String uuidString = fileName.substring(0, fileName.length() - 4);
			UUID uuid = UUID.fromString(uuidString);
			MySQLManager.createPlayer(uuidString);

			ConfigurationSection data = PlayerData.getConfig(uuid).getConfigurationSection("rewards");

			MySQLManager.updatePlayer(UUID.fromString(uuidString), data.getValues(false));

			latch.countDown();
		});

		try {
			latch.await();
			sender.sendMessage(TextUtil.colorize("&aImport from files completed successfully."));
		} catch (InterruptedException e) {
			sender.sendMessage(TextUtil.colorize("&The import process has been interrupted.\n" + e.getMessage()));
		}
	}

	public static void initiatePlayer(final Player player){
		if (isUsingMysql()) MySQLManager.createPlayer(player.getUniqueId().toString());
		else {
			final PlayerData playerData = PlayerData.getConfig(player.getUniqueId());
			if (!playerData.isConfigurationSection("rewards")) {
				playerData.createSection("rewards");
			}

			final long currentTimeInMillis = System.currentTimeMillis();
			final ConfigurationSection section = playerData.getConfigurationSection("rewards");
			final int multiplier = 60 * 60 * 1000;

			setDefaultValue(section, RewardType.DAILY.toString(), Config.DAILY_AVAILABLE_AFTER_FIRST_JOIN.asBoolean() ? 0 : currentTimeInMillis + Config.DAILY_COOLDOWN.asLong() * multiplier);
			setDefaultValue(section, RewardType.WEEKLY.toString(), Config.WEEKLY_AVAILABLE_AFTER_FIRST_JOIN.asBoolean() ? 0 : currentTimeInMillis + Config.WEEKLY_COOLDOWN.asLong() * multiplier);
			setDefaultValue(section, RewardType.MONTHLY.toString(), Config.MONTHLY_AVAILABLE_AFTER_FIRST_JOIN.asBoolean() ? 0 : currentTimeInMillis + Config.MONTHLY_COOLDOWN.asLong() * multiplier);
			setDefaultValue(section, "autoClaim", Config.AUTO_CLAIM_REWARDS_ON_JOIN_BY_DEFAULT.asBoolean() ? 1 : 0);
			setDefaultValue(section, "joinNotification", Config.JOIN_NOTIFICATION_BY_DEFAULT.asBoolean() ? 1 : 0);

			playerData.save();
		}
	}

	private static void setDefaultValue(ConfigurationSection section, String path, long value) {
		if (!section.isSet(path)) {
			section.set(path, value);
		}
	}

	public static Map<String, Object> getPlayerData(Player player){
		Map<String, Object> data = new HashMap<>();
		if (isUsingMysql()) data = MySQLManager.getRewardsCooldown(player.getUniqueId());
		else {
			PlayerData playerData = PlayerData.getConfig(player.getUniqueId());
			for (Reward reward : DailyRewardsPlugin.getRewardManager().getRewards()) {
				final RewardType rewardType = reward.getType();
				data.put(rewardType.toString(), playerData.getString("rewards." + rewardType));
			}
			data.put("autoClaim", playerData.getString("rewards.autoClaim"));
			data.put("joinNotification", playerData.getString("rewards.joinNotification"));
		}
		return data;
	}

	public static void loadPlayerDataAsync(final Player player, final FindOneCallback callback) {
		DailyRewardsPlugin.get().runAsync(() -> {
			initiatePlayer(player);
			final Map<String, Object> result = getPlayerData(player);
			if (DailyRewardsPlugin.getFoliaLib().isFolia()) {
				DailyRewardsPlugin.getFoliaLib().getScheduler().runNextTick(wrappedTask -> callback.onQueryDone(result));
			} else Bukkit.getScheduler().runTask(DailyRewardsPlugin.get(), () -> callback.onQueryDone(result));
		});
	}

	public static boolean isUsingMysql() {
		return usingMysql;
	}

	public static void setUsingMysql(boolean usingMysql) {
		DataManager.usingMysql = usingMysql;
	}
}
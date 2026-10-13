package eu.athelion.dailyrewards.manager.cooldown;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.data.DataManager;
import eu.athelion.dailyrewards.manager.reward.Reward;
import eu.athelion.dailyrewards.user.User;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

	/**
	 * Applies the new cooldown to the cached user data right away and only then
	 * queues the persistent write. Doing it the other way around leaves a window
	 * in which the reward still reads as claimable, so a second click in the same
	 * tick would hand out the reward twice.
	 */
	public static void setCooldown(final User user, Reward reward) {
		final Map<String, Object> changes = new HashMap<String, Object>() {{
			put(reward.getName(), System.currentTimeMillis() + reward.getCooldown());
		}};

		user.updateData(changes);

		DailyRewardsPlugin.get().runAsync(() -> DataManager.updateValues(
				user.getPlayer().getUniqueId(),
				user,
				changes
		));
	}
}

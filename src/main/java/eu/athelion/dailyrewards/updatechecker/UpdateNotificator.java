package eu.athelion.dailyrewards.updatechecker;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.configuration.file.Config;
import eu.athelion.dailyrewards.util.TextUtil;
import eu.athelion.dailyrewards.util.VersionUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotificator implements Listener {

	public UpdateNotificator() {
		DailyRewardsPlugin.get().registerListeners(this);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void playerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		if (!player.isOp()) return;
		if (!Config.UPDATE_CHECKER.asBoolean()) return;
		if (VersionUtil.isLatestVersion()) return;

		TextComponent download = new TextComponent(TextUtil.colorize("&a&lDownload"));
		download.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/%E2%9A%A1-daily-weekly-monthly-rewards-mysql-oraxen-itemsadder-support-1-8-1-19-4.81780/"));

		TextComponent changelog = new TextComponent(TextUtil.colorize("&a&lChangelog"));
		changelog.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/%E2%9A%A1-daily-weekly-monthly-rewards-mysql-oraxen-itemsadder-support-1-8-1-19-4.81780/updates"));

		TextComponent upgrade = new TextComponent(TextUtil.colorize("&6&lUpgrade"));
		upgrade.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/%E2%9C%A8-ultimaterewards-create-a-whole-network-reward-system-with-ease-1-8-1-19-4.108055/"));

		TextComponent donate = new TextComponent(TextUtil.colorize("&a&lSupport"));
		donate.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/kcxYUQTy6A"));


		DailyRewardsPlugin.get().runAtEntityLater(player, () -> {
			player.sendMessage(" ");
			player.sendMessage(TextUtil.colorize("&7There is a new version of &aDailyRewards&7 available."));
			player.spigot().sendMessage(download, new TextComponent(TextUtil.colorize(" &8| ")), upgrade, new TextComponent(TextUtil.colorize(" &8| ")), changelog, new TextComponent(TextUtil.colorize(" &8| ")), donate);
			player.sendMessage(TextUtil.colorize("&8Latest version: &a" + DailyRewardsPlugin.getLatestVersion() + " &8| Your version: &c" + DailyRewardsPlugin.get().getDescription().getVersion()));
			player.sendMessage(" ");
		}, 35);
	}
}
package eu.athelion.dailyrewards.util;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerUtil {

    public static float getPlayersPlayTimeInMinutes(final Player player) {
        return (float) (player.getStatistic(Statistic.valueOf(VersionUtil.isLegacyVersion() ? "PLAY_ONE_TICK" : "PLAY_ONE_MINUTE")) / 20.0 / 60.0);
    }

    public static void playSound(Player player, String sound){
        Sound soundToPlay;
        try {
            sound = sound.toUpperCase(Locale.ENGLISH);
            soundToPlay = Sound.valueOf(sound);
            player.playSound(player.getLocation(), soundToPlay, 1f, 1f);
        } catch (IllegalArgumentException | NullPointerException ignored){
            //
        }
    }

    public static CompletableFuture<OfflinePlayer> getOfflinePlayer(final UUID uuid) {
        return DailyRewardsPlugin.get().completableFuture(() -> Bukkit.getOfflinePlayer(uuid));
    }

    public static CompletableFuture<OfflinePlayer> getOfflinePlayer(final String playerName) {
        return DailyRewardsPlugin.get().completableFuture(() -> Bukkit.getOfflinePlayer(playerName));
    }

    public static void spawnFirework(Location location){
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        fw.setMetadata("nodamage", new FixedMetadataValue(DailyRewardsPlugin.get(), true));
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.ORANGE, Color.YELLOW).withFade(Color.WHITE).with(FireworkEffect.Type.BALL).build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }
}

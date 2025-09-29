package eu.athelion.dailyrewards.user;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.api.event.ReminderReceiveEvent;
import eu.athelion.dailyrewards.data.DataManager;
import eu.athelion.dailyrewards.hook.HookManager;
import eu.athelion.dailyrewards.manager.reward.RewardType;
import eu.athelion.dailyrewards.manager.reward.task.JoinNotificationTask;
import eu.athelion.dailyrewards.manager.reward.task.AutoClaimTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class UserHandler implements Listener {
    private static final Map<UUID, User> usersHashMap = new ConcurrentHashMap<>();
    private static final Map<UUID, List<Consumer<User>>> pendingTasks = new ConcurrentHashMap<>();

    private final JoinNotificationTask joinNotificationTask;
    private final AutoClaimTask autoClaimTask;

    public UserHandler() {
        this.joinNotificationTask = new JoinNotificationTask(usersHashMap);
        if (DailyRewardsPlugin.getFoliaLib().isFolia()) {
            DailyRewardsPlugin.getFoliaLib().getScheduler()
                    .runTimerAsync(wrappedTask -> this.joinNotificationTask.get().run(), 45, 45);
        } else {
            this.joinNotificationTask.get()
                    .runTaskTimerAsynchronously(DailyRewardsPlugin.get(), 45, 45);
        }

        this.autoClaimTask = new AutoClaimTask(usersHashMap);
        if (DailyRewardsPlugin.getFoliaLib().isFolia()) {
            DailyRewardsPlugin.getFoliaLib().getScheduler()
                    .runTimerAsync(wrappedTask -> this.autoClaimTask.get().run(), 45, 45);
        } else {
            this.autoClaimTask.get()
                    .runTaskTimerAsynchronously(DailyRewardsPlugin.get(), 45, 45);
        }

        DailyRewardsPlugin.get().registerListeners(this);
    }

    public static User addUser(final User user) {
        usersHashMap.put(user.getPlayer().getUniqueId(), user);

        // Spusť čekající úkoly
        List<Consumer<User>> tasks = pendingTasks.remove(user.getPlayer().getUniqueId());
        if (tasks != null) {
            for (Consumer<User> task : tasks) {
                try {
                    task.accept(user);
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("Failed to run pending User task: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        return user;
    }

    public static User getUser(final UUID uuid) {
        return usersHashMap.get(uuid);
    }

    @NotNull
    public static User getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    public static User removeUser(final UUID uuid) {
        return usersHashMap.remove(uuid);
    }

    public static void runWhenReady(Player player, Consumer<User> action) {
        UUID uuid = player.getUniqueId();
        User user = usersHashMap.get(uuid);

        if (user != null) {
            action.accept(user);
        } else {
            pendingTasks.computeIfAbsent(uuid, k -> new ArrayList<>()).add(action);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        DataManager.loadPlayerDataAsync(player, data -> {
            User user = UserHandler.addUser(
                    new User(
                            player,
                            data
                    )
            );

            final Set<RewardType> availableRewards = user.getAvailableRewards();

            ReminderReceiveEvent reminderReceiveEvent = new ReminderReceiveEvent(player, availableRewards);
            DailyRewardsPlugin.get().runSync(() -> Bukkit.getPluginManager().callEvent(reminderReceiveEvent));

            if (reminderReceiveEvent.isCancelled()) {
                return;
            }

            if (HookManager.isAuthUsed()) {
                return;
            }

            joinNotificationTask.addUser(user);
            autoClaimTask.addUser(user);
        });
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        User user = UserHandler.removeUser(event.getPlayer().getUniqueId());
        joinNotificationTask.getPlayerRewardCheckTimes().remove(user);
    }

    public JoinNotificationTask getJoinNotificationTask() {
        return joinNotificationTask;
    }

    public AutoClaimTask getAutoClaimTask() {
        return autoClaimTask;
    }
}
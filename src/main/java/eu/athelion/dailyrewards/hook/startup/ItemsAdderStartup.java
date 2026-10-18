package eu.athelion.dailyrewards.hook.startup;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.lone.itemsadder.api.ItemsAdder;
import eu.athelion.dailyrewards.DailyRewardsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import java.util.logging.Level;

/** Kept separate so servers without ItemsAdder never resolve its API classes. */
public final class ItemsAdderStartup implements Listener {
    private final DailyRewardsPlugin plugin;
    private final StartupGate gate;

    public ItemsAdderStartup(DailyRewardsPlugin plugin, Runnable initialize) {
        this.plugin = plugin;
        this.gate = new StartupGate(ItemsAdder::areItemsLoaded, () -> {
            initialize.run();
            HandlerList.unregisterAll(this);
            plugin.getLogger().info("ItemsAdder registry ready; reward system initialized.");
        }, error -> {
            HandlerList.unregisterAll(this);
            plugin.getLogger().log(Level.SEVERE, "Cannot safely initialize rewards with ItemsAdder", error);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        });
    }

    public void start() {
        // Subscribe before probing: a completed or in-flight load cannot be missed.
        plugin.registerListeners(this);
        gate.tryStart();
        if (gate.state() == StartupGate.State.WAITING) {
            plugin.getLogger().info("Waiting for ItemsAdder item registry before enabling rewards.");
            plugin.runDelayed(() -> { if (plugin.isEnabled()) gate.timeout(); }, 2400L);
        }
    }

    @EventHandler
    public void onLoaded(ItemsAdderLoadDataEvent event) {
        // The event may be asynchronous; never load Bukkit items on that thread.
        plugin.runSync(() -> { if (plugin.isEnabled()) gate.tryStart(); });
    }

    public void close() {
        gate.close();
        HandlerList.unregisterAll(this);
    }
}

package eu.athelion.dailyrewards.hook.register;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.lone.itemsadder.api.ItemsAdder;
import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.hook.Hook;
import eu.athelion.dailyrewards.util.VersionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderHook implements Hook<Void>, Listener {
    private boolean isHooked;

    private boolean hook(){
        return VersionUtil.isLoaded("ItemsAdder");
    }

    @Override
    public void register() {
        isHooked = hook();
        if (isHooked) {
            DailyRewardsPlugin.get().registerListeners(this);
        }
    }

    @Override
    public boolean isOn() {
        return isHooked;
    }

    public boolean isReady() { return isHooked && ItemsAdder.areItemsLoaded(); }

    @Override
    public @Nullable Void getApi() {
        return null;
    }

    @EventHandler
    public void onItemsAdderLoad(final ItemsAdderLoadDataEvent event){
        DailyRewardsPlugin plugin = DailyRewardsPlugin.get();
        plugin.runSync(() -> {
            if (plugin.isEnabled() && plugin.isInitialized() && ItemsAdder.areItemsLoaded()) {
                plugin.reloadPlugin();
            }
        });
    }
}

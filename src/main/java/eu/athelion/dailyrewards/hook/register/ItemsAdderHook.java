package eu.athelion.dailyrewards.hook.register;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import dev.lone.itemsadder.api.ItemsAdder;
import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.hook.Hook;
import eu.athelion.dailyrewards.util.VersionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ItemsAdderHook implements Hook<Void>, Listener {
    private boolean isHooked;
    // ItemsAdder 5 throws from CustomStack until its registry is built, so items are only looked up once the data is loaded
    private boolean isDataLoaded;

    private boolean hook(){
        return VersionUtil.isLoaded("ItemsAdder");
    }

    @Override
    public void register() {
        isHooked = hook();
        if (isHooked) {
            isDataLoaded = areItemsLoaded();
            DailyRewardsPlugin.get().registerListeners(this);
        }
    }

    @Override
    public boolean isOn() {
        return isHooked;
    }

    @Override
    public @Nullable Void getApi() {
        return null;
    }

    /**
     * Returns false when the item does not exist or ItemsAdder has not loaded its items yet.
     */
    public boolean hasItem(@NotNull final String id) {
        if (!isHooked || !isDataLoaded) {
            return false;
        }

        try {
            return CustomStack.isInRegistry(id);
        } catch (RuntimeException e) {
            DailyRewardsPlugin.get().getLogger().log(Level.WARNING, "Failed to look up ItemsAdder item '" + id + "'", e);
            return false;
        }
    }

    public @Nullable ItemStack getItem(@NotNull final String id) {
        if (!hasItem(id)) {
            return null;
        }

        final CustomStack customStack = CustomStack.getInstance(id);
        return customStack == null ? null : customStack.getItemStack();
    }

    private boolean areItemsLoaded() {
        try {
            return ItemsAdder.areItemsLoaded();
        } catch (RuntimeException | LinkageError e) {
            return false;
        }
    }

    @EventHandler
    public void onItemsAdderLoad(final ItemsAdderLoadDataEvent event){
        isDataLoaded = true;
        DailyRewardsPlugin.get().reloadPlugin();
    }
}

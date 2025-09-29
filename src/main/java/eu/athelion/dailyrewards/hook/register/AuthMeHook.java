package eu.athelion.dailyrewards.hook.register;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.hook.Hook;
import eu.athelion.dailyrewards.hook.HookManager;
import eu.athelion.dailyrewards.user.User;
import eu.athelion.dailyrewards.user.UserHandler;
import eu.athelion.dailyrewards.util.VersionUtil;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.Nullable;

public class AuthMeHook implements Hook<Void> {
    private boolean isHooked;

    private boolean hook(){
        return VersionUtil.isLoaded("AuthMe");
    }

    @Override
    public void register() {
        isHooked = hook();
        if (isHooked) {
            DailyRewardsPlugin.get().registerListeners(new AuthMeLoginListener());
            HookManager.setAuthUsed(true);
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

    private static class AuthMeLoginListener extends LoginListener<LoginEvent> {
        @Override
        @EventHandler
        public void onLogin(final LoginEvent event) {
            UserHandler.runWhenReady(event.getPlayer(), (user) -> {
                DailyRewardsPlugin.getUserHandler().getAutoClaimTask().addUser(user);
                DailyRewardsPlugin.getUserHandler().getJoinNotificationTask().addUser(user);
            });
        }
    }
}
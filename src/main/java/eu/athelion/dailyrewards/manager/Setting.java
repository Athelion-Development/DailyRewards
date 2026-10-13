package eu.athelion.dailyrewards.manager;

import eu.athelion.dailyrewards.configuration.file.Lang;

public enum Setting {
    JOIN_NOTIFICATION("joinNotification", Lang.JOIN_NOTIFICATION_SETTING_NAME),
    AUTO_CLAIM("autoClaim", Lang.JOIN_AUTO_CLAIM_SETTING_NAME);

    private final String tag;
    private final Lang name;
    Setting(String tag, Lang name) {
        this.tag = tag;
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public Lang getName() {
        return name;
    }
}

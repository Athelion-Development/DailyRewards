package eu.athelion.dailyrewards.data;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerData extends YamlConfiguration {

	private static final Map<UUID, PlayerData> configurations = new HashMap<>();
	private static final File userDataFolder = new File(DailyRewardsPlugin.get().getDataFolder(), "userdata");

	private final UUID uuid;
	private final File file;

	public PlayerData(UUID uniqueId) {
		super();
		this.uuid = uniqueId;
		this.file = new File(userDataFolder, String.format("%s.yml", uniqueId));
		if (file.exists()) this.reload();
	}

	public static boolean exists(UUID id) { return new File(DailyRewardsPlugin.get().getDataFolder(), "userdata" + File.separator + id.toString() + ".yml").exists();}

	public static PlayerData getConfig(UUID uniqueId) {
		synchronized (configurations) {
			if (configurations.containsKey(uniqueId)) return configurations.get(uniqueId);
			final PlayerData dataConfig = new PlayerData(uniqueId);

			configurations.put(uniqueId, dataConfig);
			return dataConfig;
		}
	}

	/**
	 * Player data files, or an empty list when nothing has been saved yet.
	 * listFiles returns null for a missing folder, which used to blow up the import.
	 */
	public static List<File> getFiles() {
		final File[] files = userDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
		return files == null ? Collections.emptyList() : Arrays.asList(files);
	}

	public static void removeConfigs() {
		final Collection<PlayerData> oldConfigurations = new ArrayList<>(configurations.values());
		synchronized (configurations) {
			oldConfigurations.forEach(PlayerData::discard);
		}
	}

	private void reload() {
		synchronized (this) {
			try {
				this.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				throw new RuntimeException(e);
			}
        }
	}


	public void save() {
		synchronized (this) {
			try {
				this.save(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void discard(boolean save) {
		if (save) this.save();
		synchronized (configurations) {
			configurations.remove(uuid);
		}
	}

	public void discard() {
		this.discard(false);
	}
}
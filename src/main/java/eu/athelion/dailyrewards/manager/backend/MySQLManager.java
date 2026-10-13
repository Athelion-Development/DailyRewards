package eu.athelion.dailyrewards.manager.backend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.athelion.dailyrewards.DailyRewardsPlugin;
import eu.athelion.dailyrewards.data.DataManager;
import eu.athelion.dailyrewards.configuration.file.Config;
import eu.athelion.dailyrewards.manager.reward.RewardType;
import eu.athelion.dailyrewards.user.UserHandler;
import eu.athelion.dailyrewards.util.TextUtil;
import eu.athelion.dailyrewards.util.VersionUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLManager {

	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS Rewards(id varchar(255), daily long, weekly long, monthly long);";
	private static final String ADD_REWARD_COLUMN = "ALTER TABLE Rewards ADD COLUMN `%column%` int DEFAULT %default%;";
	private static final String SELECT = "SELECT %value% FROM Rewards WHERE id='%selector%';";
	private static final String INSERT = "INSERT INTO Rewards (%columns%) VALUES (%values%);";
	private static final String UPDATE = "UPDATE Rewards SET %values% WHERE id='%selector%';";

	private static HikariDataSource dataSource;

	public static void init(){
		if (Config.BACKEND.asString().equalsIgnoreCase("SQLITE"))
			return;

		String host = Config.MYSQL_IP.asString();
		String port = Config.MYSQL_PORT.asString();
		String databaseName = Config.MYSQL_DATABASE_NAME.asString();
		String username = Config.MYSQL_USERNAME.asString();
		String password = Config.MYSQL_PASSWORD.asString();

		int maxPoolSize = Config.MYSQL_POOL_SETTINGS_MAXIMUM_POOL_SIZE.asInt();
		int minimumIdle = Config.MYSQL_POOL_SETTINGS_MINIMUM_IDLE.asInt();
		long maxLifetime = Config.MYSQL_POOL_SETTINGS_MAXIMUM_LIFETIME.asLong();
		long connectionTimeout = Config.MYSQL_POOL_SETTINGS_CONNECTION_TIMEOUT.asLong();

		Map<String, String> properties = Config.MYSQL_PROPERTIES.asStringMap();

		HikariConfig config = new HikariConfig();

		String credentials = host + ":" + port + "/" + databaseName;
		String parameters = "?testConnectOnCheckout=true" +
				"&idleConnectionTestPeriod=3600" +
				"&rewriteBatchedStatements=true";

		BackendType backendType = BackendType.valueOf(Config.BACKEND.asUppercase());
		String databaseType = "";
		String driverClassName = "";
		switch (backendType) {
			case MYSQL:
				if (VersionUtil.isLegacyVersion()) driverClassName = "com.mysql.jdbc.Driver";
				else driverClassName = "com.mysql.cj.jdbc.Driver";
				databaseType = "jdbc:mysql://";
				break;
			case MARIADB:
				driverClassName = "org.mariadb.jdbc.Driver";
				databaseType = "jdbc:mariadb://";
				break;
			case POSTGRESQL:
				driverClassName = "org.postgresql.Driver";
				databaseType = "jdbc:postgresql://";
				break;
		}

		config.setDriverClassName(driverClassName);
		config.setJdbcUrl(databaseType + credentials + parameters);

		config.setUsername(username);
		config.setPassword(password);

		config.setPoolName("dailyrewards-pool");
		config.setMaximumPoolSize(maxPoolSize);
		config.setMinimumIdle(minimumIdle);
		config.setMaxLifetime(maxLifetime);
		config.setConnectionTimeout(connectionTimeout);

		config.setIdleTimeout(0);

		properties.forEach(config::addDataSourceProperty);

		dataSource = new HikariDataSource(config);

		try (Connection connection = getConnection()) {
			connection.prepareStatement(CREATE_TABLE).execute();
			addNewRewardColumn(
					new HashMap<String, String>() {{
						put("joinNotification", Config.JOIN_NOTIFICATION_BY_DEFAULT.asBoolean() ? "1" : "0");
					}}
			);
			addNewRewardColumn(
					new HashMap<String, String>() {{
						put("autoClaim", Config.AUTO_CLAIM_REWARDS_ON_JOIN_BY_DEFAULT.asBoolean() ? "1" : "0");
					}}
			);

			DataManager.setUsingMysql(true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void createPlayer(final String uuid) {
		if (MySQLManager.playerExists(uuid)) {
			return;
		}

		final long currentTimeInMillis = System.currentTimeMillis();
		try (Connection connection = getConnection()) {
			String statement = INSERT
					.replace("%columns%", "id, daily, weekly, monthly")
					.replace("%values%", "'" + uuid + "'" + ", " +
							(Config.DAILY_AVAILABLE_AFTER_FIRST_JOIN.asBoolean() ? 0 : currentTimeInMillis + Config.DAILY_COOLDOWN.asLong() * 60 * 60 * 1000) + ", " +
							(Config.WEEKLY_AVAILABLE_AFTER_FIRST_JOIN.asBoolean() ? 0 : currentTimeInMillis + Config.WEEKLY_COOLDOWN.asLong() * 60 * 60 * 1000) + ", " +
							(Config.MONTHLY_AVAILABLE_AFTER_FIRST_JOIN.asBoolean() ? 0 : currentTimeInMillis + Config.MONTHLY_COOLDOWN.asLong() * 60 * 60 * 1000));
			connection.prepareStatement(statement)
					.execute();
		} catch (SQLException ex) {
			DailyRewardsPlugin.get().getLogger().warning("Failed to create player " + uuid + "!\n" + ex.getLocalizedMessage());
		}
	}

	public static boolean playerExists(final String uuid) {
		try (Connection connection = getConnection()) {
			return connection.prepareStatement(SELECT
							.replace("%value%", "daily")
							.replace("%selector%", uuid))
					.executeQuery()
					.next();
		} catch (SQLException ex) {
			DailyRewardsPlugin.get().getLogger().warning("Failed to check if player " + uuid + " exists!\n" + ex.getLocalizedMessage());
			return false;
		}
	}

	public static boolean updatePlayer(UUID uniqueId, Map<String, Object> changes) {
		if (DailyRewardsPlugin.isWithinMainThread()) {
			DailyRewardsPlugin.get().getLogger().warning("Action cannot be performed within main thread!");
			return false;
		}

		Optional.ofNullable(UserHandler.getUser(uniqueId)).ifPresent(
				user -> user.updateData(changes)
		);

		final StringBuilder builder = new StringBuilder();
		Iterator<String> keys = changes.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = changes.get(key);
			builder.append(key)
					.append("='")
					.append(value)
					.append("'")
					.append(keys.hasNext()
							? ","
							: "");
		}

		try (Connection connection = getConnection()) {
			String command = TextUtil.replaceString(UPDATE,
					new HashMap<String, String>() {{
						put("%values%", builder.toString());
						put("%selector%", uniqueId.toString());
					}});
			//DailyRewardsPlugin.get().getLogger().info(command);
			connection.prepareStatement(
					command
			).executeUpdate();
		} catch (SQLException ex) {
			DailyRewardsPlugin.get().getLogger().warning("Failed to update cooldown for " + uniqueId + "!\n" + ex.getLocalizedMessage());
			return false;
		}

		return true;
	}

	public static Map<String, Object> getRewardsCooldown(final UUID uuid) {
		if (DailyRewardsPlugin.isWithinMainThread()) {
			DailyRewardsPlugin.get().getLogger().warning("Action cannot be performed within main thread!");
			return Collections.emptyMap();
		}

		final Map<String, Object> cooldowns = new HashMap<>();
		try (Connection connection = getConnection()) {
			final ResultSet resultSet = connection.prepareStatement(SELECT
					.replace("%value%", "*")
					.replace("%selector%", uuid.toString())
			).executeQuery();

			while (resultSet.next()){
				cooldowns.put(RewardType.DAILY.toString(), resultSet.getLong("daily"));
				cooldowns.put(RewardType.WEEKLY.toString(), resultSet.getLong("weekly"));
				cooldowns.put(RewardType.MONTHLY.toString(), resultSet.getLong("monthly"));

				cooldowns.put("autoClaim", resultSet.getObject("autoClaim"));
				cooldowns.put("joinNotification", resultSet.getObject("joinNotification"));
			}

		} catch (SQLException exception) {
			DailyRewardsPlugin.get().getLogger().warning("Failed to load rewards for " + uuid + "!\n" + exception.getLocalizedMessage());
			return Collections.emptyMap();
		}

		return cooldowns;
	}

	public static void addNewRewardColumn(Map<String, String> keyAndValues) {
		try (Connection connection = getConnection();
			 java.sql.Statement statement = connection.createStatement()) {
			for (Map.Entry<String, String> entry : keyAndValues.entrySet()) {
				String columnName = entry.getKey();
				String defaultValue = entry.getValue();
				if (!connection.getMetaData().getColumns(null, null, "Rewards", columnName).next())
					statement.addBatch(TextUtil.replaceString(ADD_REWARD_COLUMN,
							new HashMap<String, String>() {{
								put("%column%", columnName);
								put("%default%", defaultValue);
							}}
					));
			}

			statement.executeBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			throw new SQLException("Unable to get a connection from the pool because the dataSource is null");
		}

		Connection connection = dataSource.getConnection();
		if (connection == null) {
			throw new SQLException("Unable to get a connection from the pool.");
		}
		return connection;
	}
}
# DailyRewards

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-DailyRewards-orange.svg)](https://www.spigotmc.org/resources/81780/)
[![Discord](https://img.shields.io/badge/Discord-Athelion-5865F2.svg)](https://discord.athelion.eu/)

DailyRewards is a lightweight Minecraft plugin that gives players **daily, weekly and monthly rewards**. They claim them from an in-game menu, or the plugin can claim them automatically when they join. It runs on Spigot, Paper and Folia.

> Need more than three reward types? [UltimateRewards](https://www.spigotmc.org/resources/108055/) lets you create unlimited custom rewards across a whole network.

## Features

- **Three reward types** (daily, weekly, monthly), each with its own cooldown, menu slots, items, sounds and reward actions
- **Premium rewards**: a separate set of actions for players with the `.premium` permission
- **Reward actions**: console and player commands, chat messages, action bar, titles, sounds and fireworks
- **Join notifications** and **auto-claim on join**, which each player can switch on or off in the settings menu
- **Requirements**: a minimum playtime before a player can claim, a full-inventory check, and disabled worlds
- **Storage**: SQLite (default), MySQL, MariaDB or PostgreSQL, with an `import` command to move local data into a database
- **Custom items**: player heads (Base64 textures), custom model data, and [ItemsAdder](https://itemsadder.devs.beer/) and [Oraxen](https://oraxen.com/) items
- **Integrations**: [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) placeholders, and [AuthMe](https://www.spigotmc.org/resources/6269/) (notifications and auto-claim wait until the player logs in)
- **13 languages**: English, Czech, Chinese, Finnish, French, German, Polish, Portuguese, Romanian, Russian, Spanish, Turkish and Ukrainian
- **Developer API**: cancellable Bukkit events for claims, auto-claims and reminders

## Installation

1. Download the latest release from [SpigotMC](https://www.spigotmc.org/resources/81780/).
2. Put the jar in your server's `plugins/` folder.
3. Start the server. This generates `plugins/DailyRewards/config.yml` and the `lang/` folder.
4. Edit the config to your liking and run `/reward reload`.

Optional dependencies: PlaceholderAPI, ItemsAdder, Oraxen, AuthMe.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rewards` | Opens the rewards menu | `dailyreward.use` |
| `/reward claim <daily\|weekly\|monthly> [player]` | Claims a reward, for yourself or for another player | reward permission / `dailyreward.claim.others` |
| `/reward reset <player> <daily\|weekly\|monthly\|all>` | Resets a player's cooldowns | `dailyreward.manage` |
| `/reward settings` | Opens the personal settings menu | `dailyreward.settings` |
| `/reward toggle <setting>` | Toggles a personal setting | `dailyreward.settings.<setting>` |
| `/reward import` | Imports all local data into the configured database | `dailyreward.admin` |
| `/reward reload` | Reloads the configuration and language files | `dailyreward.reload` |
| `/reward help` | Lists the commands you can use | `dailyreward.help` |

Aliases: `/reward` = `/rw`, `/dreward`; `/rewards` = `/rws`, `/drewards`.

## Permissions

| Permission | Description |
|---|---|
| `dailyreward.admin` | Grants every permission below |
| `dailyreward.use` | Open the rewards menu |
| `dailyreward.daily` / `.weekly` / `.monthly` | Claim that reward |
| `dailyreward.daily.premium` / `.weekly.premium` / `.monthly.premium` | Receive the premium version of that reward |
| `dailyreward.claim.others` | Claim rewards for other players |
| `dailyreward.manage` | Reset other players' cooldowns |
| `dailyreward.reload` | Reload the plugin |
| `dailyreward.help` | View the help message |
| `dailyreward.settings` | Open the settings menu |
| `dailyreward.settings.joinNotification` | Toggle join notifications |
| `dailyreward.settings.autoClaim` | Toggle auto-claim on join |
| `dailyreward.requiredPlaytime.bypass` | Skip the minimum playtime requirement |
| `dailyreward.requiredFreeSlots.bypass` | Skip the full-inventory check |
| `dailyreward.disabledWorlds.bypass` | Claim rewards in disabled worlds |

## Configuration

Everything is set in `config.yml`. Each reward type has its own section. Here is the daily one:

```yaml
daily-enabled: true
daily-cooldown: 24                 # in hours
daily-cooldown-format: "%hours%:%minutes%:%seconds%"
daily-positions:
  - 20                             # menu slots
daily-sound: ENTITY_PLAYER_LEVELUP
daily-available-item: "CHEST_MINECART"
daily-unavailable-item: "MINECART"
daily-rewards:
  - "[console] give %player% iron_ingot 8"
  - "[actionbar] &aClaimed daily reward!"
daily-premium-rewards:             # used instead when the player has dailyreward.daily.premium
  - "[console] give %player% iron_ingot 16"
```

### Reward actions

| Action | Effect |
|---|---|
| `[console] <command>` | Runs the command from the console |
| `[player] <command>` | Runs the command as the player |
| `[message] <text>` | Sends the player a chat message |
| `[actionbar] <text>` | Shows the text in the action bar |
| `[title] <text>` / `[subtitle] <text>` | Shows a title or subtitle |
| `[sound] <sound>` | Plays a sound to the player |
| `[firework]` | Launches a firework above the player |

### Items

Any item option (`*-item`) accepts:

- a material name, e.g. `CHEST_MINECART` (falls back to stone if the name is invalid)
- a Base64 head texture
- `CustomModel[MATERIAL]{data}` for custom model data, e.g. `CustomModel[PAPER]{1001}`
- an ItemsAdder ID, e.g. `namespace:item_id`
- an Oraxen item ID

Item and sound names differ between 1.12 and older and 1.13 and newer. The defaults use the 1.13+ names.

### Database

Set `backend` to `SQLITE`, `MYSQL`, `MARIADB` or `POSTGRESQL` and fill in the `mysql-*` connection settings. To move existing SQLite data into the new database, run `/reward import`.

## Placeholders

These need PlaceholderAPI.

| Placeholder | Value |
|---|---|
| `%dailyrewards_available%` | How many rewards the player can claim right now |
| `%dailyrewards_cooldown_daily%` | Time left until the daily reward is available, or the "available" text |
| `%dailyrewards_cooldown_weekly%` | The same for the weekly reward |
| `%dailyrewards_cooldown_monthly%` | The same for the monthly reward |

## Developer API

DailyRewards fires these Bukkit events from `eu.athelion.dailyrewards.api.event`:

| Event | Cancellable | Fired when |
|---|---|---|
| `PlayerPreClaimRewardEvent` | Yes | Before a reward is given. It exposes the commands that will run. |
| `PlayerClaimRewardEvent` | No | After a reward has been claimed |
| `AutoClaimEvent` | Yes | Rewards are about to be auto-claimed on join |
| `ReminderReceiveEvent` | Yes | A player is about to get the "rewards available" notification |

```java
@EventHandler
public void onClaim(PlayerClaimRewardEvent event) {
    Player player = event.getClaimer();
    RewardType type = event.getClaimedReward();
    // ...
}
```

## Building

You need JDK 8 or newer and Maven:

```bash
mvn clean package
```

The shaded jar is written to `target/`.

## Support

Questions, bug reports and suggestions are welcome on our [Discord](https://discord.athelion.eu/) or in [GitHub issues](https://github.com/Athelion-Development/DailyRewards/issues).

## License

DailyRewards is released under the [MIT License](LICENSE).

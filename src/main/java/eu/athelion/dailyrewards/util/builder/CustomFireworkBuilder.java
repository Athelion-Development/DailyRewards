package eu.athelion.dailyrewards.util.builder;

import eu.athelion.dailyrewards.DailyRewardsPlugin;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CustomFireworkBuilder {
    private List<Color> colors;
    private FireworkEffect.Type type;
    private int power;

    private CustomFireworkBuilder() {
        this.colors = new ArrayList<Color>() {{
            add(Color.BLUE);
        }};

        this.type = FireworkEffect.Type.BALL;
        this.power = 1;
    }

    @NotNull
    public static CustomFireworkBuilder builder() {
        return new CustomFireworkBuilder();
    }

    @NotNull
    public CustomFireworkBuilder setColors(@NotNull List<Color> colors) {
        this.colors = colors;
        return this;
    }

    @NotNull
    public CustomFireworkBuilder setType(@NotNull FireworkEffect.Type type) {
        this.type = type;
        return this;
    }

    @NotNull
    public CustomFireworkBuilder setPower(int power) {
        this.power = power;
        return this;
    }

    public void launch(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        firework.setMetadata("nodamage", new FixedMetadataValue(DailyRewardsPlugin.get(), true));
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
        for (Color color : colors) {
            effectBuilder.withColor(color);
        }
        effectBuilder.with(this.type);

        fireworkMeta.addEffect(effectBuilder.build());
        fireworkMeta.setPower(this.power);
        firework.setFireworkMeta(fireworkMeta);
    }

    public static CustomFireworkBuilder fromString(String configString) {
        CustomFireworkBuilder builder = new CustomFireworkBuilder();

        if (configString == null || configString.isEmpty()) {
            return builder;
        }

        String[] keyValuePairs = configString.split(",");
        for (String pair : keyValuePairs) {
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                continue;
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            switch (key) {
                case "colors":
                    List<Color> colors = parseColors(value);
                    builder.setColors(colors);
                    break;
                case "type":
                    builder.setType(FireworkEffect.Type.valueOf(value.toUpperCase()));
                    break;
                case "power":
                    try {
                        int power = Integer.parseInt(value);
                        builder.setPower(power);
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                default:
            }
        }

        return builder;
    }

    private static List<Color> parseColors(String colorsString) {
        List<Color> colors = new ArrayList<>();
        String[] colorStrings = colorsString.substring(colorsString.indexOf('{') + 1, colorsString.indexOf('}')).split(";");
        for (String colorString : colorStrings) {
            try {
                Color color = Color.fromRGB(Integer.parseInt(colorString, 16));
                colors.add(color);
            } catch (NumberFormatException e) {
                System.out.println("Invalid color: " + colorString);
            }
        }
        return colors;
    }
}
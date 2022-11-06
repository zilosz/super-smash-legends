package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.HotbarItem;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public abstract class Ability extends Attribute {
    protected final Section config;
    @Getter protected final int slot;
    protected HotbarItem hotbarItem;

    public Ability(SuperSmashLegends plugin, Section config, Kit kit, int slot) {
        super(plugin, kit);
        this.config = config;
        this.slot = slot;
    }

    public abstract String getUseType();

    public List<String> getDescription() {
        return config.getStringList("Description");
    }

    public String getDisplayName() {
        return MessageUtils.color(kit.getColor() + config.getString("Name"));
    }

    public ItemStack buildItem() {
        return new ItemBuilder<>(YamlReader.readItemStack(config.getSection("Item")))
                .setName(getDisplayName())
                .setLore(new Replacers()
                    .add("DESCRIPTION", getDescription())
                    .replaceLines(Arrays.asList(
                            "&3&lDescription",
                            "{DESCRIPTION}"
                    ))
        ).get();
    }

    public void sendDescription() {
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);

        new Replacers()
                .add("COLOR", kit.getColor())
                .add("DISPLAY_NAME", getDisplayName())
                .add("USE_TYPE", getUseType())
                .add("DESCRIPTION", getDescription())
                .replaceLines(Arrays.asList(
                        "{COLOR}---------------------------------",
                        "&l{DISPLAY_NAME} &7- &6{USE_TYPE}",
                        "{DESCRIPTION}",
                        "{COLOR}---------------------------------"
                )).forEach(player::sendMessage);
    }

    @Override
    public void equip() {
        super.equip();
        hotbarItem = new HotbarItem(player, buildItem(), slot);
        hotbarItem.setAction(e -> sendDescription());
        hotbarItem.register(plugin);
    }

    @Override
    public void destroy() {
        super.destroy();
        hotbarItem.destroy();
    }
}

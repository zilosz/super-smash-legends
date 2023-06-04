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
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public abstract class Ability extends Attribute implements Nameable {
    protected final Section config;
    @Getter @Setter protected int slot;
    @Getter protected HotbarItem hotbarItem;

    public Ability(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, kit);
        this.config = config;
    }

    public abstract String getUseType();

    public List<String> getDescription() {
        return config.getStringList("Description");
    }

    @Override
    public String getDisplayName() {
        return MessageUtils.color(this.kit.getColor() + config.getString("Name"));
    }

    public String getBoldedDisplayName() {
        return MessageUtils.color(this.kit.getColor() + "&l" + config.getString("Name"));
    }

    public Material getMaterial() {
        return Material.valueOf(config.getString("Item.Material"));
    }

    public ItemStack buildItem() {

        Replacers replacers = new Replacers()
                .add("DESCRIPTION", getDescription());

        List<String> lore = replacers.replaceLines(Arrays.asList(
                "&3&lDescription",
                "{DESCRIPTION}"
        ));

        return new ItemBuilder<>(YamlReader.stack(config.getSection("Item")))
                .setName(getBoldedDisplayName())
                .setLore(lore)
                .get();
    }

    public void sendDescription() {
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);

        Replacers replacers = new Replacers()
                .add("COLOR", this.kit.getColor())
                .add("DISPLAY_NAME", getDisplayName())
                .add("USE_TYPE", getUseType())
                .add("DESCRIPTION", getDescription());

        replacers.replaceLines(Arrays.asList(
                "{COLOR}-------------------------------------",
                "&l{DISPLAY_NAME} &7- &6{USE_TYPE}",
                "{DESCRIPTION}",
                "{COLOR}-------------------------------------"
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
    public void unequip() {
        hotbarItem.destroy();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        hotbarItem.setAction(null);
    }
}

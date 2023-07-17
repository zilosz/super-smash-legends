package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.HotbarItem;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import com.github.zilosz.ssl.utils.message.Replacers;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public abstract class Ability extends Attribute {
    protected YamlDocument config;
    @Getter protected AbilityType type;
    protected int slot;
    @Getter protected HotbarItem hotbarItem;

    public void initAbility(YamlDocument config, AbilityType type, int slot) {
        this.config = config;
        this.type = type;
        this.slot = slot;
    }

    @Override
    public void equip() {
        super.equip();

        Replacers replacers = new Replacers().add("DESCRIPTION", this.getDescription());
        List<String> lore = replacers.replaceLines(Arrays.asList("&3&lDescription", "{DESCRIPTION}"));
        ItemStack baseStack = YamlReader.stack(this.config.getSection("Item"));
        ItemStack stack = new ItemBuilder<>(baseStack).setName(this.getBoldedDisplayName()).setLore(lore).get();

        this.hotbarItem = new HotbarItem(this.player, stack, this.slot);
        this.hotbarItem.setAction(e -> this.sendDescription());
        this.hotbarItem.registerAndShow(SSL.getInstance());
    }

    @Override
    public void unequip() {
        this.hotbarItem.destroy();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.hotbarItem.setAction(null);
    }

    public void sendDescription() {
        this.player.playSound(this.player.getLocation(), Sound.ORB_PICKUP, 1, 1);

        Replacers replacers = new Replacers().add("COLOR", this.kit.getColor().getChatSymbol())
                .add("DISPLAY_NAME", this.getDisplayName())
                .add("USE_TYPE", this.getUseType())
                .add("DESCRIPTION", this.getDescription());

        List<String> lines = Arrays.asList(
                "{COLOR}-------------------------------------",
                "&l{DISPLAY_NAME} &7- &6{USE_TYPE}",
                "{DESCRIPTION}",
                "{COLOR}-------------------------------------"
        );

        for (String line : replacers.replaceLines(lines)) {
            this.player.sendMessage(line);
        }
    }

    private List<String> getDescription() {
        return this.config.getStringList("Description");
    }

    public String getBoldedDisplayName() {
        return MessageUtils.color(this.kit.getColor().getChatSymbol() + "&l" + this.config.getString("Name"));
    }

    public String getDisplayName() {
        return MessageUtils.color(this.kit.getColor().getChatSymbol() + this.config.getString("Name"));
    }

    public abstract String getUseType();
}

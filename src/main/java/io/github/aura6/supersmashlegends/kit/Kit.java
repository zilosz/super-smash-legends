package io.github.aura6.supersmashlegends.kit;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.Skin;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Kit {
    protected final SuperSmashLegends plugin;
    private final Section config;
    private final List<Attribute> attributes = new ArrayList<>();
    @Getter private Player player;

    public Kit(SuperSmashLegends plugin, Section config) {
        this.plugin = plugin;
        this.config = config;

        IntStream.range(0, 9).forEach(slot -> config.getOptionalString("Abilities." + slot).ifPresent(abilityName -> {
            Ability ability = plugin.getResources().loadAbility(abilityName, this, slot);
            attributes.add(ability);
        }));
    }

    public Kit copy() {
        return new Kit(plugin, config);
    }

    public String getConfigName() {
        return config.getString("ConfigName");
    }

    public String getColor() {
        return config.getString("Color");
    }

    public List<String> getDescription() {
        return config.getStringList("Description");
    }

    public ItemStack getItemStack() {
        return YamlReader.readItemStack(config.getSection("Item"));
    }

    public String getDisplayName() {
        return MessageUtils.color(getColor() + config.getString("Name"));
    }

    public double getRegen() {
        return config.getDouble("Regen");
    }

    public double getArmor() {
        return config.getDouble("Armor");
    }

    public double getDamage() {
        return config.getDouble("Damage");
    }

    public double getKb() {
        return config.getDouble("Kb");
    }

    public double getJumpPower() {
        return config.getDouble("Jump.Power");
    }

    public double getJumpHeight() {
        return config.getDouble("Jump.Height");
    }

    public int getPrice() {
        return config.getInt("Price");
    }

    public String getSkinName() {
        return config.getString("Skin");
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public void equip(Player player) {
        this.player = player;
        attributes.forEach(Attribute::equip);
        Skin.fromMojang(getSkinName()).ifPresent(skin -> skin.apply(plugin, player));
    }

    public void activate() {
        attributes.forEach(Attribute::activate);
    }

    public void destroy() {
        attributes.forEach(Attribute::destroy);
    }
}

package io.github.aura6.supersmashlegends.kit;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.implementation.Energy;
import io.github.aura6.supersmashlegends.attribute.implementation.Melee;
import io.github.aura6.supersmashlegends.attribute.implementation.Regeneration;
import io.github.aura6.supersmashlegends.attribute.implementation.Jump;
import io.github.aura6.supersmashlegends.utils.Noise;
import io.github.aura6.supersmashlegends.utils.Reflector;
import io.github.aura6.supersmashlegends.utils.Skin;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Kit {
    protected final SuperSmashLegends plugin;

    private final Section config;
    private final List<Attribute> attributes = new ArrayList<>();

    @Getter private Player player;
    @Getter private final Jump jump;

    @Getter private final Skin skin;

    @SuppressWarnings("unchecked")
    public Kit(SuperSmashLegends plugin, Section config) {
        this.plugin = plugin;
        this.config = config;

        this.skin = Skin.fromMojang(this.getSkinName());

        jump = new Jump(plugin, this);
        attributes.add(jump);

        attributes.add(new Regeneration(plugin, this));
        attributes.add(new Melee(plugin, this));

        if (config.isNumber("Energy")) {
            attributes.add(new Energy(plugin, this));
        }

        IntStream.range(0, 9).forEach(slot -> config.getOptionalSection("Abilities." + slot).ifPresent(abilityConfig -> {
            String name = Ability.class.getPackageName() + ".implementation." + abilityConfig.getString("ConfigName");
            Class<? extends Ability> clazz = (Class<? extends Ability>) Reflector.loadClass(name);
            Ability ability = Reflector.newInstance(clazz, plugin, abilityConfig, this);
            ability.setSlot(slot);
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

    public String getDisplayName() {
        return MessageUtils.color(getColor() + config.getString("Name"));
    }

    public String getBoldedDisplayName() {
        return MessageUtils.color(getColor() + "&l" + config.getString("Name"));
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

    public int getJumpCount() {
        return config.getOptionalInt("Jump.Count").orElse(1);
    }

    public Noise getJumpNoise() {
        return YamlReader.noise(config.getSection("Jump.Sound"));
    }

    public float getEnergy() {
        return config.getFloat("Energy");
    }

    public String getSkinName() {
        return this.config.getString("Skin");
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public void equip(Player player) {
        this.player = player;
        giveItems();
    }

    public void giveItems() {
        attributes.forEach(Attribute::equip);
        System.out.println("gave items for " + this.player.getName());
    }

    public void activate() {
        attributes.forEach(Attribute::activate);
        System.out.println("activated for " + this.player.getName());
    }

    public void deactivate() {
        attributes.forEach(Attribute::deactivate);
        System.out.println("deactivated for " + this.player.getName());
    }

    public void destroy() {
        attributes.forEach(Attribute::destroy);
        System.out.println("destroyed for " + this.player.getName());
    }
}

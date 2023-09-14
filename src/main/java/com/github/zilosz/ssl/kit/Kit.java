package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.AbilityType;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.implementation.Energy;
import com.github.zilosz.ssl.attribute.implementation.Jump;
import com.github.zilosz.ssl.attribute.implementation.Melee;
import com.github.zilosz.ssl.attribute.implementation.Regeneration;
import com.github.zilosz.ssl.util.Noise;
import com.github.zilosz.ssl.util.Skin;
import com.github.zilosz.ssl.util.effects.ColorType;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.message.MessageUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Kit {
    private final YamlDocument config;
    @Getter @EqualsAndHashCode.Include private final KitType type;
    @Getter private final Jump jump;
    @Getter private final Regeneration regeneration;
    @Getter private final Melee melee;
    @Getter private final Energy energy;
    @Getter private final Skin skin;
    private final List<Attribute> attributes = new ArrayList<>();
    @Getter private Player player;

    public Kit(YamlDocument config, KitType type) {
        this.config = config;
        this.type = type;

        this.skin = Skin.fromMojang(this.getSkinName());
        this.jump = this.addAttribute(new Jump());
        this.regeneration = this.addAttribute(new Regeneration());
        this.melee = this.addAttribute(new Melee());
        this.energy = this.addAttribute(new Energy());

        Optional.ofNullable(config.getSection("Abilities")).ifPresent(abilities -> {

            IntStream.range(0, 6).forEach(slot -> {

                Optional.ofNullable(abilities.getString(String.valueOf(slot))).ifPresent(abilityName -> {
                    AbilityType abilityType = AbilityType.valueOf(abilityName);
                    YamlDocument abilityConfig = SSL.getInstance().getResources().getAbilityConfig(abilityType);
                    this.addAttribute(abilityType.get()).initAbility(abilityConfig, abilityType, slot);
                });
            });
        });
    }

    public String getSkinName() {
        return this.config.getString("Skin");
    }

    private <T extends Attribute> T addAttribute(T attribute) {
        this.attributes.add(attribute);
        attribute.initAttribute(this);
        return attribute;
    }

    public List<String> getDescription() {
        return this.config.getStringList("Description");
    }

    public String getDisplayName() {
        return MessageUtils.color(this.getColor().getChatSymbol() + this.config.getString("Name"));
    }

    public ColorType getColor() {
        return ColorType.valueOf(this.config.getString("Color"));
    }

    public String getBoldedDisplayName() {
        return MessageUtils.color(this.getColor().getChatSymbol() + "&l" + this.config.getString("Name"));
    }

    public double getRegen() {
        return this.config.getDouble("Regen");
    }

    public double getArmor() {
        return this.config.getDouble("Armor");
    }

    public double getDamage() {
        return this.config.getDouble("Damage");
    }

    public double getKb() {
        return this.config.getDouble("Kb");
    }

    public double getJumpPower() {
        return this.config.getDouble("Jump.Power");
    }

    public double getJumpHeight() {
        return this.config.getDouble("Jump.Height");
    }

    public int getJumpCount() {
        return this.config.getOptionalInt("Jump.Count").orElse(1);
    }

    public Noise getJumpNoise() {
        return YamlReader.noise(this.config.getSection("Jump.Sound"));
    }

    public Noise getHurtNoise() {
        return YamlReader.noise(this.config.getSection("HurtSound"));
    }

    public Noise getDeathNoise() {
        return YamlReader.noise(this.config.getSection("DeathSound"));
    }

    public float getEnergyValue() {
        return this.config.getFloat("Energy");
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(this.attributes);
    }

    public void equip(Player player) {
        this.player = player;
        this.equip();
    }

    public void equip() {
        this.attributes.forEach(Attribute::equip);
    }

    public void activate() {
        this.attributes.forEach(Attribute::activate);
    }

    public void deactivate() {
        this.attributes.forEach(Attribute::deactivate);
    }

    public void destroy() {
        this.attributes.forEach(Attribute::destroy);
    }
}

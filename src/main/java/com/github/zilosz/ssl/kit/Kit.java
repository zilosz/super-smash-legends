package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.AbilityType;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.impl.Energy;
import com.github.zilosz.ssl.attribute.impl.Jump;
import com.github.zilosz.ssl.attribute.impl.Melee;
import com.github.zilosz.ssl.attribute.impl.Regeneration;
import com.github.zilosz.ssl.config.Resources;
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

    skin = Skin.fetchFromAPI(getSkinName());
    jump = addAttribute(new Jump());
    regeneration = addAttribute(new Regeneration());
    melee = addAttribute(new Melee());
    energy = addAttribute(new Energy());

    Optional.ofNullable(config.getSection("Abilities")).ifPresent(section -> {
      IntStream.range(0, 6).forEach(slot -> {
        Optional.ofNullable(section.getString(String.valueOf(slot))).ifPresent(name -> {
          AbilityType abilityType = AbilityType.valueOf(name);
          Resources resources = SSL.getInstance().getResources();
          YamlDocument abilityConfig = resources.getAbilityConfig(abilityType);
          addAttribute(abilityType.get()).initAbility(abilityConfig, abilityType, slot);
        });
      });
    });
  }

  private <T extends Attribute> T addAttribute(T attribute) {
    attributes.add(attribute);
    attribute.assignKit(this);
    return attribute;
  }

  public String getSkinName() {
    return config.getString("Skin");
  }

  public List<String> getDescription() {
    return config.getStringList("Description");
  }

  public String getDisplayName() {
    return MessageUtils.color(getColor().getChatSymbol() + config.getString("Name"));
  }

  public ColorType getColor() {
    return ColorType.valueOf(config.getString("Color"));
  }

  public String getBoldedDisplayName() {
    return MessageUtils.color(getColor().getChatSymbol() + "&l" + config.getString("Name"));
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

  public Noise getHurtNoise() {
    return YamlReader.noise(config.getSection("HurtSound"));
  }

  public Noise getDeathNoise() {
    return YamlReader.noise(config.getSection("DeathSound"));
  }

  public float getEnergyValue() {
    return config.getFloat("Energy");
  }

  public List<Attribute> getAttributes() {
    return Collections.unmodifiableList(attributes);
  }

  public void equip(Player player) {
    this.player = player;
    equip();
  }

  public void equip() {
    attributes.forEach(Attribute::equip);
  }

  public void activate() {
    attributes.forEach(Attribute::activate);
  }

  public void deactivate() {
    attributes.forEach(Attribute::deactivate);
  }

  public void destroy() {
    attributes.forEach(Attribute::destroy);
  }
}

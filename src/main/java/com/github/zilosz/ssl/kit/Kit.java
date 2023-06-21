package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.implementation.Energy;
import com.github.zilosz.ssl.attribute.implementation.Jump;
import com.github.zilosz.ssl.attribute.implementation.Melee;
import com.github.zilosz.ssl.attribute.implementation.Regeneration;
import com.github.zilosz.ssl.utils.Noise;
import com.github.zilosz.ssl.utils.Reflector;
import com.github.zilosz.ssl.utils.Skin;
import com.github.zilosz.ssl.utils.effect.ColorType;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kit {
    private static final SSL plugin = SSL.getInstance();

    private final Section config;

    private final List<Attribute> attributes = new ArrayList<>();
    @Getter private final Jump jump;
    @Getter private final Skin skin;

    @Getter private Player player;

    @SuppressWarnings("unchecked")
    public Kit(Section config) {
        this.config = config;

        this.skin = Skin.fromMojang(this.getSkinName());

        this.jump = new Jump(SSL.getInstance(), this);
        this.attributes.add(this.jump);

        this.attributes.add(new Regeneration(plugin, this));
        this.attributes.add(new Melee(plugin, this));

        if (config.isNumber("Energy")) {
            this.attributes.add(new Energy(plugin, this));
        }

        for (int i = 0; i < 9; i++) {
            int finalI = i;

            config.getOptionalSection("Abilities." + i).ifPresent(abilityConfig -> {
                String configName = abilityConfig.getString("ConfigName");
                String name = Ability.class.getPackageName() + ".implementation." + configName;
                Class<? extends Ability> clazz = (Class<? extends Ability>) Reflector.loadClass(name);
                Ability ability = Reflector.newInstance(clazz, plugin, abilityConfig, this);
                ability.setSlot(finalI);
                this.attributes.add(ability);
            });
        }
    }

    public String getSkinName() {
        return this.config.getString("Skin");
    }

    public String getConfigName() {
        return this.config.getString("ConfigName");
    }

    public List<String> getDescription() {
        return this.config.getStringList("Description");
    }

    public String getDisplayName() {
        return MessageUtils.color(this.getColor().getChatSymbol() + this.config.getString("Name"));
    }

    public ColorType getColor() {
        try {
            return ColorType.valueOf(this.config.getString("Color"));
        } catch (IllegalArgumentException e) {
            return ColorType.WHITE;
        }
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

    public float getEnergy() {
        return this.config.getFloat("Energy");
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(this.attributes);
    }

    public void equip(Player player) {
        this.player = player;
        this.giveItems();
    }

    public void giveItems() {
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

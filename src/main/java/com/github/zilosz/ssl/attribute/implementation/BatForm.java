package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.ClickableAbility;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attack.AttributeDamageEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.event.attribute.JumpEvent;
import com.github.zilosz.ssl.event.attribute.RegenEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.DisguiseUtils;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.Noise;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Set;
import java.util.stream.Collectors;

public class BatForm extends PassiveAbility {
    private int oldJumpLimit;
    private Set<Attribute> removedAttributes;
    private boolean isBat = false;

    public BatForm(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @EventHandler
    public void onAttributeDamage(AttributeDamageEvent event) {
        if (!(event.getAttribute().getPlayer() == this.player)) return;
        if (!(event.getAttribute() instanceof Melee)) return;
        if (!this.isBat) return;
        if (!RegenEvent.attempt(this.player, this.config.getDouble("Regen"))) return;

        Location center = EntityUtils.center(event.getVictim());
        this.player.getWorld().playSound(center, Sound.ZOMBIE_UNFECT, 1, 2);
        new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, center, 3, 0.3, 7);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(DamageEvent event) {
        if (event.getVictim() != this.player) return;

        if (this.isBat) {
            double multiplier = this.config.getDouble("DamageTakenMultiplier");
            event.getDamageSettings().setDamage(event.getDamageSettings().getDamage() * multiplier);
            return;
        }

        if (event.willDie() || event.getNewHealth() > this.config.getDouble("HealthThreshold")) return;

        this.isBat = true;

        this.player.getWorld().playSound(this.player.getLocation(), Sound.BAT_HURT, 1, 0.5f);

        for (int i = 0; i < 3; i++) {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).setSpread(0.5f, 0.5f, 0.5f).show(this.player.getLocation());
        }

        Disguise disguise = DisguiseUtils.applyDisguiseParams(this.player, new MobDisguise(DisguiseType.BAT));
        DisguiseAPI.disguiseToAll(this.player, disguise);

        this.oldJumpLimit = this.kit.getJump().getMaxCount();
        this.kit.getJump().setMaxCount(this.config.getInt("ExtraJumps"));

        this.removedAttributes = this.kit.getAttributes().stream().filter(ClickableAbility.class::isInstance)
                .collect(Collectors.toSet());

        this.removedAttributes.forEach(Attribute::destroy);

        this.player.getInventory().setItem(0, new ItemBuilder<>(Material.GOLD_SWORD).get());
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }

    @Override
    public String getUseType() {
        return "Passive";
    }

    private void reset() {
        if (!this.isBat) return;

        this.isBat = false;
        this.player.getInventory().remove(Material.GOLD_SWORD);
        this.kit.getJump().setMaxCount(this.oldJumpLimit);
        DisguiseAPI.undisguiseToAll(this.player);
    }

    @EventHandler
    public void onRegen(RegenEvent event) {
        if (event.getPlayer() != this.player) return;
        if (!this.isBat) return;

        event.setRegen(event.getRegen() * this.config.getDouble("RegenMultiplier"));

        if (this.player.getHealth() + event.getRegen() <= this.config.getDouble("HealthThreshold")) return;

        this.reset();

        for (Attribute attribute : this.removedAttributes) {
            attribute.equip();
            attribute.activate();
        }

        this.player.getWorld().playSound(this.player.getLocation(), Sound.BAT_HURT, 1, 2);

        for (int i = 0; i < 3; i++) {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).setSpread(0.5f, 0.5f, 0.5f).show(this.player.getLocation());
        }
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (event.getPlayer() != this.player) return;
        if (!this.isBat) return;

        event.setPower(event.getPower() * this.config.getDouble("JumpPowerMultiplier"));
        event.setHeight(event.getHeight() * this.config.getDouble("JumpHeightMultiplier"));
        event.setNoise(new Noise(Sound.BAT_TAKEOFF, 1, 2));
    }
}
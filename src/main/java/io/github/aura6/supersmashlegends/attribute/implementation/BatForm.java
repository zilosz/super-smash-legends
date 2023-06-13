package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.PassiveAbility;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.event.JumpEvent;
import io.github.aura6.supersmashlegends.event.RegenEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.DisguiseUtils;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.Noise;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Set;
import java.util.stream.Collectors;

public class BatForm extends PassiveAbility {
    private int oldJumpLimit;
    private Set<Attribute> removedAttributes;
    private boolean isBat = false;

    public BatForm(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Passive";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onReceivingDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) return;
        if (event.getEntity() != this.player) return;
        if (this.player.getHealth() - event.getFinalDamage() > this.config.getDouble("HealthThreshold")) return;
        if (this.isBat) return;

        this.isBat = true;

        this.player.getWorld().playSound(this.player.getLocation(), Sound.BAT_HURT, 1, 0.5f);

        for (int i = 0; i < 3; i++) {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).setSpread(0.5f, 0.5f, 0.5f).show(this.player.getLocation());
        }

        Disguise disguise = DisguiseUtils.applyDisguiseParams(this.player, new MobDisguise(DisguiseType.BAT));
        DisguiseAPI.disguiseToAll(this.player, disguise);

        this.oldJumpLimit = this.kit.getJump().getCount();
        this.kit.getJump().setCount(this.config.getInt("ExtraJumps"));

        this.removedAttributes = this.kit.getAttributes().stream()
                .filter(attr -> attr instanceof ClickableAbility)
                .collect(Collectors.toSet());
        this.removedAttributes.forEach(Attribute::destroy);

        this.player.getInventory().setItem(0, new ItemBuilder<>(Material.GOLD_SWORD).get());
    }

    private void reset() {
        if (!this.isBat) return;

        this.isBat = false;
        this.player.getInventory().remove(Material.GOLD_SWORD);
        this.kit.getJump().setCount(this.oldJumpLimit);
        DisguiseAPI.undisguiseToAll(this.player);
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
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

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() == this.player && this.isBat) {
            event.setDamage(event.getDamage() * this.config.getDouble("DamageTakenMultiplier"));
        }
    }

    @EventHandler
    public void onCustomDamage(DamageEvent event) {
        if (event.getVictim() == this.player && this.isBat) {
            event.getDamage().setDamage(event.getDamage().getDamage() * this.config.getDouble("DamageTakenMultiplier"));
        }
    }

    @EventHandler
    public void onAttributeDamage(AttributeDamageEvent event) {
        Attribute attribute = event.getAttribute();

        if (attribute.getPlayer() != this.player) return;
        if (!(attribute instanceof Melee)) return;
        if (!this.isBat) return;
        if (!RegenEvent.attempt(this.player, this.config.getDouble("Regen"))) return;

        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_UNFECT, 1, 2);
        new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, EntityUtils.center(event.getVictim()), 3, 0.3, 7);
    }
}

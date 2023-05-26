package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.PassiveAbility;
import io.github.aura6.supersmashlegends.event.JumpEvent;
import io.github.aura6.supersmashlegends.event.RegenEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.DisguiseUtils;
import io.github.aura6.supersmashlegends.utils.Noise;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class BatForm extends PassiveAbility {
    private Bloodlust bloodlust;
    private int oldJumpLimit;
    private List<Attribute> replaced;
    private boolean active = false;

    public BatForm(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Passive";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onReceivingDamage(EntityDamageEvent event) {
        if (event.getEntity() != player) return;
        if (active) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) return;
        if (player.getHealth() - event.getFinalDamage() > config.getDouble("HealthThreshold")) return;

        active = true;

        player.getWorld().playSound(player.getLocation(), Sound.BAT_HURT, 1, 0.5f);

        for (int i = 0; i < 4; i++) {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).show(player.getLocation());
        }

        DisguiseAPI.disguiseToAll(player, DisguiseUtils.applyDisguiseParams(player, new MobDisguise(DisguiseType.BAT)));

        oldJumpLimit = kit.getJump().getCount();
        kit.getJump().setCount(config.getInt("ExtraJumps"));

        bloodlust.equip();
        bloodlust.activate();
        bloodlust.getHotbarItem().show();

        replaced = new ArrayList<>();

        for (Attribute attribute : kit.getAttributes()) {

            if (attribute instanceof ClickableAbility) {
                attribute.destroy();
                replaced.add(attribute);
            }
        }
    }

    @EventHandler
    public void onRegen(RegenEvent event) {
        if (event.getPlayer() != player) return;
        if (!active) return;
        if (player.getHealth() + event.getRegen() <= config.getDouble("HealthThreshold")) return;

        reset();

        for (Attribute attribute : replaced) {
            attribute.equip();
            attribute.activate();
        }

        for (int i = 0; i < 4; i++) {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(player.getLocation(), 2, 20, 0.5);
        }

        player.getWorld().playSound(player.getLocation(), Sound.BAT_HURT, 1, 2);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (!active || event.getPlayer() != player) return;

        event.setPower(event.getPower() * config.getDouble("JumpPowerMultiplier"));
        event.setHeight(event.getHeight() * config.getDouble("JumpHeightMultiplier"));
        event.setNoise(new Noise(Sound.BAT_TAKEOFF, 1, 2));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (active && event.getEntity() == player) {
            event.setDamage(event.getDamage() * config.getDouble("DamageTakenMultiplier"));
        }
    }

    private void reset() {
        active = false;
        bloodlust.destroy();
        kit.getJump().setCount(oldJumpLimit);
        DisguiseAPI.undisguiseToAll(player);
    }

    @Override
    public void activate() {
        super.activate();

        for (Attribute attribute : kit.getAttributes()) {

            if (attribute instanceof Bloodlust) {
                bloodlust = (Bloodlust) attribute;
                Bukkit.getScheduler().runTaskLater(plugin, bloodlust::deactivate, 20);
            }
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (active) {
            reset();
        }
    }
}

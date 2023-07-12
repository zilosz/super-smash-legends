package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.utils.NmsUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.file.YamlReader;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class SuperhumanPunch extends RightClickAbility {
    private boolean hit = false;
    @Nullable private BukkitTask particleTask;
    private LivingEntity victim;

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (!this.hit) {
            this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_THROW, 2, 1);
        }

        this.hit = false;

        for (Player other : Bukkit.getOnlinePlayers()) {
            NmsUtils.sendPacket(other, new PacketPlayOutAnimation(NmsUtils.getPlayer(this.player), 0));
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.hit = false;

        if (this.particleTask != null) {
            this.particleTask.cancel();
            this.particleTask = null;
        }
    }

    @EventHandler
    public void onUseEntity(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer() != this.player) return;
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        if (this.player.getInventory().getHeldItemSlot() != this.hotbarItem.getSlot()) return;
        if (this.cooldownLeft > 0) return;

        this.hit = true;
        this.victim = (LivingEntity) event.getRightClicked();

        Vector direction = this.player.getEyeLocation().getDirection();
        Attack settings = YamlReader.attack(this.config, direction);
        AttackInfo attackInfo = new AttackInfo(AttackType.MELEE, this);
        SSL.getInstance().getDamageManager().attack(this.victim, settings, attackInfo);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPIDER_DEATH, 2, 2);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_WOODBREAK, 0.5f, 2);

        if (this.particleTask != null) {
            this.particleTask.cancel();
        }

        this.particleTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (this.victim.isOnGround()) {
                this.particleTask.cancel();
                this.particleTask = null;

            } else {
                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
                new ParticleMaker(particle).show(this.victim.getLocation());
            }
        }, 0, 0);
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if (event.getPlayer() == this.victim && this.particleTask != null) {
            this.particleTask.cancel();
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getVictim() == this.victim && this.particleTask != null) {
            this.particleTask.cancel();
        }
    }
}

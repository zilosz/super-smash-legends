package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.event.attack.AttributeKbEvent;
import com.github.zilosz.ssl.event.attribute.DoubleJumpEvent;
import com.github.zilosz.ssl.utils.effects.Effects;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class AgileCombat extends RightClickAbility {
    private State state = State.INACTIVE;
    private BukkitTask dropTask;
    private BukkitTask leapTask;
    private BukkitTask cancelTask;
    private boolean canLeap = true;

    @Override
    public void onClick(PlayerInteractEvent event) {

        switch (this.state) {

            case INACTIVE:
                this.sendUseMessage();
                this.onFirstClick();
                break;

            case SPRINTING:
                if (this.canLeap) {
                    this.onLeap();
                }
        }
    }

    private void onFirstClick() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 1, 2);
        Effects.launchFirework(EntityUtils.top(this.player), this.kit.getColor().getColor(), 1);

        this.player.setVelocity(new Vector(0, -this.config.getDouble("DropVelocity"), 0));
        this.player.setWalkSpeed(this.config.getFloat("WalkSpeed"));

        this.state = State.DROPPING;

        this.dropTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (EntityUtils.isPlayerGrounded(this.player)) {
                this.dropTask.cancel();
                this.state = State.SPRINTING;
            }
        }, 0, 0);

        int duration = this.config.getInt("Duration");
        this.cancelTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> this.reset(true), duration);
    }

    private void onLeap() {
        this.state = State.LEAPING;

        double velocity = this.config.getDouble("Leap.Velocity");
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_IDLE, 1, 2);

        this.leapTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (EntityUtils.isPlayerGrounded(this.player)) {
                this.endLeap(false);
                return;
            }

            EntitySelector selector = new HitBoxSelector(this.config.getDouble("Leap.HitBox"));

            new EntityFinder(selector).findClosest(this.player).ifPresent(target -> {
                Vector direction = this.player.getEyeLocation().getDirection();
                Attack attack = YamlReader.attack(this.config.getSection("Leap"), direction.clone().multiply(-1));

                if (SSL.getInstance().getDamageManager().attack(target, this, attack)) {
                    this.endLeap(true);

                    double springVel = this.config.getDouble("SpringVelocity");
                    double springY = this.config.getDouble("SpringVelocityY");
                    this.player.setVelocity(direction.multiply(springVel).setY(springY));

                    this.player.getWorld().playSound(this.player.getLocation(), Sound.HORSE_GALLOP, 3, 1);
                    this.player.getWorld().playSound(this.player.getLocation(), Sound.EXPLODE, 1, 2);
                }
            });
        }, 4, 0);
    }

    private void reset(boolean natural) {
        if (this.state == State.INACTIVE) return;

        this.state = State.INACTIVE;
        this.canLeap = true;
        this.player.setWalkSpeed(0.2f);

        if (this.cancelTask != null) {
            this.cancelTask.cancel();
        }

        if (this.dropTask != null) {
            this.dropTask.cancel();
        }

        if (this.leapTask != null) {
            this.leapTask.cancel();
        }

        if (natural) {
            this.startCooldown();
            this.player.playSound(this.player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 2);
        }
    }

    private void endLeap(boolean canLeap) {
        this.canLeap = canLeap;
        this.state = State.SPRINTING;
        this.leapTask.cancel();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset(false);
    }

    @EventHandler
    public void onKb(AttributeKbEvent event) {
        if (event.getVictim() == this.player && this.state == State.DROPPING) {
            this.reset(true);
        }
    }

    @EventHandler
    public void onJump(DoubleJumpEvent event) {
        if (event.getPlayer() == this.player && this.state.boostsJump) {
            Section jumpConfig = this.config.getSection("Jump");
            event.setPower(jumpConfig.getDouble("Power"));
            event.setHeight(jumpConfig.getDouble("Height"));
            event.setNoise(YamlReader.noise(jumpConfig.getSection("Sound")));
        }
    }

    private enum State {
        INACTIVE(false),
        DROPPING(false),
        LEAPING(true),
        SPRINTING(true);

        private final boolean boostsJump;

        State(boolean boostsJump) {
            this.boostsJump = boostsJump;
        }
    }
}

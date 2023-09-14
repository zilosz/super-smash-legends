package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.DoubleJumpEvent;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class Jump extends Attribute {
    @Getter @Setter private int maxCount;
    private int countLeft;
    @Nullable private BukkitTask hitGroundTask;

    @Override
    public void activate() {
        super.activate();

        this.player.setFlying(false);
        this.player.setAllowFlight(true);

        this.maxCount = this.kit.getJumpCount();
        this.countLeft = this.kit.getJumpCount();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.player.setFlying(false);
        this.player.setAllowFlight(false);
    }

    public void giveExtraJumps(int count) {
        if (this.countLeft + count <= this.maxCount) {
            this.countLeft += count;
            this.player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (event.getPlayer() != this.player) return;

        event.setCancelled(true);

        DoubleJumpEvent jumpEvent = new DoubleJumpEvent(
                this.player,
                this.kit.getJumpPower(),
                this.kit.getJumpHeight(),
                this.kit.getJumpNoise()
        );

        Bukkit.getPluginManager().callEvent(jumpEvent);

        if (jumpEvent.isCancelled()) return;

        Vector direction = this.player.getLocation().getDirection();
        Vector velocity = direction.multiply(jumpEvent.getPower()).setY(jumpEvent.getHeight());

        if (((Entity) this.player).isOnGround()) {
            double boost = SSL.getInstance().getResources().getConfig().getDouble("JumpGroundBooster");
            velocity.add(new Vector(0, boost, 0));
        }

        this.player.setVelocity(velocity);

        jumpEvent.getNoise().playForAll(this.player.getLocation());

        if (--this.countLeft == 0) {
            this.player.setAllowFlight(false);
        }

        if (this.hitGroundTask == null) {
            this.hitGroundTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
                if (EntityUtils.isPlayerGrounded(this.player)) {
                    this.replenish();
                }
            }, 0, 0);
        }
    }

    public void replenish() {
        this.countLeft = this.maxCount;
        this.player.setAllowFlight(true);

        if (this.hitGroundTask != null) {
            this.hitGroundTask.cancel();
            this.hitGroundTask = null;
        }
    }
}

package io.github.aura6.supersmashlegends.attribute.implementation;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.attribute.JumpEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class Jump extends Attribute {
    @Getter @Setter private int maxCount;
    private int countLeft;
    private BukkitTask hitGroundTask;

    public Jump(SuperSmashLegends plugin, Kit kit) {
        super(plugin, kit);
    }

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

    public void replenish() {
        this.countLeft = this.maxCount;
        this.player.setAllowFlight(true);

        if (this.hitGroundTask != null) {
            this.hitGroundTask.cancel();
            this.hitGroundTask = null;
        }
    }
    
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (event.getPlayer() != this.player) return;

        event.setCancelled(true);

        JumpEvent jumpEvent = new JumpEvent(this.player, this.kit.getJumpPower(), this.kit.getJumpHeight(), this.kit.getJumpNoise());
        Bukkit.getPluginManager().callEvent(jumpEvent);

        if (jumpEvent.isCancelled()) return;

        Vector direction = this.player.getLocation().getDirection();
        Vector velocity = direction.multiply(jumpEvent.getPower()).setY(jumpEvent.getHeight());

        if (((Entity) this.player).isOnGround()) {
            double boost = this.plugin.getResources().getConfig().getDouble("JumpGroundBooster");
            velocity.add(new Vector(0, boost, 0));
        }

        this.player.setVelocity(velocity);

        jumpEvent.getNoise().playForAll(this.player.getLocation());
        
        if (--this.countLeft == 0) {
            this.player.setAllowFlight(false);
        }

        if (this.hitGroundTask == null) {
            this.hitGroundTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
                if (EntityUtils.isPlayerGrounded(this.player)) {
                    this.replenish();
                }
            }, 0, 0);
        }
    }
}

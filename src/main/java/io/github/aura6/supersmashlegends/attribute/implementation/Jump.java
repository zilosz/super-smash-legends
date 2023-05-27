package io.github.aura6.supersmashlegends.attribute.implementation;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.JumpEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Jump extends Attribute {
    @Getter @Setter private int count;
    private int amountLeft;
    private BukkitTask hitGroundTask;

    public Jump(SuperSmashLegends plugin, Kit kit) {
        super(plugin, kit);
    }

    @Override
    public void activate() {
        super.activate();

        player.setFlying(false);
        player.setAllowFlight(true);

        count = kit.getJumpCount();
        amountLeft = kit.getJumpCount();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        player.setFlying(false);
        player.setAllowFlight(false);
    }

    public void giveExtraJumps(int count) {
        if (amountLeft + count <= this.count) {
            amountLeft += count;
            player.setAllowFlight(true);
        }
    }
    
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (event.getPlayer() != player) return;

        event.setCancelled(true);

        JumpEvent jumpEvent = new JumpEvent(player, kit.getJumpPower(), kit.getJumpHeight(), kit.getJumpNoise());
        Bukkit.getPluginManager().callEvent(jumpEvent);

        if (jumpEvent.isCancelled()) return;

        player.setVelocity(player.getLocation().getDirection().multiply(jumpEvent.getPower()).setY(jumpEvent.getHeight()));
        jumpEvent.getNoise().playForAll(player.getLocation());
        
        if (--amountLeft == 0) {
            player.setAllowFlight(false);
        }

        if (hitGroundTask == null) {
            hitGroundTask = new BukkitRunnable() {

                @Override
                public void run() {
                    if (EntityUtils.isPlayerGrounded(player)) {
                        amountLeft = count;
                        player.setAllowFlight(true);
                        hitGroundTask = null;
                        cancel();
                    }
                }

            }.runTaskTimer(plugin, 0, 0);
        }
    }
}

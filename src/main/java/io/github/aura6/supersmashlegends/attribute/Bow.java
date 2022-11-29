package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

public abstract class Bow extends PassiveAbility {
    protected int ticksCharging = 0;
    private int bowSlot;

    public Bow(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Bow";
    }

    public void onStart() {}

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != player) return;
        if (!event.hasItem() || event.getItem().getType() != Material.BOW) return;
        if (!event.getPlayer().getInventory().contains(Material.ARROW)) return;
        if (!event.getAction().name().contains("RIGHT")) return;

        ticksCharging = 1;
        bowSlot = event.getPlayer().getInventory().getHeldItemSlot();
        onStart();
    }

    public void onChargeTick() {}

    @Override
    public void run() {
        super.run();

        if (ticksCharging > 0) {
            onChargeTick();
            ticksCharging++;
        }
    }

    public void onFinish() {}

    public void finish() {
        ticksCharging = 0;
        onFinish();
    }

    public void onShot(double force) {}

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (event.getEntity() != player) return;

        event.getProjectile().remove();
        onShot(event.getForce());
        finish();
    }

    @EventHandler
    public void onSwitchItemSlot(PlayerItemHeldEvent event) {
        if (ticksCharging > 0 && event.getPreviousSlot() == bowSlot) {
            finish();
        }
    }
}

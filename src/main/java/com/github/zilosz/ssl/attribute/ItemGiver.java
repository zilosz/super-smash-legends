package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ItemGiver extends PassiveAbility {
    private BukkitTask giveTask;

    @Override
    public void activate() {
        super.activate();
        int ticksPerItem = this.config.getInt("TicksPerItem");

        this.giveTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            ItemStack stack = this.player.getInventory().getItem(this.slot);
            int atOnce = this.config.getInt("AmountAtOnce");

            if (stack == null || stack.getAmount() + atOnce <= this.config.getInt("MaxAmount")) {
                this.player.getInventory().addItem(this.hotbarItem.getItemStack());
                this.player.playSound(this.player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
            }
        }, ticksPerItem, ticksPerItem);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.player.getInventory().remove(this.hotbarItem.getItemStack());

        if (this.giveTask != null) {
            this.giveTask.cancel();
        }
    }

    @Override
    public String getUseType() {
        return "&oPassive";
    }
}

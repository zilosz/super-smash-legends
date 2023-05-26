package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public abstract class ItemGiver extends PassiveAbility {
    private BukkitTask giveTask;

    public ItemGiver(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Passive";
    }

    @Override
    public void activate() {
        super.activate();

        giveTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack == null || stack.getAmount() + config.getInt("AmountAtOnce") <= config.getInt("MaxAmount")) {
                player.getInventory().addItem(hotbarItem.getItemStack());
                player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
            }
        }, config.getInt("TicksPerItem"), config.getInt("TicksPerItem"));
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.player.getInventory().remove(this.hotbarItem.getItemStack());

        if (this.giveTask != null) {
            this.giveTask.cancel();
        }
    }
}

package io.github.aura6.supersmashlegends.power.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Droid extends RightClickAbility {
    private int droidsUsed = 0;

    public Droid(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getDisplayName() {
        return config.getString("Name");
    }

    @Override
    public ItemStack buildItem() {
        ItemStack stack = super.buildItem();
        stack.setAmount(config.getInt("Uses"));
        return stack;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getEyeLocation(), Sound.FIREWORK_LAUNCH, 4, 1);

        droidsUsed++;
        player.getInventory().getItem(slot).setAmount(config.getInt("Uses") - droidsUsed);

        if (droidsUsed == config.getInt("Uses")) {
            destroy();
            kit.removeAttribute(this);
        }
    }
}

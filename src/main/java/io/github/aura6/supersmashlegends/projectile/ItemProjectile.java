package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ItemProjectile extends EmulatedProjectile<Item> {

    public ItemProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
        super(plugin, ability, config);
    }

    public ItemStack getStack() {
        return YamlReader.stack(config.getSection("Item"));
    }

    @Override
    public Item createEntity(Location location) {
        Item item = location.getWorld().dropItem(location, getStack());
        item.setPickupDelay(Integer.MAX_VALUE);
        return item;
    }
}

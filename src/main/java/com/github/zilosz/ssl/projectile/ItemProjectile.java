package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ItemProjectile extends EmulatedProjectile<Item> {

    public ItemProjectile(SSL plugin, Ability ability, Section config) {
        super(plugin, ability, config);
    }

    @Override
    public Item createEntity(Location location) {
        Item item = location.getWorld().dropItem(location, this.getStack());
        item.setPickupDelay(Integer.MAX_VALUE);
        return item;
    }

    public ItemStack getStack() {
        return YamlReader.stack(this.config.getSection("Item"));
    }
}

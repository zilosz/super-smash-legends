package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ItemProjectile extends EmulatedProjectile<Item> {
    @Getter @Setter private ItemStack itemStack;

    public ItemProjectile(Ability ability, Section config) {
        super(ability, config);
        this.config.getOptionalSection("Item").ifPresent(section -> this.itemStack = YamlReader.getStack(section));
    }

    @Override
    public Item createEntity(Location location) {
        Item item = location.getWorld().dropItem(location, this.itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        return item;
    }
}

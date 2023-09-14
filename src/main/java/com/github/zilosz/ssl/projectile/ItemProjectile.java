package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.util.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class ItemProjectile extends EmulatedProjectile<Item> {
    @Getter @Setter private ItemStack itemStack;

    public ItemProjectile(Section config, AttackInfo attackInfo) {
        super(config, attackInfo);
        this.config.getOptionalSection("Item").ifPresent(section -> this.itemStack = YamlReader.stack(section));
    }

    @Override
    public Item createEntity(Location location) {
        Item item = location.getWorld().dropItem(location, this.itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        return item;
    }
}

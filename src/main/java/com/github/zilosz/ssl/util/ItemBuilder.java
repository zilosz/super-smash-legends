package com.github.zilosz.ssl.util;

import com.github.zilosz.ssl.util.message.MessageUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemBuilder<T extends ItemMeta> implements Supplier<ItemStack> {
    private final Material material;
    private boolean isEnchanted = false;
    private int count;
    private byte data;
    private Consumer<T> meta = (meta) -> {};

    public ItemBuilder(Material material) {
        this.material = material;
        this.count = 1;
        this.data = 0;
    }

    public ItemBuilder(ItemStack itemStack) {
        this.material = itemStack.getType();
        this.count = itemStack.getAmount();
        this.data = itemStack.getData().getData();

        Optional.ofNullable(itemStack.getItemMeta().getDisplayName()).ifPresent(this::setName);
        Optional.ofNullable(itemStack.getItemMeta().getLore()).ifPresent(this::setLore);
    }

    public ItemBuilder<T> setName(String name) {
        return this.applyMeta(meta -> meta.setDisplayName(MessageUtils.color(name)));
    }

    public ItemBuilder<T> setLore(List<String> lore) {
        return this.applyMeta(meta -> meta.setLore(MessageUtils.color(lore)));
    }

    public ItemBuilder<T> applyMeta(Consumer<T> meta) {
        this.meta = this.meta.andThen(meta);
        return this;
    }

    public ItemBuilder<T> setCount(int amount) {
        this.count = amount;
        return this;
    }

    public ItemBuilder<T> setData(byte data) {
        this.data = data;
        return this;
    }

    public ItemBuilder<T> setEnchanted(boolean isEnchanted) {
        this.isEnchanted = isEnchanted;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStack get() {
        ItemStack itemStack = new ItemStack(this.material, this.count, this.data);

        if (this.isEnchanted) {
            itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        }

        T meta = (T) itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.spigot().setUnbreakable(true);
        this.meta.accept(meta);
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}

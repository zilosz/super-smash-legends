package com.github.zilosz.ssl.utils;

import com.github.zilosz.ssl.utils.message.MessageUtils;
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
    private int amount;
    private byte data;
    private Consumer<T> meta = ($) -> {};

    public ItemBuilder(Material material) {
        this.material = material;
        this.amount = 1;
        this.data = 0;
    }

    public ItemBuilder(ItemStack itemStack) {
        this.material = itemStack.getType();
        this.amount = itemStack.getAmount();
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

    public ItemBuilder<T> setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder<T> setData(int data) {
        this.data = (byte) data;
        return this;
    }

    public ItemBuilder<T> addEnchantment(Enchantment enchantment) {
        return this.applyMeta(meta -> meta.addEnchant(enchantment, 0, false));
    }

    public ItemBuilder<T> setEnchanted(boolean isEnchanted) {
        this.isEnchanted = isEnchanted;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStack get() {
        ItemStack itemStack = new ItemStack(this.material, this.amount, this.data);
        if (this.isEnchanted) itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.spigot().setUnbreakable(true);
        this.meta.accept((T) meta);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}

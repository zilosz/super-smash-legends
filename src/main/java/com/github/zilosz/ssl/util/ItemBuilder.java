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
  private boolean isEnchanted;
  private int count = 1;
  private byte data;
  private Consumer<T> meta = (meta) -> {};

  public ItemBuilder(Material material) {
    this.material = material;
  }

  public ItemBuilder(ItemStack itemStack) {
    material = itemStack.getType();
    count = itemStack.getAmount();
    data = itemStack.getData().getData();

    Optional.ofNullable(itemStack.getItemMeta().getDisplayName()).ifPresent(this::setName);
    Optional.ofNullable(itemStack.getItemMeta().getLore()).ifPresent(this::setLore);
  }

  public ItemBuilder<T> setName(String name) {
    return applyMeta(meta -> meta.setDisplayName(MessageUtils.color(name)));
  }

  public ItemBuilder<T> applyMeta(Consumer<T> meta) {
    this.meta = this.meta.andThen(meta);
    return this;
  }

  public ItemBuilder<T> setLore(List<String> lore) {
    return applyMeta(meta -> meta.setLore(MessageUtils.color(lore)));
  }

  public ItemBuilder<T> setCount(int amount) {
    count = amount;
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
    ItemStack stack = new ItemStack(material, count, data);

    if (isEnchanted) {
      stack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
    }

    T stackMeta = (T) stack.getItemMeta();
    stackMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS,
        ItemFlag.HIDE_ATTRIBUTES,
        ItemFlag.HIDE_UNBREAKABLE
    );
    stackMeta.spigot().setUnbreakable(true);
    meta.accept(stackMeta);
    stack.setItemMeta(stackMeta);

    return stack;
  }
}

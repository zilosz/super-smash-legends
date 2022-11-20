package io.github.aura6.supersmashlegends.utils;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public abstract class HorizontalInventory<T> implements InventoryProvider {
    protected final SuperSmashLegends plugin;

    public HorizontalInventory(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    public SmartInventory build() {
        return SmartInventory.builder()
                .provider(this)
                .title(MessageUtils.color(getTitle()))
                .size(3, 9)
                .manager(plugin.getInventoryManager())
                .type(InventoryType.CHEST)
                .build();
    }

    public abstract String getTitle();

    public abstract List<T> getElements();

    public abstract ItemStack getItemStack(T element, Player player);

    public abstract void onItemClick(T element, Player player, InventoryClickEvent event);

    @Override
    public void init(Player player, InventoryContents contents) {
        List<T> elements = getElements();
        ClickableItem[] items = new ClickableItem[elements.size()];

        IntStream.range(0, elements.size()).forEach(slot -> {
            T el = elements.get(slot);
            items[slot] = ClickableItem.of(getItemStack(el, player), event -> onItemClick(el, player, event));
        });

        Pagination pagination = contents.pagination();
        pagination.setItems(items);
        pagination.setItemsPerPage(7);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1));

        if (items.length > 7) {

            ItemStack previous = new ItemBuilder<>(Material.ARROW)
                    .setName("&3Previous")
                    .setLore(Arrays.asList("&7Click to go to the previous page.", ""))
                    .get();

            contents.set(2, 3, ClickableItem.of(previous, e -> {
                player.playSound(player.getLocation(), Sound.ARROW_HIT, 1, 2);
                build().open(player, pagination.previous().getPage());
            }));

            ItemStack next = new ItemBuilder<>(Material.ARROW)
                    .setName("&3Next")
                    .setLore(Arrays.asList("&7Click to go to the next page.", ""))
                    .get();

            contents.set(2, 5, ClickableItem.of(next, e -> {
                player.playSound(player.getLocation(), Sound.ARROW_HIT, 1, 2);
                build().open(player, pagination.next().getPage());
            }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}

package com.github.zilosz.ssl.utils.inventory;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public abstract class CustomInventory<T> implements InventoryProvider {
    private static final int MAX_ROWS = 5;
    private static final int MAX_COLUMNS = 7;

    @Override
    public void init(Player player, InventoryContents contents) {
        List<T> items = this.getItems();

        int index = 0;
        int r;
        int c = 1;
        boolean ended = false;

        for (r = 1; r <= MAX_ROWS; r++) {

            for (c = 1; c <= MAX_COLUMNS; c++) {

                if (index >= items.size()) {
                    ended = true;
                    break;
                }

                T item = items.get(index);
                ItemStack itemStack = this.getItemStack(player, item);
                Consumer<InventoryClickEvent> action = e -> this.onItemClick(player, item, e);
                contents.set(r, c, ClickableItem.of(itemStack, action));

                index++;
            }

            if (ended) {
                break;
            }
        }

        ItemStack borderStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 7);
        ClickableItem clickableItem = ClickableItem.empty(borderStack);
        contents.fillBorders(clickableItem);

        while (c <= this.getColumnCount() - 1) {
            contents.set(r, c, clickableItem);
            c++;
        }
    }

    public abstract List<T> getItems();

    public abstract ItemStack getItemStack(Player player, T item);

    public abstract void onItemClick(Player player, T item, InventoryClickEvent event);

    public int getColumnCount() {
        return MAX_COLUMNS + 2;
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        if (!(this instanceof HasRandomOption)) return;

        HasRandomOption randomOption = (HasRandomOption) this;

        int state = contents.property("state", 0);
        contents.setProperty("state", state + 1);

        if (state % randomOption.getTicksPerColor() != 0) return;

        ItemStack stack = new ItemBuilder<>(Material.WOOL)
                .setData(CollectionUtils.selectRandom(randomOption.getColors()))
                .setName("&cR&6A&eN&aD&bO&dM")
                .setLore(List.of("&7Pick a random option!"))
                .get();

        contents.set(0, this.getColumnCount() - 1, ClickableItem.of(stack, e -> {
            randomOption.getChatType().send(player, randomOption.getMessage());
            this.onItemClick(player, CollectionUtils.selectRandom(this.getItems()), e);
        }));
    }

    public SmartInventory build() {
        return SmartInventory.builder()
                .provider(this)
                .type(InventoryType.CHEST)
                .size(this.getRowCount(), this.getColumnCount())
                .manager(SSL.getInstance().getInventoryManager())
                .title(MessageUtils.color(this.getTitle()))
                .build();
    }

    public int getRowCount() {
        return Math.min(MAX_ROWS, (int) Math.ceil(this.getItems().size() / 7.0)) + 2;
    }

    public abstract String getTitle();
}

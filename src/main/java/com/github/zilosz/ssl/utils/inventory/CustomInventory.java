package com.github.zilosz.ssl.utils.inventory;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.effects.ColorType;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class CustomInventory<T> implements InventoryProvider {
    private static final int MAX_ROWS = 5;
    private static final int MAX_COLUMNS = 7;

    private final Map<InventoryCoordinate, T> itemsByCoordinate = new HashMap<>();

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

    public int getColumnCount() {
        return MAX_COLUMNS + 2;
    }

    public abstract String getTitle();

    public abstract List<T> getItems();    @Override
    public void init(Player clicker, InventoryContents contents) {
        List<T> items = this.getItems();

        int index = 0;
        boolean ended = false;

        for (int r = 1; r <= MAX_ROWS; r++) {

            for (int c = 1; c <= MAX_COLUMNS; c++) {

                if (index >= items.size()) {
                    ended = true;
                    break;
                }

                T item = items.get(index);
                this.setItem(contents, clicker, items.get(index), r, c);
                this.itemsByCoordinate.put(new InventoryCoordinate(r, c), item);

                index++;
            }

            if (ended) {
                break;
            }
        }
    }




    private void setItem(InventoryContents contents, Player clicker, T item, int row, int column) {
        ItemStack itemStack = this.getItemStack(clicker, item);
        Consumer<InventoryClickEvent> action = e -> this.onItemClick(clicker, item, e);
        contents.set(row, column, ClickableItem.of(itemStack, action));
    }


    public abstract ItemStack getItemStack(Player player, T item);

    public abstract void onItemClick(Player player, T item, InventoryClickEvent event);

    @Override
    public void update(Player player, InventoryContents contents) {
        int state = contents.property("state", 0);
        contents.setProperty("state", state + 1);

        if (this.updatesItems()) {

            if (state % 10 == 0) {

                this.itemsByCoordinate.forEach((coordinate, item) -> {
                    this.setItem(contents, player, item, coordinate.getRow(), coordinate.getColumn());
                });
            }

            if (state % 40 == 0) {

                CollectionUtils.removeWhileIteratingOverEntry(this.itemsByCoordinate, (coordinate, item) -> {
                    ClickableItem empty = ClickableItem.empty(new ItemStack(Material.AIR));
                    contents.set(coordinate.getRow(), coordinate.getColumn(), empty);
                });

                this.init(player, contents);
            }
        }

        if (this instanceof HasRandomOption && state % 10 == 0) {
            HasRandomOption randomOption = (HasRandomOption) this;

            ItemStack stack = new ItemBuilder<>(Material.WOOL)
                    .setData(CollectionUtils.selectRandom(ColorType.values()).getDyeColor().getWoolData())
                    .setName("&cR&6A&eN&aD&bO&dM")
                    .setLore(List.of("&7Pick a random option!"))
                    .get();

            contents.set(0, this.getColumnCount() - 1, ClickableItem.of(stack, e -> {
                randomOption.getChatType().send(player, randomOption.getMessage());
                this.onItemClick(player, CollectionUtils.selectRandom(this.getItems()), e);
            }));
        }
    }

    public abstract boolean updatesItems();


}

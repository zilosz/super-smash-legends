package com.github.zilosz.ssl.util.inventory;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.ItemBuilder;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.effects.ColorType;
import com.github.zilosz.ssl.util.message.MessageUtils;
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

    private final Map<T, InventoryCoordinate> coordinatesByItem = new HashMap<>();

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

    public abstract List<T> getItems();

    @Override
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
                this.coordinatesByItem.put(item, new InventoryCoordinate(r, c));
                this.setItem(contents, clicker, item);
                index++;
            }

            if (ended) {
                break;
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        int state = contents.property("state", 0);
        contents.setProperty("state", state + 1);

        if (this instanceof AutoUpdatesHard && state % ((AutoUpdatesHard) this).getHardResetTicks() == 0) {

            CollectionUtils.removeWhileIteratingOverValues(this.coordinatesByItem, (item, coordinate) -> {
                ClickableItem empty = ClickableItem.empty(new ItemStack(Material.AIR));
                contents.set(coordinate.getRow(), coordinate.getColumn(), empty);
            });

            this.init(player, contents);

        } else if (this instanceof AutoUpdatesSoft && state % ((AutoUpdatesSoft) this).getSoftUpdateTicks() == 0) {
            this.coordinatesByItem.keySet().forEach(item -> this.setItem(contents, player, item));
        }

        if (this instanceof HasRandomOption) {
            HasRandomOption randomOption = (HasRandomOption) this;

            if (state % randomOption.getTicksPerColorChange() == 0) {

                ItemStack stack = new ItemBuilder<>(Material.WOOL)
                        .setData(CollectionUtils.selectRandom(ColorType.values()).getDyeColor().getWoolData())
                        .setName("&cR&6A&eN&aD&bO&dM")
                        .setLore(List.of("&7Pick a random option!"))
                        .get();

                contents.set(0, this.getColumnCount() - 1, ClickableItem.of(stack, e -> {
                    randomOption.getChatType().send(player, randomOption.getMessage());
                    this.onItemClick(contents, player, CollectionUtils.selectRandom(this.getItems()), e);
                }));
            }
        }
    }

    protected void setItem(InventoryContents contents, Player clicker, T item) {
        ItemStack itemStack = this.getItemStack(clicker, item);
        Consumer<InventoryClickEvent> action = e -> this.onItemClick(contents, clicker, item, e);
        InventoryCoordinate coordinate = this.coordinatesByItem.get(item);
        contents.set(coordinate.getRow(), coordinate.getColumn(), ClickableItem.of(itemStack, action));
    }

    public abstract ItemStack getItemStack(Player player, T item);

    public abstract void onItemClick(InventoryContents contents, Player player, T item, InventoryClickEvent event);
}

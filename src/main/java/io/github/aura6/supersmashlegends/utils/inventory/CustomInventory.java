package io.github.aura6.supersmashlegends.utils.inventory;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.CollectionUtils;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
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

    public abstract int getBorderColorData();

    public abstract String getTitle();

    public abstract List<T> getItems();

    public abstract ItemStack getItemStack(Player player, T item);

    public abstract void onItemClick(Player player, T item, InventoryClickEvent event);

    @Override
    public void init(Player player, InventoryContents contents) {
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
                ItemStack itemStack = this.getItemStack(player, item);
                Consumer<InventoryClickEvent> action = e -> this.onItemClick(player, item, e);
                contents.set(r, c, ClickableItem.of(itemStack, action));

                index++;
            }

            if (ended) {
                break;
            }
        }

        ItemStack borderStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) this.getBorderColorData());
        contents.fillBorders(ClickableItem.empty(borderStack));
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

    public int getRowCount() {
        return Math.min(MAX_ROWS, (int) Math.ceil(this.getItems().size() / 7.0)) + 2;
    }

    public int getColumnCount() {
        return MAX_COLUMNS + 2;
    }

    public SmartInventory build() {
        return SmartInventory.builder()
                .provider(this)
                .type(InventoryType.CHEST)
                .size(this.getRowCount(), this.getColumnCount())
                .manager(SuperSmashLegends.getInstance().getInventoryManager())
                .title(MessageUtils.color(this.getTitle()))
                .build();
    }
}

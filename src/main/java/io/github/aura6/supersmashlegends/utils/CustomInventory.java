package io.github.aura6.supersmashlegends.utils;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
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

        ItemStack borderStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) this.getBorderColorData());
        ClickableItem borderItem = ClickableItem.empty(borderStack);
        contents.fillBorders(borderItem);

        while (c <= MAX_COLUMNS) {
            contents.set(r, c++, borderItem);
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}

    public SmartInventory build() {
        return SmartInventory.builder()
                .provider(this)
                .type(InventoryType.CHEST)
                .size(Math.min(MAX_ROWS, (int) Math.ceil(this.getItems().size() / 7.0)) + 2, MAX_COLUMNS + 2)
                .manager(SuperSmashLegends.getInstance().getInventoryManager())
                .title(MessageUtils.color(this.getTitle()))
                .build();
    }
}

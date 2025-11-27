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

public abstract class CustomInventory<T> implements InventoryProvider {
  private static final int MAX_ROWS = 5;
  private static final int MAX_COLUMNS = 7;

  private final Map<T, InventoryCoordinate> itemToCoords = new HashMap<>();

  public SmartInventory build() {
    return SmartInventory
        .builder()
        .provider(this)
        .type(InventoryType.CHEST)
        .size(getRowCount(), getColumnCount())
        .manager(SSL.getInstance().getInventoryManager())
        .title(MessageUtils.color(getTitle()))
        .build();
  }

  public int getRowCount() {
    return Math.min(MAX_ROWS, (int) Math.ceil(getItems().size() / 7.0)) + 2;
  }

  public abstract List<T> getItems();

  public int getColumnCount() {
    return MAX_COLUMNS + 2;
  }

  public abstract String getTitle();

  @Override
  public void init(Player clicker, InventoryContents contents) {
    List<T> items = getItems();

    int index = 0;
    boolean ended = false;

    for (int r = 1; r <= MAX_ROWS; r++) {

      for (int c = 1; c <= MAX_COLUMNS; c++) {

        if (index >= items.size()) {
          ended = true;
          break;
        }

        T item = items.get(index);
        itemToCoords.put(item, new InventoryCoordinate(r, c));
        setItem(contents, clicker, item);
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

    if (this instanceof AutoUpdatesHard) {
      int ticks = ((AutoUpdatesHard) this).getHardResetTicks();

      if (state % ticks == 0) {
        CollectionUtils.clearOverValues(itemToCoords, (item, coordinate) -> {
          ClickableItem empty = ClickableItem.empty(new ItemStack(Material.AIR));
          contents.set(coordinate.getRow(), coordinate.getColumn(), empty);
        });

        init(player, contents);
      }
    }
    else if (this instanceof AutoUpdatesSoft) {
      int ticks = ((AutoUpdatesSoft) this).getSoftUpdateTicks();

      if (state % ticks == 0) {
        itemToCoords.keySet().forEach(item -> setItem(contents, player, item));
      }
    }

    if (this instanceof HasRandomOption) {
      HasRandomOption randOption = (HasRandomOption) this;

      if (state % randOption.getTicksPerColorChange() == 0) {

        ItemStack stack = new ItemBuilder<>(Material.WOOL)
            .setData(CollectionUtils.randChoice(ColorType.values()).getDyeColor().getWoolData())
            .setName("&cR&6A&eN&aD&bO&dM")
            .setLore(List.of("&7Pick a random option!"))
            .get();

        contents.set(0, getColumnCount() - 1, ClickableItem.of(stack, e -> {
          randOption.getChatType().send(player, randOption.getMessage());
          onItemClick(contents, player, CollectionUtils.randChoice(getItems()), e);
        }));
      }
    }
  }

  protected void setItem(InventoryContents contents, Player clicker, T item) {
    ItemStack itemStack = getItemStack(clicker, item);
    InventoryCoordinate coordinate = itemToCoords.get(item);

    contents.set(
        coordinate.getRow(),
        coordinate.getColumn(),
        ClickableItem.of(itemStack, e -> onItemClick(contents, clicker, item, e))
    );
  }

  public abstract ItemStack getItemStack(Player player, T item);

  public abstract void onItemClick(
      InventoryContents contents, Player player, T item, InventoryClickEvent event
  );
}

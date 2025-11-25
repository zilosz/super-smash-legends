package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.HotbarItem;
import com.github.zilosz.ssl.util.ItemBuilder;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.message.MessageUtils;
import com.github.zilosz.ssl.util.message.Replacers;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public abstract class Ability extends Attribute {
  protected YamlDocument config;
  @Getter protected AbilityType type;
  protected int slot;
  @Getter protected HotbarItem hotbarItem;

  public void initAbility(YamlDocument config, AbilityType type, int slot) {
    this.config = config;
    this.type = type;
    this.slot = slot;
  }

  @Override
  public void equip() {
    super.equip();

    Replacers replacers = new Replacers().add("DESCRIPTION", getDescription());
    List<String> lore = replacers.replaceLines(Arrays.asList("&3&lDescription", "{DESCRIPTION}"));
    ItemStack baseStack = YamlReader.stack(config.getSection("Item"));
    ItemStack stack =
        new ItemBuilder<>(baseStack).setName(getBoldedDisplayName()).setLore(lore).get();

    hotbarItem = new HotbarItem(player, stack, slot);
    hotbarItem.setAction(e -> sendDescription());
    hotbarItem.registerAndShow(SSL.getInstance());
  }

  @Override
  public void unequip() {
    hotbarItem.destroy();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    hotbarItem.setAction(null);
  }

  private List<String> getDescription() {
    return config.getStringList("Description");
  }

  public String getBoldedDisplayName() {
    return MessageUtils.color(kit.getColor().getChatSymbol() + "&l" + config.getString("Name"));
  }

  public void sendDescription() {
    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);

    Replacers replacers = new Replacers()
        .add("COLOR", kit.getColor().getChatSymbol())
        .add("DISPLAY_NAME", getDisplayName())
        .add("USE_TYPE", getUseType())
        .add("DESCRIPTION", getDescription());

    List<String> lines = Arrays.asList(
        "{COLOR}-------------------------------------",
        "&l{DISPLAY_NAME} &7- &6{USE_TYPE}",
        "{DESCRIPTION}",
        "{COLOR}-------------------------------------"
    );

    for (String line : replacers.replaceLines(lines)) {
      player.sendMessage(line);
    }
  }

  public String getDisplayName() {
    return MessageUtils.color(kit.getColor().getChatSymbol() + config.getString("Name"));
  }

  public abstract String getUseType();
}

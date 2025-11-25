package com.github.zilosz.ssl.attribute;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.github.zilosz.ssl.event.attribute.AbilityUseEvent;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;

import java.text.DecimalFormat;

public abstract class ClickableAbility extends Ability {
  protected int cooldownLeft;

  @Override
  public void activate() {
    super.activate();
    cooldownLeft = 0;
    hotbarItem.setAction(this::onClickAttempt);
  }

  public void onClickAttempt(PlayerInteractEvent event) {
    if (invalidate(event)) return;

    AbilityUseEvent abilityUseEvent = new AbilityUseEvent(this);
    Bukkit.getPluginManager().callEvent(abilityUseEvent);

    if (abilityUseEvent.isCancelled()) return;

    onClick(event);

    if (sendsUseMessageInstantly()) {
      sendUseMessage();
    }

    if (startsCooldownInstantly()) {
      startCooldown();
    }

    player.setExp(player.getExp() - getEnergyCost());
  }

  public boolean invalidate(PlayerInteractEvent event) {
    if (player.getExp() < getEnergyCost() || cooldownLeft > 0) return true;

    if (EntityUtils.isPlayerGrounded(player)) {

      if (mustBeAirborne()) {
        Chat.ABILITY.send(player, "&7You must be airborne to use this ability.");
        return true;
      }
    }
    else if (mustBeGrounded()) {
      Chat.ABILITY.send(player, "&7You must be grounded to use this ability.");
      return true;
    }

    return false;
  }

  public float getEnergyCost() {
    return config.getFloat("EnergyCost");
  }

  public boolean mustBeAirborne() {
    return config.getBoolean("MustBeAirborne");
  }

  public boolean mustBeGrounded() {
    return config.getBoolean("MustBeGrounded");
  }

  public abstract void onClick(PlayerInteractEvent event);

  public boolean sendsUseMessageInstantly() {
    return config.getOptionalBoolean("AutoSendUseMessage").orElse(true);
  }

  public void sendUseMessage() {
    Chat.ABILITY.send(player, String.format("&7You used %s&7.", getDisplayName()));
  }

  public boolean startsCooldownInstantly() {
    return config.getOptionalBoolean("AutoStartCooldown").orElse(true);
  }

  public void startCooldown() {
    cooldownLeft = getCooldown();
  }

  public int getCooldown() {
    return config.getInt("Cooldown");
  }

  @Override
  public void run() {
    onTick();

    if (cooldownLeft > 0 && --cooldownLeft == 0) {
      onCooldownEnd();
      Chat.ABILITY.send(player, String.format("&7You can use %s&7.", getDisplayName()));
    }

    if (slot != player.getInventory().getHeldItemSlot()) return;

    String message;

    if (cooldownLeft == 0) {
      message = String.format("%s &7- &6%s", getBoldedDisplayName(), "&l" + getUseType());
    }
    else {
      String color = kit.getColor().getChatSymbol();
      String emptyColor = color.equals("&7") ? "&8&l" : "&7&l";
      int cooldownSoFar = getCooldown() - cooldownLeft;
      String bar =
          MessageUtils.progressBar("❚", "❚", color, emptyColor, cooldownSoFar, getCooldown(), 20);

      DecimalFormat format = new DecimalFormat("#.#");
      format.setMinimumFractionDigits(1);
      String label = format.format(cooldownLeft / 20.0);

      message = String.format("%s %s &f&l%s", getBoldedDisplayName(), bar, label);
    }

    ActionBarAPI.sendActionBar(player, MessageUtils.color(message));
  }

  protected void onTick() {}

  public void onCooldownEnd() {}
}

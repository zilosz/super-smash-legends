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
        this.cooldownLeft = 0;
        this.hotbarItem.setAction(this::onClickAttempt);
    }

    public void onClickAttempt(PlayerInteractEvent event) {
        if (this.invalidate(event)) return;

        AbilityUseEvent abilityUseEvent = new AbilityUseEvent(this);
        Bukkit.getPluginManager().callEvent(abilityUseEvent);

        if (abilityUseEvent.isCancelled()) return;

        this.onClick(event);

        if (this.sendsUseMessageInstantly()) {
            this.sendUseMessage();
        }

        if (this.startsCooldownInstantly()) {
            this.startCooldown();
        }

        this.player.setExp(this.player.getExp() - this.getEnergyCost());
    }

    public boolean invalidate(PlayerInteractEvent event) {
        if (this.player.getExp() < this.getEnergyCost() || this.cooldownLeft > 0) return true;

        if (EntityUtils.isPlayerGrounded(this.player)) {

            if (this.mustBeAirborne()) {
                Chat.ABILITY.send(this.player, "&7You must be airborne to use this ability.");
                return true;
            }

        } else if (this.mustBeGrounded()) {
            Chat.ABILITY.send(this.player, "&7You must be grounded to use this ability.");
            return true;
        }

        return false;
    }

    public abstract void onClick(PlayerInteractEvent event);

    public boolean sendsUseMessageInstantly() {
        return this.config.getOptionalBoolean("AutoSendUseMessage").orElse(true);
    }

    public void sendUseMessage() {
        Chat.ABILITY.send(this.player, String.format("&7You used %s&7.", this.getDisplayName()));
    }

    public boolean startsCooldownInstantly() {
        return this.config.getOptionalBoolean("AutoStartCooldown").orElse(true);
    }

    public void startCooldown() {
        this.cooldownLeft = this.getCooldown();
    }

    public float getEnergyCost() {
        return this.config.getFloat("EnergyCost");
    }

    public boolean mustBeAirborne() {
        return this.config.getBoolean("MustBeAirborne");
    }

    public boolean mustBeGrounded() {
        return this.config.getBoolean("MustBeGrounded");
    }

    public int getCooldown() {
        return this.config.getInt("Cooldown");
    }

    @Override
    public void run() {
        this.onTick();

        if (this.cooldownLeft > 0 && --this.cooldownLeft == 0) {
            this.onCooldownEnd();
            Chat.ABILITY.send(this.player, String.format("&7You can use %s&7.", this.getDisplayName()));
        }

        if (this.slot != this.player.getInventory().getHeldItemSlot()) return;

        String message;

        if (this.cooldownLeft == 0) {
            message = String.format("%s &7- &6%s", this.getBoldedDisplayName(), "&l" + this.getUseType());

        } else {
            String color = this.kit.getColor().getChatSymbol();
            String emptyColor = color.equals("&7") ? "&8&l" : "&7&l";
            int cooldownSoFar = this.getCooldown() - this.cooldownLeft;
            String bar = MessageUtils.progressBar("❚", "❚", color, emptyColor, cooldownSoFar, this.getCooldown(), 20);

            DecimalFormat format = new DecimalFormat("#.#");
            format.setMinimumFractionDigits(1);
            String label = format.format(this.cooldownLeft / 20.0);

            message = String.format("%s %s &f&l%s", this.getBoldedDisplayName(), bar, label);
        }

        ActionBarAPI.sendActionBar(this.player, MessageUtils.color(message));
    }

    protected void onTick() {}

    public void onCooldownEnd() {}
}

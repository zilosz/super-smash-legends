package com.github.zilosz.ssl.attribute;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.github.zilosz.ssl.event.attribute.AbilityUseEvent;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;

import java.text.DecimalFormat;

public abstract class ClickableAbility extends Ability {
    protected int cooldown;
    protected float energyCost;
    protected boolean autoStartCooldown;
    protected boolean autoSendUseMessage;
    protected int cooldownLeft = 0;

    public ClickableAbility() {
        this.cooldown = this.config.getInt("Cooldown");
        this.energyCost = this.config.getFloat("EnergyCost");
        this.autoStartCooldown = this.config.getOptionalBoolean("AutoStartCooldown").orElse(true);
        this.autoSendUseMessage = this.config.getOptionalBoolean("AutoSendUseMessage").orElse(true);
    }

    @Override
    public void activate() {
        super.activate();
        this.hotbarItem.setAction(this::onClickAttempt);
    }

    public void onClickAttempt(PlayerInteractEvent event) {
        if (this.invalidate(event)) return;

        AbilityUseEvent abilityUseEvent = new AbilityUseEvent(this);
        Bukkit.getPluginManager().callEvent(abilityUseEvent);

        if (abilityUseEvent.isCancelled()) return;

        this.onClick(event);

        if (this.autoSendUseMessage) {
            this.sendUseMessage();
        }

        if (this.autoStartCooldown) {
            this.startCooldown();
        }

        this.player.setExp(this.player.getExp() - this.energyCost);
    }

    public boolean invalidate(PlayerInteractEvent event) {
        return this.player.getExp() < this.energyCost || this.cooldownLeft > 0;
    }

    public abstract void onClick(PlayerInteractEvent event);

    public void sendUseMessage() {
        Chat.ABILITY.send(this.player, String.format("&7You used %s&7.", this.getDisplayName()));
    }

    public void startCooldown() {
        this.cooldownLeft = this.cooldown;
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
            int cooldownSoFar = this.cooldown - this.cooldownLeft;
            String bar = MessageUtils.progressBar("❚", "❚", color, emptyColor, cooldownSoFar, this.cooldown, 20);

            DecimalFormat format = new DecimalFormat("#.#");
            format.setMinimumFractionDigits(1);
            String label = format.format(this.cooldownLeft / 20.0);

            message = String.format("%s %s &f&l%s", this.getBoldedDisplayName(), bar, label);
        }

        ActionBarAPI.sendActionBar(this.player, MessageUtils.color(message));
    }

    protected void onTick() {}

    public void onCooldownEnd() {}

    @Override
    public void deactivate() {
        super.deactivate();
        this.cooldownLeft = 0;
    }
}

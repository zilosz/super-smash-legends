package com.github.zilosz.ssl.attribute;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.github.zilosz.ssl.event.attribute.AbilityUseEvent;
import com.github.zilosz.ssl.utils.message.Chat;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
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

    public ClickableAbility(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);

        cooldown = config.getInt("Cooldown");
        energyCost = config.getFloat("EnergyCost");
        autoStartCooldown = config.getOptionalBoolean("AutoStartCooldown").orElse(true);
        autoSendUseMessage = config.getOptionalBoolean("AutoSendUseMessage").orElse(true);
    }

    public boolean invalidate(PlayerInteractEvent event) {
        return player.getExp() < energyCost || cooldownLeft > 0;
    }

    public abstract void onClick(PlayerInteractEvent event);

    public void onClickAttempt(PlayerInteractEvent event) {
        if (invalidate(event)) return;

        AbilityUseEvent abilityUseEvent = new AbilityUseEvent(this);
        Bukkit.getPluginManager().callEvent(abilityUseEvent);

        if (abilityUseEvent.isCancelled()) return;

        onClick(event);

        if (autoSendUseMessage) {
            sendUseMessage();
        }

        if (autoStartCooldown) {
            startCooldown();
        }

        player.setExp(player.getExp() - energyCost);
    }

    public void sendUseMessage() {
        Chat.ABILITY.send(this.player, String.format("&7You used %s&7.", this.getDisplayName()));
    }

    public void startCooldown() {
        this.cooldownLeft = this.cooldown;
    }

    @Override
    public void activate() {
        super.activate();
        hotbarItem.setAction(this::onClickAttempt);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.cooldownLeft = 0;
    }

    public void onCooldownEnd() {}

    protected void onTick() {}

    @Override
    public void run() {
        this.onTick();

        if (this.cooldownLeft > 0) {

            if (--this.cooldownLeft == 0) {
                this.onCooldownEnd();
                Chat.ABILITY.send(this.player, String.format("&7You can use %s&7.", this.getDisplayName()));
            }
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
}

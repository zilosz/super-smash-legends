package io.github.aura6.supersmashlegends.attribute;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.event.AbilityUseEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;

import java.text.DecimalFormat;

public abstract class ClickableAbility extends Ability {
    protected int cooldown;
    protected float energyCost;
    protected boolean autoStartCooldown;
    protected boolean autoSendUseMessage;
    protected int cooldownLeft = 0;

    public ClickableAbility(SuperSmashLegends plugin, Section config, Kit kit) {
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
        Chat.ABILITY.send(player, String.format("&7You used %s&7.", getDisplayName()));
    }

    public void startCooldown() {
        cooldownLeft = cooldown;
    }

    @Override
    public void activate() {
        super.activate();
        hotbarItem.setAction(this::onClickAttempt);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        cooldownLeft = 0;
    }

    public void onCooldownEnd() {}

    @Override
    public void run() {

        if (cooldownLeft > 0) {
            cooldownLeft--;

            if (cooldownLeft == 0) {
                onCooldownEnd();
                Chat.ABILITY.send(player, String.format("&7You can now use %s&7.", getDisplayName()));
            }
        }

        if (slot != player.getInventory().getHeldItemSlot()) return;

        String message;

        if (cooldownLeft == 0) {
            message = String.format("%s &7- &6%s", getBoldedDisplayName(), "&l" + getUseType());

        } else {
            String bar = MessageUtils.progressBar(cooldown - cooldownLeft, cooldown, 20, "&a&l|", "&7&l|");
            String cd = new DecimalFormat("#.#").format(cooldownLeft / 20.0);
            message = String.format("%s %s &f&l%s", getBoldedDisplayName(), bar, cd);
        }

        ActionBarAPI.sendActionBar(player, MessageUtils.color(message));
    }
}

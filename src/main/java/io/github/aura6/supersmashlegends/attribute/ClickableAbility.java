package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class ClickableAbility extends Ability {
    protected int cooldown = 0;
    protected int energyCost = 0;
    protected boolean autoStartCooldown = true;
    protected boolean autoSendUseMessage = true;

    public ClickableAbility(SuperSmashLegends plugin, Section config, Kit kit, int slot) {
        super(plugin, config, kit, slot);
    }

    public abstract void onUse(PlayerInteractEvent event);

    public void onClick(PlayerInteractEvent event) {

    }

    @Override
    public void activate() {
        super.activate();
        hotbarItem.setAction(this::onClick);
    }
}

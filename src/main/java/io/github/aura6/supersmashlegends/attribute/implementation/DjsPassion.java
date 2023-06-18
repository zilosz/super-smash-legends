package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;

public class DjsPassion extends RightClickAbility {
    @Setter private boolean succeeded = false;

    public DjsPassion(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (this.succeeded) {
            this.startCooldown();

        } else {
            Chat.ABILITY.send(this.player, "&7There is no Boombox in sight...");
            this.player.playSound(this.player.getLocation(), Sound.NOTE_PIANO, 1, 1);
        }

        this.succeeded = false;
    }
}

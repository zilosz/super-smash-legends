package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.message.Chat;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;

public class DjsPassion extends RightClickAbility {
    @Setter private boolean succeeded = false;

    public DjsPassion(SSL plugin, Section config, Kit kit) {
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

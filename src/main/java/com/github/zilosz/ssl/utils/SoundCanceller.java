package com.github.zilosz.ssl.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

public class SoundCanceller extends PacketAdapter {
    private final String soundToCancel;

    public SoundCanceller(Plugin plugin, String soundToCancel) {
        super(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT);
        this.soundToCancel = soundToCancel;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacket().getStrings().read(0).equals(this.soundToCancel)) {
            event.setCancelled(true);
        }
    }
}

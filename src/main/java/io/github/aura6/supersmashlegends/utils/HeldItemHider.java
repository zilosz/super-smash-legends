package io.github.aura6.supersmashlegends.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class HeldItemHider extends PacketAdapter {

    public HeldItemHider(Plugin plugin) {
        super(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacket().getIntegers().read(1) == 0) {
            event.getPacket().getItemModifier().write(0, new ItemStack(Material.AIR));
        }
    }
}

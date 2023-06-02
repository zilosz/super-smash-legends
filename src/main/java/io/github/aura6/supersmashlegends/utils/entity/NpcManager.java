package io.github.aura6.supersmashlegends.utils.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;

public class NpcManager implements Listener {
    private final Set<FakeNpc> fakeNpcs = new HashSet<>();

    public void registerNpc(FakeNpc fakeNpc) {
        this.fakeNpcs.add(fakeNpc);
    }

    public void removeNpc(FakeNpc fakeNpc) {
        this.fakeNpcs.remove(fakeNpc);
    }

    private void tryShow(PlayerEvent event) {
        Player p = event.getPlayer();
        this.fakeNpcs.stream().filter(npc -> npc.getLocation().getWorld() == p.getWorld()).forEach(npc -> npc.showTo(p));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.tryShow(event);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        this.tryShow(event);
    }
}

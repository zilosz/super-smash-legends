package com.github.zilosz.ssl.utils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class NpcStorage {
    private final NPCRegistry registry = CitizensAPI.createNamedNPCRegistry("my-registry", new MemoryNPCDataStore());

    public NPC createPlayer(String name) {
        return this.registry.createNPC(EntityType.PLAYER, name);
    }

    public void deregisterNpc(NPC npc) {
        this.registry.deregister(npc);
    }

    public boolean isNpc(Entity entity) {
        return this.registry.isNPC(entity);
    }
}

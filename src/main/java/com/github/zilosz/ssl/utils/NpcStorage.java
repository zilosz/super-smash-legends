package com.github.zilosz.ssl.utils;

import net.citizensnpcs.api.npc.NPC;

import java.util.HashSet;
import java.util.Set;

public class NpcStorage {
    private final Set<NPC> npcs = new HashSet<>();

    public void addNpc(NPC npc) {
        this.npcs.add(npc);
    }

    public void removeNpc(NPC npc) {
        this.npcs.remove(npc);
    }

    public void destroyNpcs() {
        this.npcs.forEach(NPC::destroy);
    }
}

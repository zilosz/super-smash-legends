package com.github.zilosz.ssl.utils;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.entity.Entity;

public class DisguiseUtils {

    public static Disguise applyDisguiseParams(Entity entity, Disguise disguise) {
        disguise.setKeepDisguiseOnPlayerLogout(false);
        disguise.setKeepDisguiseOnPlayerDeath(false);
        disguise.getWatcher().setCustomName(entity.getName());
        disguise.getWatcher().setCustomNameVisible(true);
        disguise.setHideHeldItemFromSelf(true);
        return disguise.setViewSelfDisguise(false);
    }
}

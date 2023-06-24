package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.utils.effect.Effects;
import com.github.zilosz.ssl.utils.entity.DisguiseUtils;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class IncarnationSlam extends RightClickAbility {
    private BukkitTask task;
    private boolean active = false;

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.active;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.active = true;

        MobDisguise disguise = new MobDisguise(DisguiseType.SLIME);
        ((SlimeWatcher) disguise.getWatcher()).setSize(this.config.getInt("SlimeSize"));
        DisguiseAPI.disguiseToAll(this.player, DisguiseUtils.applyDisguiseParams(this.player, disguise));

        Vector direction = this.player.getEyeLocation().getDirection();
        this.player.setVelocity(direction.multiply(this.config.getDouble("Velocity")));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.SLIME_WALK, 3, 0.75f);

        this.task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (EntityUtils.isPlayerGrounded(this.player)) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_DEATH, 2, 2);
                DisguiseAPI.undisguiseToAll(this.player);
                this.startCooldown();
                this.reset();
                return;
            }

            EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

            new EntityFinder(selector).findAll(this.player).forEach(target -> {
                AttackSettings settings = new AttackSettings(this.config, this.player.getLocation().getDirection());
                SSL.getInstance().getDamageManager().attack(target, this, settings);

                target.getWorld().playSound(target.getLocation(), Sound.SLIME_ATTACK, 2, 2);
                Effects.itemBoom(SSL.getInstance(), target.getLocation(), new ItemStack(Material.SLIME_BALL), 4, 0.3, 5);
            });
        }, 4, 0);
    }

    public void reset() {
        this.active = false;
        this.task.cancel();
        DisguiseAPI.undisguiseToAll(this.player);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.active) {
            this.reset();
        }
    }
}

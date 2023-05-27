package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.DisguiseUtils;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.Effects;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.HitBoxSelector;
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

    public IncarnationSlam(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || active;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        active = true;

        MobDisguise disguise = new MobDisguise(DisguiseType.SLIME);
        ((SlimeWatcher) disguise.getWatcher()).setSize(config.getInt("SlimeSize"));
        DisguiseAPI.disguiseToAll(player, DisguiseUtils.applyDisguiseParams(player, disguise));

        Vector direction = player.getEyeLocation().getDirection();
        player.setVelocity(direction.multiply(config.getDouble("Velocity")).setY(config.getDouble("VelocityY")));

        player.getWorld().playSound(player.getLocation(), Sound.SLIME_WALK, 3, 0.75f);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (EntityUtils.isPlayerGrounded(player)) {
                player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 2, 2);
                DisguiseAPI.undisguiseToAll(player);
                startCooldown();
                reset();
                return;
            }

            new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox"))).findAll(player).forEach(target -> {
                Damage damage = Damage.Builder.fromConfig(config, player.getLocation().getDirection()).build();
                plugin.getDamageManager().attemptAttributeDamage(target, damage, this);

                target.getWorld().playSound(target.getLocation(), Sound.SLIME_ATTACK, 2, 2);
                Effects.itemBoom(plugin, target.getLocation(), new ItemStack(Material.SLIME_BALL), 4, 0.3, 5);
            });
        }, 4, 0);
    }

    public void reset() {
        active = false;
        task.cancel();
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (active) {
            reset();
        }
    }
}

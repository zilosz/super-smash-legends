package com.github.zilosz.ssl.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class ChargedRightClickAbility extends RightClickAbility {
    protected boolean showExpBar;
    protected boolean expIncreases;
    protected int minChargeTicks;
    protected int maxChargeTicks;
    protected boolean endChargeInstantly;
    protected int ticksCharging = 0;
    protected boolean startCooldownAfterCharge;

    public ChargedRightClickAbility(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);

        this.autoStartCooldown = false;

        showExpBar = config.getOptionalBoolean("ShowExp").orElse(true);
        expIncreases = config.getOptionalBoolean("ExpIncreases").orElse(true);
        minChargeTicks = config.getInt("MinChargeTicks");
        maxChargeTicks = config.getOptionalInt("MaxChargeTicks").orElse(Integer.MAX_VALUE);
        endChargeInstantly = config.getOptionalBoolean("EndChargeInstantly").orElse(true);
        this.startCooldownAfterCharge = config.getOptionalBoolean("StartCooldownAfterCharge").orElse(true);
    }

    @Override
    public String getUseType() {
        return "Hold Right Click";
    }

    public void onInitialClick(PlayerInteractEvent event) {}

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || ticksCharging > 0;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        ticksCharging = 1;
        onInitialClick(event);

        if (energyCost == 0 && showExpBar) {
            player.setExp(expIncreases ? 0 : 1);
        }
    }

    public void onChargeTick() {}

    public void onGeneralCharge() {}

    public void onFailedCharge() {}

    public void onSuccessfulCharge() {}

    public void onChargeEnd() {
        onGeneralCharge();

        if (ticksCharging < minChargeTicks) {
            onFailedCharge();

        } else {
            onSuccessfulCharge();
        }

        ticksCharging = 0;

        if (energyCost == 0 && showExpBar) {
            player.setExp(0);
        }

        if (this.startCooldownAfterCharge) {
            this.startCooldown();
        }
    }

    @Override
    public void run() {
        super.run();

        if (ticksCharging == 0) return;

        if (!player.isBlocking() || endChargeInstantly && ticksCharging >= maxChargeTicks) {
            onChargeEnd();
            return;
        }

        if (player.getExp() >= energyCost) {
            onChargeTick();

            if (showExpBar) {
                player.setExp(player.getExp() - energyCost);

                if (energyCost == 0 && maxChargeTicks < Integer.MAX_VALUE) {
                    float percent = expIncreases ? ticksCharging : maxChargeTicks - ticksCharging;
                    player.setExp(percent / maxChargeTicks);
                }
            }
        }

        ticksCharging++;
    }

    @Override
    public void deactivate() {
        if (ticksCharging > 0) {
            onChargeEnd();
        }
        super.deactivate();
    }
}

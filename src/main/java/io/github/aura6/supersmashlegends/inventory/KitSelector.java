package io.github.aura6.supersmashlegends.inventory;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.PassiveAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.kit.KitAccessType;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitSelector extends HorizontalInventory<Kit> {

    public KitSelector(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getTitle() {
        return "&5&lKit Selector";
    }

    @Override
    public List<Kit> getElements() {
        return plugin.getKitManager().getKits();
    }

    @Override
    public ItemStack getItemStack(Kit kit, Player player) {
        List<String> abilityUses = new ArrayList<>();

        for (Attribute attribute : kit.getAttributes()) {

            if (attribute instanceof Ability) {
                Ability ability = (Ability) attribute;
                String useDescription;

                if (ability instanceof ClickableAbility) {
                    ClickableAbility clickable = (ClickableAbility) ability;
                    useDescription = String.format("&6%s &7to use %s&7.", clickable.getUseType(), ability.getDisplayName());

                } else if (ability instanceof PassiveAbility) {
                    PassiveAbility passive = (PassiveAbility) ability;
                    useDescription = String.format("%s&7: %s&7.", ability.getDisplayName(), passive.getUseDescription());

                } else {
                    useDescription = "&cThis ability does not extend PassiveAbility or ClickableAbility.";
                }

                abilityUses.add(useDescription);
            }
        }

        KitAccessType accessType = plugin.getKitManager().getKitAccess(player, kit);

        return new ItemBuilder<>(kit.getItemStack())
                .setEnchanted(accessType == KitAccessType.ALREADY_SELECTED)
                .setName("&l" + kit.getDisplayName())
                .setLore(new Replacers()
                        .add("STATUS", accessType.getLore(kit))
                        .add("REGEN", String.valueOf(kit.getRegen()))
                        .add("ARMOR", String.valueOf(kit.getArmor()))
                        .add("DAMAGE", String.valueOf(kit.getDamage()))
                        .add("KB", String.valueOf(kit.getKb()))
                        .add("JUMP_POWER", String.valueOf(kit.getJumpPower()))
                        .add("JUMP_HEIGHT", String.valueOf(kit.getJumpHeight()))
                        .add("ABILITIES", abilityUses)
                        .add("DESCRIPTION", kit.getDescription())
                        .add("COLOR", kit.getColor())
                        .replaceLines(Arrays.asList(
                                "{STATUS}",
                                "",
                                "&3&lStats",
                                "&7Regen: {COLOR}{REGEN}",
                                "&7Armor: {COLOR}{ARMOR}",
                                "&7Damage: {COLOR}{DAMAGE}",
                                "&7Kb: {COLOR}{KB}",
                                "&7Jump Power: {COLOR}{JUMP_POWER}",
                                "&7Jump Height: {COLOR}{JUMP_HEIGHT}",
                                "",
                                "&3&lAbilities",
                                "{ABILITIES}",
                                "",
                                "&3&lDescription",
                                "{DESCRIPTION}",
                                ""
                        ))
                ).get();
    }

    @Override
    public void onItemClick(Kit kit, Player player, InventoryClickEvent event) {
        if (plugin.getKitManager().handleKitSelection(player, kit) != KitAccessType.ALREADY_SELECTED) {
            player.closeInventory();
        }
    }
}

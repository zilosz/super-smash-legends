package io.github.aura6.supersmashlegends.kit;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.PassiveAbility;
import io.github.aura6.supersmashlegends.utils.CustomInventory;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KitSelector extends CustomInventory<Kit> {

    @Override
    public int getBorderColorData() {
        return 3;
    }

    @Override
    public String getTitle() {
        return "Kit Selector";
    }

    @Override
    public List<Kit> getItems() {
        return SuperSmashLegends.getInstance().getKitManager().getKits().stream()
                .sorted(Comparator.comparing(Kit::getConfigName))
                .collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemStack(Player player, Kit kit) {
        List<String> abilityUses = new ArrayList<>();

        for (Attribute attribute : kit.getAttributes()) {

            if (attribute instanceof Ability) {
                Ability ability = (Ability) attribute;
                String useType = ability.getUseType();
                String displayName = ability.getDisplayName();

                if (ability instanceof ClickableAbility) {
                    abilityUses.add(String.format("&6%s &7to use %s&7", useType, displayName));

                } else if (ability instanceof PassiveAbility) {
                    abilityUses.add(String.format("%s: &6%s&7", displayName, useType));
                }
            }
        }

        KitManager kitManager = SuperSmashLegends.getInstance().getKitManager();
        KitAccessType accessType = kitManager.getKitAccess(player, kit.getConfigName());

        Replacers replacers = new Replacers()
                .add("STATUS", accessType.getLore(kit))
                .add("REGEN", kit.getRegen())
                .add("ARMOR", kit.getArmor())
                .add("DAMAGE", kit.getDamage())
                .add("KB", kit.getKb())
                .add("JUMP_POWER", kit.getJumpPower())
                .add("JUMP_HEIGHT", kit.getJumpHeight())
                .add("JUMP_COUNT", kit.getJumpCount())
                .add("ABILITIES", abilityUses)
                .add("DESCRIPTION", kit.getDescription())
                .add("COLOR", kit.getColor());

        List<String> lore = new ArrayList<>(Arrays.asList(
                "{STATUS}",
                "",
                "&3&lStats",
                "&7Regen: {COLOR}{REGEN}",
                "&7Armor: {COLOR}{ARMOR}",
                "&7Damage: {COLOR}{DAMAGE}",
                "&7Kb: {COLOR}{KB}",
                "&7Jump Power: {COLOR}{JUMP_POWER}",
                "&7Jump Height: {COLOR}{JUMP_HEIGHT}",
                "&7Jump Count: {COLOR}{JUMP_COUNT}",
                "",
                "&3&lAbilities",
                "{ABILITIES}",
                "",
                "&3&lDescription",
                "{DESCRIPTION}",
                ""
        ));

        if (kit.getEnergy() > 0) {
            lore.add(10, "&7Energy: {COLOR}{ENERGY}");
            replacers.add("ENERGY", kit.getEnergy());
        }


        return new ItemBuilder<SkullMeta>(Material.SKULL_ITEM)
                .setData(3)
                .applyMeta(meta -> meta.setOwner(kit.getSkinName()))
                .setEnchanted(accessType == KitAccessType.ALREADY_SELECTED)
                .setName("&l" + kit.getBoldedDisplayName())
                .setLore(replacers.replaceLines(lore))
                .get();
    }

    @Override
    public void onItemClick(Player player, Kit kit, InventoryClickEvent event) {
        KitManager kitManager = SuperSmashLegends.getInstance().getKitManager();

        if (kitManager.handleKitSelection(player, kit) != KitAccessType.ALREADY_SELECTED) {
            player.closeInventory();
        }
    }
}

package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.inventory.CustomInventory;
import com.github.zilosz.ssl.utils.inventory.HasRandomOption;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.Replacers;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitSelector extends CustomInventory<Kit> implements HasRandomOption {

    @Override
    public List<Kit> getItems() {
        return SSL.getInstance().getKitManager().getKits();
    }

    @Override
    public ItemStack getItemStack(Player player, Kit kit) {
        List<String> abilityUses = new ArrayList<>();

        for (Attribute attribute : kit.getAttributes()) {

            if (attribute instanceof Ability) {
                Ability ability = (Ability) attribute;
                String useType = ability.getUseType();
                String displayName = ability.getDisplayName();
                abilityUses.add(String.format("&6%s &7to use %s&7", useType, displayName));
            }
        }

        KitManager kitManager = SSL.getInstance().getKitManager();
        KitAccessType accessType = kitManager.getKitAccess(player, kit.getType());

        Replacers replacers = new Replacers()
                .add("STATUS", accessType.getLore())
                .add("REGEN", kit.getRegen())
                .add("ARMOR", kit.getArmor())
                .add("DAMAGE", kit.getDamage())
                .add("KB", kit.getKb())
                .add("JUMP_POWER", kit.getJumpPower())
                .add("JUMP_HEIGHT", kit.getJumpHeight())
                .add("JUMP_COUNT", kit.getJumpCount())
                .add("ABILITIES", abilityUses)
                .add("DESCRIPTION", kit.getDescription())
                .add("COLOR", kit.getColor().getChatSymbol());

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

        return new ItemBuilder<SkullMeta>(Material.SKULL_ITEM).setData(3)
                .applyMeta(meta -> kit.getSkin().applyToSkull(meta))
                .setEnchanted(accessType == KitAccessType.SELECTED)
                .setName("&l" + kit.getBoldedDisplayName())
                .setLore(replacers.replaceLines(lore))
                .get();
    }

    @Override
    public void onItemClick(Player player, Kit kit, InventoryClickEvent event) {
        SSL.getInstance().getKitManager().setKit(player, kit.getType());
        player.closeInventory();
    }

    @Override
    public String getTitle() {
        return "Kit Selector";
    }

    @Override
    public Chat getChatType() {
        return Chat.KIT;
    }

    @Override
    public String getMessage() {
        return "&7Selecting a random kit...";
    }
}

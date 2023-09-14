package com.github.zilosz.ssl.team;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.ItemBuilder;
import com.github.zilosz.ssl.util.inventory.AutoUpdatesSoft;
import com.github.zilosz.ssl.util.inventory.CustomInventory;
import com.github.zilosz.ssl.util.inventory.HasRandomOption;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.Replacers;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamSelector extends CustomInventory<Team> implements HasRandomOption, AutoUpdatesSoft {

    @Override
    public String getTitle() {
        return "Team Selector";
    }

    @Override
    public List<Team> getItems() {
        return SSL.getInstance().getTeamManager().getTeamList();
    }

    @Override
    public ItemStack getItemStack(Player player, Team team) {
        String players = team.getPlayers().stream().map(Player::getName).collect(Collectors.joining(", "));

        Replacers replacers = new Replacers().add("SIZE", team.getPlayerCount())
                .add("CAP", team.getPlayerCap())
                .add("PLAYERS", team.getPlayerCount() > 0 ? players : "&7&oNone");

        List<String> lore = replacers.replaceLines(Arrays.asList(
                "&3&lCapacity: &5{SIZE}&7/&f{CAP}",
                "&3&lPlayers: &7{PLAYERS}"
        ));

        return new ItemBuilder<>(Material.WOOL)
                .setData(team.getColorType().getDyeColor().getWoolData())
                .setName(team.getName())
                .setLore(lore)
                .get();
    }

    @Override
    public void onItemClick(InventoryContents contents, Player player, Team team, InventoryClickEvent event) {
        TeamManager teamManager = SSL.getInstance().getTeamManager();

        if (team.equals(teamManager.getEntityTeam(player))) {
            teamManager.removeEntityFromTeam(player);
            Chat.TEAM.send(player, String.format("&7You left the %s &7team.", team.getName()));
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
            return;
        }

        if (team.getPlayerCap() == team.getPlayerCount()) {
            Chat.TEAM.send(player, String.format("%s &7is already full!", team.getName()));
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_HIT, 1, 1);
            return;
        }

        teamManager.removeEntityFromTeam(player);
        teamManager.addEntityToTeam(player, team);

        Chat.TEAM.send(player, String.format("&7You joined the %s &7team.", team.getName()));
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
    }

    @Override
    public Chat getChatType() {
        return Chat.TEAM;
    }

    @Override
    public String getMessage() {
        return "&7Selecting a random team...";
    }
}

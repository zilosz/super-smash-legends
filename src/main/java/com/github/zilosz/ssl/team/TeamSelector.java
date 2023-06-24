package com.github.zilosz.ssl.team;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.inventory.CustomInventory;
import com.github.zilosz.ssl.utils.inventory.HasRandomOption;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.Replacers;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TeamSelector extends CustomInventory<Team> implements HasRandomOption {

    @Override
    public List<Team> getItems() {
        List<Team> teams = SSL.getInstance().getTeamManager().getTeamList();
        return teams.stream().sorted(Comparator.comparing(Team::getName)).collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemStack(Player player, Team team) {
        String players = team.getPlayers().stream().map(Player::getName).collect(Collectors.joining(", "));

        Replacers replacers = new Replacers().add("SIZE", team.getSize())
                .add("CAP", SSL.getInstance().getTeamManager().getTeamSize())
                .add("PLAYERS", team.getSize() > 0 ? players : "&7&oNone");

        List<String> lore = replacers.replaceLines(Arrays.asList(
                "&3&lCapacity: &5{SIZE}&7/&f{CAP}",
                "&3&lPlayers: &7{PLAYERS}"
        ));

        return new ItemBuilder<>(team.getItemStack()).setName(team.getName()).setEnchanted(team.hasPlayer(player))
                .setLore(lore).get();
    }

    @Override
    public void onItemClick(Player player, Team team, InventoryClickEvent event) {
        TeamManager teamManager = SSL.getInstance().getTeamManager();

        if (team.getSize() == teamManager.getTeamSize()) {
            Chat.TEAM.send(player, "&7This team is full!");
            return;
        }

        AtomicReference<Team> chosenAtomic = new AtomicReference<>();

        teamManager.findChosenTeam(player).ifPresent(chosen -> {
            chosen.removePlayer(player);
            chosenAtomic.set(chosen);
        });

        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.5f);

        if (chosenAtomic.get() == team) {
            Chat.TEAM.send(player, "&7You are no longer on a team.");

        } else {
            team.addPlayer(player);
            Chat.TEAM.send(player, String.format("&7You are now on the %s &7team.", team.getName()));
        }
    }

    @Override
    public String getTitle() {
        return "Team Selector";
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

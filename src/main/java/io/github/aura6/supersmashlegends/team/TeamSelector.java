package io.github.aura6.supersmashlegends.team;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.CustomInventory;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TeamSelector extends CustomInventory<Team> {

    @Override
    public int getBorderColorData() {
        return 1;
    }

    @Override
    public String getTitle() {
        return "Team Selector";
    }

    @Override
    public List<Team> getItems() {
        return SuperSmashLegends.getInstance().getTeamManager().getTeamList().stream()
                .sorted(Comparator.comparing(Team::getName))
                .collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemStack(Player player, Team team) {
        String players = team.getPlayers().stream().map(Player::getName).collect(Collectors.joining(", "));

        Replacers replacers = new Replacers()
                .add("SIZE", team.getSize())
                .add("CAP", SuperSmashLegends.getInstance().getTeamManager().getTeamSize())
                .add("PLAYERS", team.getSize() > 0 ? players : "&7&oNone");

        List<String> lore = replacers.replaceLines(Arrays.asList(
                "&3&lCapacity: &5{SIZE}&7/&f{CAP}",
                "&3&lPlayers: &7{PLAYERS}"
        ));

        return new ItemBuilder<>(team.getItemStack())
                .setName(team.getName())
                .setEnchanted(team.hasPlayer(player))
                .setLore(lore)
                .get();
    }

    @Override
    public void onItemClick(Player player, Team team, InventoryClickEvent event) {
        TeamManager teamManager = SuperSmashLegends.getInstance().getTeamManager();

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

        if (chosenAtomic.get() == team) {
            Chat.TEAM.send(player, "&7You are no longer on a team.");

        } else {
            team.addPlayer(player);
            Chat.TEAM.send(player, String.format("&7You are now on the %s &7team.", team.getName()));
        }
    }
}

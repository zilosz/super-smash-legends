package io.github.aura6.supersmashlegends.inventory;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.team.Team;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TeamSelector extends HorizontalInventory<Team> {

    public TeamSelector(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getTitle() {
        return "&a&lTeam Selector";
    }

    @Override
    public List<Team> getElements() {
        return plugin.getTeamManager().getTeams();
    }

    @Override
    public ItemStack getItemStack(Team team, Player player) {
        String players;

        if (team.getSize() > 0) {
            players = team.getPlayers().stream().map(Player::getName).collect(Collectors.joining(", "));

        } else {
            players = "&7&oNone.";
        }

        return new ItemBuilder<>(team.getItemStack())
                .setName(team.getName())
                .setEnchanted(team.hasPlayer(player))
                .setLore(new Replacers()
                        .add("SIZE", String.valueOf(team.getSize()))
                        .add("CAP", String.valueOf(plugin.getTeamManager().getTeamSize()))
                        .add("PLAYERS", players)
                        .replaceLines(Arrays.asList(
                                "&3&lCapacity: &5{SIZE}&7/&f{CAP}",
                                "&3&lPlayers: &7{PLAYERS}"
                        ))
                ).get();
    }

    @Override
    public void onItemClick(Team team, Player player, InventoryClickEvent event) {
        AtomicReference<Team> chosenAtomic = new AtomicReference<>();

        plugin.getTeamManager().getChosenTeam(player).ifPresent(chosen -> {
            chosen.removePlayer(player);
            chosenAtomic.set(chosen);
        });

        if (chosenAtomic.get() == team) {
            Chat.TEAM.send(player, "&7You are no longer on a team.");

        } else {
            team.addPlayer(player);
            Chat.TEAM.send(player, String.format("&7You are now on the %s &7team.", team.getName()));
        }

        player.closeInventory();
        build().open(player);
    }
}

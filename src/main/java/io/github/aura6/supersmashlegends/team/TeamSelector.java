package io.github.aura6.supersmashlegends.team;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.HorizontalInventory;
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
        return plugin.getTeamManager().getTeamList();
    }

    @Override
    public ItemStack getItemStack(Team team, Player player) {
        String players = team.getPlayers().stream().map(Player::getName).collect(Collectors.joining(", "));

        Replacers replacers = new Replacers()
                .add("SIZE", team.getSize())
                .add("CAP", this.plugin.getTeamManager().getTeamSize())
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
    public void onItemClick(Team team, Player player, InventoryClickEvent event) {
        TeamManager teamManager = plugin.getTeamManager();

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

package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.ItemBuilder;
import com.github.zilosz.ssl.util.inventory.AutoUpdatesSoft;
import com.github.zilosz.ssl.util.inventory.CustomInventory;
import com.github.zilosz.ssl.util.inventory.HasRandomOption;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.Replacers;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ArenaVoter extends CustomInventory<Arena> implements HasRandomOption, AutoUpdatesSoft {

    @Override
    public String getTitle() {
        return "Arena Voter";
    }

    @Override
    public List<Arena> getItems() {
        return SSL.getInstance().getArenaManager().getArenas();
    }

    @Override
    public ItemStack getItemStack(Player player, Arena arena) {
        Replacers replacers = new Replacers().add("AUTHORS", arena.getAuthors()).add("VOTES", arena.getTotalVotes());

        List<String> lore = replacers.replaceLines(Arrays.asList(
                "&3&lAuthors: &7{AUTHORS}&7",
                "&3&lVotes: &f{VOTES}",
                ""
        ));

        return new ItemBuilder<>(arena.getItemStack())
                .setEnchanted(arena.isVotedBy(player))
                .setName(arena.getName())
                .setLore(lore)
                .get();
    }

    @Override
    public void onItemClick(InventoryContents contents, Player player, Arena arena, InventoryClickEvent event) {
        ArenaManager arenaManager = SSL.getInstance().getArenaManager();
        Arena chosenArena = arenaManager.getChosenArena(player);

        if (arena.equals(chosenArena)) {
            arenaManager.removeVote(player);

            Chat.ARENA.send(player, "&7Your arena vote was wiped.");
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);

        } else {
            arenaManager.removeVote(player);
            arenaManager.addVote(player, arena);

            Chat.ARENA.send(player, String.format("&7You voted for %s&7.", arena.getName()));
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
        }
    }

    @Override
    public Chat getChatType() {
        return Chat.ARENA;
    }

    @Override
    public String getMessage() {
        return "&7Voting for a random arena...";
    }
}

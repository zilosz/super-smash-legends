package com.github.zilosz.ssl.arena;

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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ArenaVoter extends CustomInventory<Arena> implements HasRandomOption {

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
    public void onItemClick(Player player, Arena arena, InventoryClickEvent event) {
        AtomicReference<Arena> chosenAtomic = new AtomicReference<>();

        SSL.getInstance().getArenaManager().getChosenArena(player).ifPresent(chosen -> {
            chosen.wipeVote(player);
            chosenAtomic.set(chosen);
        });

        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.5F);

        if (chosenAtomic.get() == arena) {
            Chat.ARENA.send(player, "&7Your arena vote has been wiped.");

        } else {
            arena.addVote(player);
            Chat.ARENA.send(player, String.format("&7You have voted for the %s &7arena.", arena.getName()));
        }

        player.closeInventory();
        this.build().open(player);
    }

    @Override
    public boolean updatesItems() {
        return false;
    }

    @Override
    public String getTitle() {
        return "Arena Voter";
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

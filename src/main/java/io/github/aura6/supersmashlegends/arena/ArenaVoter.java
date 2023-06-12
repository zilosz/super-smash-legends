package io.github.aura6.supersmashlegends.arena;

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

public class ArenaVoter extends CustomInventory<Arena> {

    @Override
    public int getBorderColorData() {
        return 13;
    }

    @Override
    public String getTitle() {
        return "Arena Voter";
    }

    @Override
    public List<Arena> getItems() {
        return SuperSmashLegends.getInstance().getArenaManager().getArenas().stream()
                .sorted(Comparator.comparing(Arena::getName))
                .collect(Collectors.toList());
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
                .setEnchanted(arena.isVotedFor(player))
                .setName(arena.getName())
                .setLore(lore)
                .get();
    }

    @Override
    public void onItemClick(Player player, Arena arena, InventoryClickEvent event) {
        AtomicReference<Arena> chosenAtomic = new AtomicReference<>();

        SuperSmashLegends.getInstance().getArenaManager().getChosenArena(player).ifPresent(chosen -> {
            chosen.wipeVote(player);
            chosenAtomic.set(chosen);
        });

        if (chosenAtomic.get() == arena) {
            Chat.ARENA.send(player, "&7Your arena vote has been wiped.");

        } else {
            arena.addVote(player);
            Chat.ARENA.send(player, String.format("&7You have voted for the %s &7arena.", arena.getName()));
        }

        player.closeInventory();
        this.build().open(player);
    }
}

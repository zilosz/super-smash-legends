package io.github.aura6.supersmashlegends.inventory;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ArenaVoter extends HorizontalInventory<Arena> {

    public ArenaVoter(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getTitle() {
        return "&2&lArena Voter";
    }

    @Override
    public List<Arena> getElements() {
        return plugin.getArenaManager().getArenas();
    }

    @Override
    public ItemStack getItemStack(Arena arena, Player player) {
        return new ItemBuilder<>(arena.getItemStack())
                .setEnchanted(arena.isVotedFor(player))
                .setName(arena.getName())
                .setLore(new Replacers()
                        .add("AUTHORS", arena.getAuthors())
                        .add("VOTES", String.valueOf(arena.getTotalVotes()))
                        .replaceLines(Arrays.asList(
                                "&3&lAuthors: &7{AUTHORS}&7",
                                "&3&lVotes: &f{VOTES}",
                                ""
                        ))
                ).get();
    }

    @Override
    public void onItemClick(Arena arena, Player player, InventoryClickEvent event) {
        AtomicReference<Arena> chosenAtomic = new AtomicReference<>();

        plugin.getArenaManager().getChosenArena(player).ifPresent(chosen -> {
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
        build().open(player);
    }
}

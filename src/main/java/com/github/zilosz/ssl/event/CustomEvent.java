package com.github.zilosz.ssl.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomEvent extends Event {
    private static final HandlerList Handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return Handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return Handlers;
    }
}

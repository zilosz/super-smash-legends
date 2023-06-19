package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.RegenEvent;
import com.github.zilosz.ssl.kit.Kit;

public class Regeneration extends Attribute {

    public Regeneration(SSL plugin, Kit kit) {
        super(plugin, kit);
        this.period = 20;
    }

    @Override
    public void run() {
        RegenEvent.attempt(this.player, this.kit.getRegen());
    }
}

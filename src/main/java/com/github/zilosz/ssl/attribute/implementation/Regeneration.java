package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.RegenEvent;

public class Regeneration extends Attribute {

    public Regeneration() {
        this.period = 20;
    }

    @Override
    public void run() {
        RegenEvent.attempt(this.player, this.kit.getRegen());
    }
}

package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.RegenEvent;

public class Regeneration extends Attribute {

  public Regeneration() {
    period = 20;
  }

  @Override
  public void run() {
    RegenEvent.attempt(player, kit.getRegen());
  }
}

package com.github.zilosz.ssl.attack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Attack {
  private final Damage damage;
  private final KnockBack kb;
  @Setter private String name;
  @Setter private int immunityTicks;
}

package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AttackSource {
  private final Attribute attribute;
  private final Attack attack;
}

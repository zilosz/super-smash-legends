package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@RequiredArgsConstructor
public class AttackInfo {
  private final AttackType type;
  private final Attribute attribute;
}

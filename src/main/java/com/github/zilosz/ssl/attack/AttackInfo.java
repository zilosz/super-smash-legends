package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
public class AttackInfo {
    private final AttackType type;
    private final Attribute attribute;

    public AttackInfo(AttackType type, Attribute attribute) {
        this.type = type;
        this.attribute = attribute;
    }
}

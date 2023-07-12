package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import lombok.Getter;

@Getter
public class AttackSource {
    private final Attribute attribute;
    private final Attack attack;

    public AttackSource(Attribute attribute, Attack attack) {
        this.attribute = attribute;
        this.attack = attack;
    }
}

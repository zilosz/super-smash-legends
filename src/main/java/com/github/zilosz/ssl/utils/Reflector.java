package com.github.zilosz.ssl.utils;

import java.lang.reflect.InvocationTargetException;

public class Reflector {

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<? extends T> clazz, Object... params) {
        try {
            return (T) clazz.getDeclaredConstructors()[0].newInstance(params);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

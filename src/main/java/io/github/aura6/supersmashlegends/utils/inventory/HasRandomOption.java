package io.github.aura6.supersmashlegends.utils.inventory;

import io.github.aura6.supersmashlegends.utils.message.Chat;

import java.util.Arrays;
import java.util.List;

public interface HasRandomOption {

    Chat getChatType();

    String getMessage();

    default int getTicksPerColor() {
        return 10;
    }

    default List<Integer> getColors() {
        return Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    }
}

package com.github.zilosz.ssl.utils.inventory;

import com.github.zilosz.ssl.utils.message.Chat;

public interface HasRandomOption {

    Chat getChatType();

    String getMessage();

    default int getTicksPerColorChange() {
        return 10;
    }
}

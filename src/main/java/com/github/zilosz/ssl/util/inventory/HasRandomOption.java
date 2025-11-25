package com.github.zilosz.ssl.util.inventory;

import com.github.zilosz.ssl.util.message.Chat;

public interface HasRandomOption {

  Chat getChatType();

  String getMessage();

  default int getTicksPerColorChange() {
    return 10;
  }
}

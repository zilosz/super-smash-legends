package com.github.zilosz.ssl.util.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Replacers {
  private final Map<String, String> singles = new HashMap<>();
  private final Map<String, List<String>> multiples = new HashMap<>();

  public Replacers add(String placeholder, Number replacement) {
    return add(placeholder, String.valueOf(replacement));
  }

  public Replacers add(String placeholder, String replacement) {
    singles.put(wrap(placeholder), replacement);
    return this;
  }

  public static String wrap(String placeholder) {
    return "{" + placeholder + "}";
  }

  public Replacers add(String placeholder, List<String> replacements) {
    multiples.put(wrap(placeholder), replacements);
    return this;
  }

  public List<String> replaceLines(List<String> lines) {
    List<String> newLines = new ArrayList<>();

    for (String line : lines) {
      boolean useOriginal = true;

      for (var multiple : multiples.entrySet()) {

        if (line.contains(multiple.getKey())) {
          newLines.addAll(multiple.getValue());
          useOriginal = false;

          break;
        }
      }

      if (useOriginal) {
        newLines.add(line);
      }
    }

    newLines.replaceAll(this::replace);
    return newLines;
  }

  public String replace(String line) {
    String newLine = line;

    for (var single : singles.entrySet()) {
      newLine = newLine.replace(single.getKey(), single.getValue());
    }

    return MessageUtils.color(newLine);
  }
}

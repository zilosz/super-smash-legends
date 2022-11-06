package io.github.aura6.supersmashlegends.utils.file;

import java.io.File;

public class PathBuilder {

    public static String build(String... parts) {
        StringBuilder path = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            path.append(parts[i]);

            if (i < parts.length - 1) {
                path.append(File.separator);
            }
        }

        return path.toString();
    }
}

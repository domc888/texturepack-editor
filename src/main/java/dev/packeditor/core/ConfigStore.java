package dev.packeditor.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plain text config:
 * <pre>
 * [applied]
 * end_crystal=style:CRIMSON
 * [setup:My Setup]
 * totem=custom:FF8800
 * </pre>
 */
public final class ConfigStore {
    public static final int MAX_SAVED = 100;

    public Setup applied = new Setup();
    public final Map<String, Setup> saved = new LinkedHashMap<>();

    public static String cleanName(String raw) {
        if (raw == null) return "";
        String s = raw.replaceAll("[\\[\\]\\r\\n=]", "").trim();
        return s.length() > 32 ? s.substring(0, 32).trim() : s;
    }

    public static ConfigStore load(Path file) {
        ConfigStore cs = new ConfigStore();
        if (!Files.isRegularFile(file)) return cs;
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            return cs;
        }
        Setup current = null;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                String head = line.substring(1, line.length() - 1);
                if (head.equals("applied")) current = cs.applied;
                else if (head.startsWith("setup:") && cs.saved.size() < MAX_SAVED) {
                    String name = cleanName(head.substring(6));
                    if (name.isEmpty()) { current = null; continue; }
                    current = new Setup();
                    cs.saved.put(name, current);
                } else current = null;
                continue;
            }
            if (current == null) continue;
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            String id = line.substring(0, eq);
            if (Targets.byId(id) == null) continue;
            try {
                current.set(id, LookSpec.parse(line.substring(eq + 1)));
            } catch (IllegalArgumentException ignored) {
                // skip one bad line, keep the rest
            }
        }
        return cs;
    }

    public void save(Path file) throws IOException {
        StringBuilder sb = new StringBuilder("# Pack Editor config. Safe to delete.\n[applied]\n");
        write(sb, applied);
        for (Map.Entry<String, Setup> e : saved.entrySet()) {
            sb.append("[setup:").append(e.getKey()).append("]\n");
            write(sb, e.getValue());
        }
        Files.createDirectories(file.getParent());
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(tmp, sb.toString(), StandardCharsets.UTF_8);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void write(StringBuilder sb, Setup s) {
        for (Map.Entry<String, LookSpec> e : s.entries().entrySet()) {
            sb.append(e.getKey()).append('=').append(e.getValue().spec()).append('\n');
        }
    }
}

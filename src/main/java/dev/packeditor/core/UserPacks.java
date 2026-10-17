package dev.packeditor.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Reads textures straight out of the player's own resource packs (.zip or folder). */
public final class UserPacks {
    public static final String GENERATED_NAME = "packeditor_generated";
    private static final long MAX_ENTRY_BYTES = 8L * 1024 * 1024;

    private final Path dir;

    public UserPacks(Path resourcePacksDir) { this.dir = resourcePacksDir; }

    public Path dir() { return dir; }

    public List<String> list() {
        List<String> out = new ArrayList<>();
        if (!Files.isDirectory(dir)) return out;
        try (var stream = Files.list(dir)) {
            stream.forEach(p -> {
                String n = p.getFileName().toString();
                if (n.equals(GENERATED_NAME)) return;
                if (Files.isDirectory(p) && Files.isDirectory(p.resolve("assets"))) out.add(n);
                else if (Files.isRegularFile(p) && n.toLowerCase().endsWith(".zip")) out.add(n);
            });
        } catch (IOException ignored) {
            // unreadable folder: no packs
        }
        Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    /** Bytes of assets/minecraft/{path} inside the named pack. */
    public Optional<byte[]> read(String pack, String path) {
        if (pack == null || pack.isEmpty() || pack.contains("/") || pack.contains("\\") || pack.contains("..")) {
            return Optional.empty();
        }
        Path p = dir.resolve(pack).normalize();
        if (!p.startsWith(dir.normalize())) return Optional.empty();
        String inner = "assets/minecraft/" + path;
        try {
            if (Files.isDirectory(p)) {
                Path f = p.resolve(inner).normalize();
                if (!f.startsWith(p) || !Files.isRegularFile(f) || Files.size(f) > MAX_ENTRY_BYTES) return Optional.empty();
                return Optional.of(Files.readAllBytes(f));
            }
            if (Files.isRegularFile(p)) {
                try (ZipFile z = new ZipFile(p.toFile())) {
                    ZipEntry e = z.getEntry(inner);
                    if (e == null || e.isDirectory() || e.getSize() > MAX_ENTRY_BYTES) return Optional.empty();
                    try (InputStream in = z.getInputStream(e)) {
                        byte[] data = in.readNBytes((int) MAX_ENTRY_BYTES + 1);
                        if (data.length > MAX_ENTRY_BYTES) return Optional.empty();
                        return Optional.of(data);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            // unreadable pack: treat as "texture not present"
        }
        return Optional.empty();
    }

    /** True if the pack contains at least one texture of the target. */
    public boolean has(String pack, Target t) {
        for (String path : t.paths()) if (read(pack, path).isPresent()) return true;
        return false;
    }
}

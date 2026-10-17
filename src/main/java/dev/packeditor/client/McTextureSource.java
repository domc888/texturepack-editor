package dev.packeditor.client;

import dev.packeditor.core.TextureSource;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Reads the ORIGINAL vanilla textures. getResourceStack returns every copy of a file from
 * lowest to highest priority, so index 0 is the vanilla one even while our generated pack
 * (or any other pack) is overriding it.
 */
final class McTextureSource implements TextureSource {

    @Override
    public Optional<byte[]> vanilla(String path) { return read(path); }

    @Override
    public Optional<byte[]> vanillaMeta(String path) { return read(path + ".mcmeta"); }

    private static Optional<byte[]> read(String path) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath("minecraft", path);
            ResourceManager rm = Minecraft.getInstance().getResourceManager();
            List<Resource> stack = rm.getResourceStack(id);
            if (stack.isEmpty()) return Optional.empty();
            try (InputStream in = stack.get(0).open()) {
                return Optional.of(in.readAllBytes());
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

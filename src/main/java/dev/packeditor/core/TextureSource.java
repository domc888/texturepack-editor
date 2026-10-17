package dev.packeditor.core;

import java.util.Optional;

/** Where original (vanilla) texture bytes come from. The game implements this. */
public interface TextureSource {
    /** Bytes of assets/minecraft/{path}, or empty if the game has no such file. */
    Optional<byte[]> vanilla(String path);

    /** Bytes of assets/minecraft/{path}.mcmeta (animation info etc.), or empty. */
    Optional<byte[]> vanillaMeta(String path);
}

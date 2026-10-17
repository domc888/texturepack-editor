package dev.packeditor.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.packeditor.client.screen.MainScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class PackEditorClient implements ClientModInitializer {
    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        System.setProperty("java.awt.headless", "true"); // we only use ImageIO, never a window

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("packeditor", "main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.packeditor.open", InputConstants.Type.KEYSYM, InputConstants.KEY_O, category));

        Editor.get(); // load config early

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                Editor.open(new MainScreen(null));
            }
        });
    }
}

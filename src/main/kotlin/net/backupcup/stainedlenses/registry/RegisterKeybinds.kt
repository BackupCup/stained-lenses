package net.backupcup.stainedlenses.registry

import io.netty.buffer.Unpooled
import net.backupcup.stainedlenses.screens.spyglassScreen.SpyglassScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.network.PacketByteBuf
import org.lwjgl.glfw.GLFW


object RegisterKeybinds {
    val openTinkery = KeyBindingHelper.registerKeyBinding(KeyBinding(
    "key.stained-lenses.open_spyglass_tinkery",
    InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
    "category.stained-lenses.keybinding_category"
    ))

    fun registerKeybinds() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            if (openTinkery.wasPressed() && client.currentScreen !is SpyglassScreen && client.player?.isUsingSpyglass == false) {
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeBoolean(true)

                ClientPlayNetworking.send(RegisterPackets.SPYGLASS_SCREEN_PACKET, buf)
            }
        })
    }
}
package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.screens.spyglassScreen.SpyglassScreen
import net.backupcup.stainedlenses.screens.spyglassScreen.SpyglassScreenHandler
import net.backupcup.stainedlenses.utils.DataHelper
import net.backupcup.stainedlenses.utils.FocalLensServer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier

object RegisterPackets {
    val SPYGLASS_UI_UPDATE = Identifier(StainedLenses.MOD_ID, "spyglass_ui_update")
    val SPYGLASS_SCREEN_PACKET = Identifier(StainedLenses.MOD_ID, "spyglass_screen_packet")
    val RECIEVE_LENS_ZOOM = Identifier(StainedLenses.MOD_ID, "receive_lens_zoom")
    val FOCAL_LENS_UPDATE = Identifier(StainedLenses.MOD_ID, "focal_lens_update")

    fun registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(
            SPYGLASS_UI_UPDATE,
            RegisterPackets::syncSpyglassScreenData
        )
    }

    fun registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(
            SPYGLASS_SCREEN_PACKET,
            RegisterPackets::openSpyglassScreen
        )

        ServerPlayNetworking.registerGlobalReceiver(
            RECIEVE_LENS_ZOOM,
            RegisterPackets::recieveLensZoom
        )

        ServerPlayNetworking.registerGlobalReceiver(
            FOCAL_LENS_UPDATE,
            RegisterPackets::updateFocalLensDurability
        )
    }

    private fun syncSpyglassScreenData(client: MinecraftClient, handler: ClientPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
        val activeScreen = MinecraftClient.getInstance().currentScreen as? SpyglassScreen ?: return

        val lensStack = buf.readItemStack()
        val moduleStack = buf.readItemStack()
        val pouchStack = buf.readItemStack()

        activeScreen.updateScreenData(lensStack, moduleStack, pouchStack)
    }

    private fun recieveLensZoom(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
        val spyglassStack = buf.readItemStack()
        val lensStack = if (DataHelper.getLensStack(spyglassStack) != ItemStack.EMPTY) DataHelper.getLensStack(spyglassStack) else return

        lensStack.orCreateNbt.putFloat("savedZoom", buf.readFloat())
        DataHelper.storeItemStackInAnother("lensSlot", lensStack, player.getStackInHand(player.activeHand))
        (player as FocalLensServer).setShouldBeam(false)
    }

    private fun openSpyglassScreen(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
        val itemStack = player.getStackInHand(Hand.MAIN_HAND)
        if (!itemStack.isOf(Items.SPYGLASS)) return

        val screenHandlerFactory: NamedScreenHandlerFactory = object : NamedScreenHandlerFactory {
            override fun createMenu(
                syncId: Int,
                playerInventory: PlayerInventory,
                player: PlayerEntity
            ): ScreenHandler {
                return SpyglassScreenHandler(syncId, playerInventory, player, ScreenHandlerContext.EMPTY, itemStack)
            }

            override fun getDisplayName(): Text {
                return Text.translatable("container.stained-lenses.spyglass_tinkery")
            }
        }

        player.playSound(SoundEvents.ITEM_SPYGLASS_USE, SoundCategory.PLAYERS, 1.0f, 1.0f)
        player.openHandledScreen(screenHandlerFactory)
    }

    private fun updateFocalLensDurability(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
        val itemStack = player.getStackInHand(player.activeHand)
        if (!itemStack.isOf(Items.SPYGLASS)) return

        val isBeaming = buf.readBoolean()
        if (!isBeaming) {
            val heldCharge = buf.readInt()

            val lensStack = DataHelper.getLensStack(itemStack)
            lensStack.damage += heldCharge
            DataHelper.storeItemStackInAnother("lensSlot", lensStack, player.getStackInHand(player.activeHand))
        }

        (player as FocalLensServer).setShouldBeam(isBeaming)
    }
}
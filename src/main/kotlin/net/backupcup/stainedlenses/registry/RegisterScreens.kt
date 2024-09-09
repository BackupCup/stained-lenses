package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.items.PouchItem
import net.backupcup.stainedlenses.screens.pouchScreen.PouchScreen
import net.backupcup.stainedlenses.screens.spyglassScreen.SpyglassScreen
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.item.ItemStack

object RegisterScreens {
    val SPYGLASS_SCREEN = ScreenRegistry.register(RegisterScreenHandlers.SPYGLASS_SCREEN_HANDLER) { handler, playerInventory, title ->
        SpyglassScreen(handler, playerInventory, title, playerInventory.mainHandStack)
    }
    val POUCH_SCREEN = ScreenRegistry.register(RegisterScreenHandlers.POUCH_SCREEN_HANDLER) { handler, playerInventory, title ->
        PouchScreen(handler, playerInventory, title)
    }

    fun registerScreens() {
        SPYGLASS_SCREEN
        POUCH_SCREEN
    }
}
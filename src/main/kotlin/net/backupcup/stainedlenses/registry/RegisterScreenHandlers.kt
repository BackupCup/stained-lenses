package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.screens.pouchScreen.PouchScreenHandler
import net.backupcup.stainedlenses.screens.spyglassScreen.SpyglassScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier

object RegisterScreenHandlers {
    val SPYGLASS_SCREEN_HANDLER: ScreenHandlerType<SpyglassScreenHandler> = ScreenHandlerRegistry.registerSimple(
        Identifier(StainedLenses.MOD_ID, "spyglass_screen_handler"), ::SpyglassScreenHandler)
    val POUCH_SCREEN_HANDLER: ScreenHandlerType<PouchScreenHandler> = ScreenHandlerRegistry.registerSimple(
        Identifier(StainedLenses.MOD_ID, "pouch_screen_handler"), ::PouchScreenHandler)

    fun registerScreenHandlers() {
        SPYGLASS_SCREEN_HANDLER
        POUCH_SCREEN_HANDLER
    }
}
package net.backupcup.stainedlenses

import net.backupcup.stainedlenses.blocks.EtherealBlockRenderer
import net.backupcup.stainedlenses.registry.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import org.slf4j.LoggerFactory


object StainedLensesClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("stained-lenses")

	override fun onInitializeClient() {
		RegisterScreens.registerScreens()
		RegisterKeybinds.registerKeybinds()
		RegisterPackets.registerClientPackets()
		RegisterSounds.registerSounds()
		//RegisterRendering.registerAll()


		RegisterParticles.registerCParticles()

		RegisterBlocks.registerBlockCutouts()
		BlockEntityRendererRegistry.register(RegisterBlocks.ETHEREAL_BLOCK_ENTITY) { EtherealBlockRenderer() }
	}
}
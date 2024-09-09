package net.backupcup.stainedlenses

import net.backupcup.stainedlenses.registry.*
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory


object StainedLenses : ModInitializer {
	const val MOD_ID = "stained-lenses"
	var logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		RegisterScreenHandlers.registerScreenHandlers()
		RegisterPackets.registerServerPackets()
		RegisterItems.registerItems()
		RegisterItemGroups.registerItemGroup()
		RegisterBlocks.registerBlocks()
		RegisterBlocks.registerBlockEntities()

		RegisterTimedEvents.registerServerTick()
		RegisterParticles.registerSParticles()

		ModifyLootTables.registerLootModifiers()

		/**
		TODO LIST:

		- Lenses:
			Marked Lens

			add:
    			/*
    			Optimize the charge bar by rendering a white rectangle and then particles on top
    			*/
		- Modules:
			"night vision" module (but actual night vision like irl specialized glasses) (post processing with Lodestone)
			Binoculars module
		 */
	}

	/*
	IMMA GO EAT
		I ATE A SINGULAR COOKIE, IMMA GO GRAB MORE
		I ATE ANOTHER SINGULAR COOKIE, IM SATISFIED
		I ATE 2 WHOLE COOKIES, GREAT
		I ATE A PIECE OF COOKED MEAT, NICE
	*/
}
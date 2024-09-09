package net.backupcup.stainedlenses

import net.backupcup.stainedlenses.datagen.DatagenLoot
import net.backupcup.stainedlenses.datagen.DatagenRecipes
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object StainedLensesDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		val pack: FabricDataGenerator.Pack = fabricDataGenerator.createPack()

		pack.addProvider(::DatagenRecipes)
		pack.addProvider(::DatagenLoot)
	}
}
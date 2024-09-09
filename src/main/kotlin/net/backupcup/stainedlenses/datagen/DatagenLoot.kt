package net.backupcup.stainedlenses.datagen

import net.backupcup.stainedlenses.registry.RegisterBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider

class DatagenLoot(dataOutput: FabricDataOutput?) : FabricBlockLootTableProvider(dataOutput) {
    override fun generate() {
        addDrop(RegisterBlocks.ETHEREAL_BLOCK, drops(RegisterBlocks.ETHEREAL_BLOCK.asItem()))
    }
}
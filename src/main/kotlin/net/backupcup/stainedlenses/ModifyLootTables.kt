package net.backupcup.stainedlenses

import net.backupcup.stainedlenses.registry.RegisterItems
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.loot.v2.LootTableSource
import net.minecraft.loot.LootManager
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.LootTables
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.SetDamageLootFunction
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object ModifyLootTables {
    private val bastionLoot = arrayOf(LootTables.BASTION_TREASURE_CHEST)
    private fun modifyBastion() {
        LootTableEvents.MODIFY.register(LootTableEvents.Modify {
                _: ResourceManager?,
                _: LootManager?,
                id: Identifier?, tableBuilder:
                LootTable.Builder,
                source: LootTableSource ->

            if (id != null) {
                if (source.isBuiltin && bastionLoot.contains(id)) {
                    val poolBuilder = LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0.25f, 1f))
                        .with(ItemEntry.builder(RegisterItems.FOCAL_LENS)
                            .apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0f, 0.25f)))
                            .weight(100)
                        )
                    tableBuilder.pool(poolBuilder)
                }
            }
        })
    }

    fun registerLootModifiers() {
        modifyBastion()
    }
}
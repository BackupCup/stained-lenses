package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.items.ModifierUtil
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object RegisterItemGroups {
    fun registerItemGroup(): ItemGroup {
        return Registry.register(
            Registries.ITEM_GROUP, Identifier(StainedLenses.MOD_ID, "modifiers"), FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stained-lenses.modifiers"))
            .icon { ItemStack(RegisterItems.DEFAULT_LENS) }
            .entries { _, entries ->
                entries.add(ItemStack(RegisterItems.ATTACHEMENT_POUCH))
                Registries.ITEM.forEach { item ->
                    if (item is ModifierUtil) {
                        if (item != RegisterItems.FOCAL_LENS) entries.add(ItemStack(item))
                        else {
                            val focalLensStack = ItemStack(item)
                            focalLensStack.damage = 256
                            entries.add(focalLensStack)
                        }
                    }
                }
                entries.add(ItemStack(RegisterBlocks.ETHEREAL_BLOCK.asItem()))
            }.build()
        )
    }
}
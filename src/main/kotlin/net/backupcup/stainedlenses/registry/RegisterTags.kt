package net.backupcup.stainedlenses.registry

import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier


object RegisterTags {
    val C_HEADS: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c", "heads"))
    val C_GLASS: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c", "glass_blocks"))
    val C_GLASS_PANES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c", "glass_panes"))
}
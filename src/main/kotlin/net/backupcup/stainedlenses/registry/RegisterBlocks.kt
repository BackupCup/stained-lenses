package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.blocks.EtherealBlock
import net.backupcup.stainedlenses.blocks.EtherealBlockEntity
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.BlockItem
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object RegisterBlocks {
    val ETHEREAL_BLOCK: Block = EtherealBlock(
        FabricBlockSettings.create()
        .strength(0.5f)
        .sounds(BlockSoundGroup.LARGE_AMETHYST_BUD)
        .nonOpaque()
        .allowsSpawning(Blocks::never)
        .solidBlock(Blocks::never)
        .suffocates(Blocks::never)
        .blockVision(Blocks::never)
        .pistonBehavior(PistonBehavior.NORMAL)
    )

    val ETHEREAL_BLOCK_ENTITY: BlockEntityType<EtherealBlockEntity> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier(StainedLenses.MOD_ID, "ethereal_block_entity"),
        FabricBlockEntityTypeBuilder.create({ pos: BlockPos, state: BlockState -> EtherealBlockEntity(pos, state)}, ETHEREAL_BLOCK).build()
    )

    fun registerBlocks() {
        Registry.register(Registries.BLOCK, Identifier(StainedLenses.MOD_ID, "ethereal_block"), ETHEREAL_BLOCK)
        Registry.register(Registries.ITEM, Identifier(StainedLenses.MOD_ID, "ethereal_block"), BlockItem(ETHEREAL_BLOCK, FabricItemSettings()))
    }

    fun registerBlockCutouts() {
        BlockRenderLayerMap.INSTANCE.putBlock(ETHEREAL_BLOCK, RenderLayer.getCutout())
    }

    fun registerBlockEntities() {
        ETHEREAL_BLOCK_ENTITY
    }
}
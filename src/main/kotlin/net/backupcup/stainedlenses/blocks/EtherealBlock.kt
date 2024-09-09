package net.backupcup.stainedlenses.blocks

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.mixin.client.KeybindingAccessor
import net.backupcup.stainedlenses.registry.RegisterItems
import net.backupcup.stainedlenses.utils.DataHelper
import net.backupcup.stainedlenses.utils.TextWrapUtils
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class EtherealBlock(settings: Settings?) : BlockWithEntity(settings) {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return EtherealBlockEntity(pos, state)
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: BlockView?,
        tooltip: MutableList<Text>,
        options: TooltipContext
    ) {
        val boundSneakKey = (MinecraftClient.getInstance().options.sneakKey as KeybindingAccessor).boundKey

        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, boundSneakKey.code)) {
            TextWrapUtils.wrapText((MinecraftClient.getInstance().window.width / 1.5f).toInt(),
                "block.stained-lenses.ethereal_block.desc", Formatting.GRAY).forEach { text -> tooltip.add(text) }
        } else {
            tooltip.add(Text.translatable("tooltip.stained-lenses.hold_crouch_desc",
                Text.translatable(MinecraftClient.getInstance().options.sneakKey.boundKeyTranslationKey)
                    .formatted(Formatting.GOLD)).formatted(Formatting.GRAY))
        }

        super.appendTooltip(stack, world, tooltip, options)
    }

    override fun getCollisionShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return VoxelShapes.fullCube()
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        val player = MinecraftClient.getInstance().player
        var isVisible = false

        if (player?.offHandStack?.isOf(Items.SPYGLASS) == true) {
            if (DataHelper.getLensStack(player.offHandStack as ItemStack).isOf(RegisterItems.ETHEREAL_LENS)) isVisible = true
        } else {
            if (player?.mainHandStack?.isOf(Items.SPYGLASS) == true)
                if (DataHelper.getLensStack(player.mainHandStack as ItemStack).isOf(RegisterItems.ETHEREAL_LENS)) isVisible = true
        }

        return if (isVisible) {
            super.getOutlineShape(state, world, pos, context)
        } else return VoxelShapes.empty()
    }

    override fun isTransparent(state: BlockState, world: BlockView, pos: BlockPos): Boolean { return true }
    override fun getRenderType(state: BlockState): BlockRenderType { return BlockRenderType.INVISIBLE }
    override fun getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos): Float { return 1.0f }
}
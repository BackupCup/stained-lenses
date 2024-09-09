package net.backupcup.stainedlenses.blocks

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.registry.RegisterItems
import net.backupcup.stainedlenses.utils.DataHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import org.joml.Vector3f
import team.lodestar.lodestone.handlers.RenderHandler
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry
import team.lodestar.lodestone.systems.rendering.LodestoneRenderType
import team.lodestar.lodestone.systems.rendering.VFXBuilders
import team.lodestar.lodestone.systems.rendering.rendeertype.RenderTypeToken
import java.awt.Color

class EtherealBlockRenderer: BlockEntityRenderer<EtherealBlockEntity> {
    private val QUAD: LodestoneRenderType = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(
        RenderTypeToken.createToken(Identifier(StainedLenses.MOD_ID, "textures/vfx/ethereal_block/layer_0.png")))

    private fun generateFaceQuads(): Map<Direction, Pair<Array<Vector3f>, Array<Vector3f>>> {
        val quads = mutableMapOf<Direction, Pair<Array<Vector3f>, Array<Vector3f>>>()

        val positions = mapOf(
            Direction.UP    to arrayOf(0.01f, 0.99f, 0.01f, 0.99f, 0.99f, 0.01f, 0.99f, 0.99f, 0.99f, 0.01f, 0.99f, 0.99f),
            Direction.DOWN  to arrayOf(0.01f, 0.01f, 0.01f, 0.99f, 0.01f, 0.01f, 0.99f, 0.01f, 0.99f, 0.01f, 0.01f, 0.99f),
            Direction.SOUTH to arrayOf(0.01f, 0.01f, 0.99f, 0.99f, 0.01f, 0.99f, 0.99f, 0.99f, 0.99f, 0.01f, 0.99f, 0.99f),
            Direction.NORTH to arrayOf(0.01f, 0.01f, 0.01f, 0.99f, 0.01f, 0.01f, 0.99f, 0.99f, 0.01f, 0.01f, 0.99f, 0.01f),
            Direction.WEST  to arrayOf(0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.99f, 0.01f, 0.99f, 0.99f, 0.01f, 0.99f, 0.01f),
            Direction.EAST  to arrayOf(0.99f, 0.01f, 0.01f, 0.99f, 0.01f, 0.99f, 0.99f, 0.99f, 0.99f, 0.99f, 0.99f, 0.01f)
        )

        positions.forEach { (direction, pos) ->
            quads[direction] = Pair(
                arrayOf(
                    Vector3f(pos[0], pos[1], pos[2]),
                    Vector3f(pos[3], pos[4], pos[5]),
                    Vector3f(pos[6], pos[7], pos[8]),
                    Vector3f(pos[9], pos[10], pos[11])),
                arrayOf(
                    Vector3f(pos[0], pos[1], pos[2]),
                    Vector3f(pos[9], pos[10], pos[11]),
                    Vector3f(pos[6], pos[7], pos[8]),
                    Vector3f(pos[3], pos[4], pos[5]))
            )
        }

        return quads
    }

    override fun render(
        entity: EtherealBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val player = MinecraftClient.getInstance().player

        if (player?.isUsingSpyglass == true) {
            if (DataHelper.getLensStack(player.getStackInHand(player.activeHand)).isOf(RegisterItems.ETHEREAL_LENS)) {
                if (entity.transparency < 0.5f) entity.transparency += 0.0125f
            } else if (entity.transparency > 0f) entity.transparency -= 0.0125f
        } else if (entity.transparency > 0f) entity.transparency -= 0.0125f

        mutableListOf(Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH).forEach { direction ->
            if (entity.world?.getBlockEntity(entity.pos.offset(direction)) is EtherealBlockEntity)
                entity.connectedList.remove(direction)
            else if (!entity.connectedList.contains(direction)) entity.connectedList.add(direction)
        }

        if (entity.transparency > 0f) {
            matrices.push()

                val builder = VFXBuilders.createWorld()
                builder.replaceBufferSource(RenderHandler.LATE_DELAYED_RENDER.target)
                    .setRenderType(QUAD)
                    .setColor(Color(getCurrentColor(entity.world!!.time)))
                    .setAlpha(entity.transparency)

                val quads = generateFaceQuads()
                quads.forEach { (direction, quadPair) ->
                    if (entity.connectedList.contains(direction)) {
                        builder.renderQuad(matrices, quadPair.first, 1f)
                        builder.renderQuad(matrices, quadPair.second, 1f)
                    }
                }

            matrices.pop()
        }
    }

    private fun getCurrentColor(currentTick: Long): Int {
        val position = (currentTick % 100f) / 100f

        return if (position <= 0.5f) interpolateColor(0x8fd3ff, 0x0d8780, position * 2)
               else interpolateColor(0x0d8780, 0x8fd3ff, (position - 0.5f) * 2)
    }

    private fun interpolateColor(color1: Int, color2: Int, ratio: Float): Int {
        val r = interpolateComponent((color1 shr 16) and 0xFF, (color2 shr 16) and 0xFF, ratio)
        val g = interpolateComponent((color1 shr 8) and 0xFF, (color2 shr 8) and 0xFF, ratio)
        val b = interpolateComponent(color1 and 0xFF, color2 and 0xFF, ratio)

        return (r shl 16) or (g shl 8) or b
    }

    private fun interpolateComponent(c1: Int, c2: Int, ratio: Float): Int { return Math.round(c1 + (c2 - c1) * ratio) }
}
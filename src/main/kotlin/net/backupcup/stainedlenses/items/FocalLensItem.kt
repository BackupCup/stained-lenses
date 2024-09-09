package net.backupcup.stainedlenses.items

import io.github.fabricators_of_create.porting_lib.item.DamageableItem
import net.backupcup.stainedlenses.mixin.client.KeybindingAccessor
import net.backupcup.stainedlenses.utils.TextWrapUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.Pair
import net.minecraft.world.World
import team.lodestar.lodestone.handlers.screenparticle.ParticleEmitterHandler
import team.lodestar.lodestone.registry.common.particle.LodestoneScreenParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.ScreenParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData
import team.lodestar.lodestone.systems.particle.screen.ScreenParticleHolder
import java.awt.Color
import kotlin.math.max
import kotlin.math.sin

class FocalLensItem(
    settings: Settings?,
    GUITexture: Identifier,
    tooltipList: List<Text>,
    zoomLimit: Float
) : LensItem(settings, GUITexture,
    tooltipList,
    zoomLimit
), DamageableItem, ParticleEmitterHandler.ItemParticleSupplier {

    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>, context: TooltipContext?) {
        val boundSneakKey = (MinecraftClient.getInstance().options.sneakKey as KeybindingAccessor).boundKey
        val boundAttackKey = (MinecraftClient.getInstance().options.attackKey as KeybindingAccessor).boundKey

        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, boundSneakKey.code)) {
            TextWrapUtils.wrapText((MinecraftClient.getInstance().window.width / 1.5f).toInt(), "item.stained-lenses.focal_lens.desc", Formatting.GRAY).forEach {
                    formattedText -> tooltip.add(formattedText) }

            TextWrapUtils.wrapText((MinecraftClient.getInstance().window.width / 1.5f).toInt(),
                Text.translatable("item.stained-lenses.focal_lens.desc_use", Text.translatable(boundAttackKey.translationKey).formatted(Formatting.GOLD)), Formatting.GRAY).forEach {
                    formattedText -> tooltip.add(formattedText) }
        } else {
            tooltip.add(Text.translatable("tooltip.stained-lenses.hold_crouch_desc",
                Text.translatable(MinecraftClient.getInstance().options.sneakKey.boundKeyTranslationKey)
                    .formatted(Formatting.GOLD)).formatted(Formatting.GRAY))
        }
    }

    override fun isItemBarVisible(stack: ItemStack?): Boolean {
        return true
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        val f = max(0.0, ((maxDamage.toFloat() - stack.damage.toFloat()) / maxDamage.toFloat()).toDouble()).toFloat()

        return when {
            f > 0.66 -> { interpolateColor(0xE6904E,0xFFE0BC, (f - 0.66F) / 0.34F) }
            f > 0.33 -> { interpolateColor(0x6E2727, 0xE6904E, (f - 0.33F) / 0.33F) }
            else -> { 0x6E2727 }
        }
    }

    private fun interpolateColor(colorStart: Int, colorEnd: Int, fraction: Float): Int {
        val startRed = (colorStart shr 16) and 0xFF
        val startGreen = (colorStart shr 8) and 0xFF
        val startBlue = colorStart and 0xFF

        val endRed = (colorEnd shr 16) and 0xFF
        val endGreen = (colorEnd shr 8) and 0xFF
        val endBlue = colorEnd and 0xFF

        val red = (startRed + (endRed - startRed) * fraction).toInt()
        val green = (startGreen + (endGreen - startGreen) * fraction).toInt()
        val blue = (startBlue + (endBlue - startBlue) * fraction).toInt()

        return (red shl 16) or (green shl 8) or blue
    }

    override fun spawnLateParticles(
        target: ScreenParticleHolder,
        world: World,
        partialTick: Float,
        stack: ItemStack,
        x: Float,
        y: Float
    ) {
        val transparency: Float = (1f - (stack.damage - 1f) / stack.maxDamage) * 0.75f
        val gameTime: Float = world.time + partialTick
        val colorPair = Pair(Color(0xE6904E), Color(0xFFE0BC))

        val spinDataBuilder =
            SpinParticleData.create(0f, 1f).setSpinOffset(0.025f * gameTime % 6.28f).setEasing(Easing.EXPO_IN_OUT)

        ScreenParticleBuilder.create(LodestoneScreenParticleRegistry.STAR, target)
            .setTransparencyData(GenericParticleData.create(0.11f * transparency, 0f).setEasing(Easing.QUINTIC_IN).build())
            .setLifetime(7)
            .setRandomOffset(0.05)

            .setScaleData(GenericParticleData.create((transparency - sin((gameTime * 0.075f).toDouble()) * 0.125f).toFloat(), 0f
                ).build())

            .setColorData(ColorParticleData.create(colorPair.left, colorPair.right).build())
            .setSpinData(spinDataBuilder.setSpinOffset(0.785f - 0.01f * gameTime % 6.28f).build())
            .spawnOnStack(-4.0, -4.0)
    }

    override fun spawnEarlyParticles(
        target: ScreenParticleHolder,
        world: World,
        partialTick: Float,
        stack: ItemStack,
        x: Float,
        y: Float
    ) {
        val transparency: Float = (1f - (stack.damage - 1f) / stack.maxDamage) * 0.75f
        val gameTime: Float = world.time + partialTick
        val colorPair = Pair(Color(0xE6904E), Color(0xFFE0BC))

        val spinDataBuilder =
            SpinParticleData.create(0f, 1f).setSpinOffset(0.025f * gameTime % 6.28f).setEasing(Easing.EXPO_IN_OUT)

        ScreenParticleBuilder.create(LodestoneScreenParticleRegistry.WISP, target)
            .setTransparencyData(GenericParticleData.create(0.11f * transparency, 0f).setEasing(Easing.QUINTIC_IN).build())
            .setLifetime(7)
            .setRandomOffset(0.05)

            .setScaleData(GenericParticleData.create((transparency - sin((gameTime * 0.05f).toDouble()) * 0.125f).toFloat(), 0f
            ).build())

            .setColorData(ColorParticleData.create(colorPair.left, colorPair.right).build())
            .setSpinData(spinDataBuilder.setSpinOffset(0.785f - 0.01f * gameTime % 6.28f).build())
            .spawnOnStack(-4.0, -4.0)
    }
}
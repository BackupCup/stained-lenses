package net.backupcup.stainedlenses.items

import net.backupcup.stainedlenses.mixin.client.KeybindingAccessor
import net.backupcup.stainedlenses.utils.TextWrapUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.InputUtil
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.World

open class LensItem(settings: Settings?,
                    override val GUITexture: Identifier,
                    private val tooltipList: List<Text>,
                    val zoomLimit: Float,
) : Item(settings), ModifierUtil {

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        val boundSneakKey = (MinecraftClient.getInstance().options.sneakKey as KeybindingAccessor).boundKey

        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, boundSneakKey.code)) {
            tooltipList.forEach {
                text -> TextWrapUtils.wrapText((MinecraftClient.getInstance().window.width / 1.5f).toInt(), text, Formatting.GRAY).forEach {
                    formattedText -> tooltip.add(formattedText) } }
        } else {
            tooltip.add(Text.translatable("tooltip.stained-lenses.hold_crouch_desc",
                Text.translatable(MinecraftClient.getInstance().options.sneakKey.boundKeyTranslationKey)
                    .formatted(Formatting.GOLD)).formatted(Formatting.GRAY))
        }

        super.appendTooltip(stack, world, tooltip, context)
    }
}
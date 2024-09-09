package net.backupcup.stainedlenses.items

import net.backupcup.stainedlenses.mixin.client.KeybindingAccessor
import net.backupcup.stainedlenses.screens.pouchScreen.PouchScreenHandler
import net.backupcup.stainedlenses.utils.TextWrapUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class PouchItem(
    settings: Settings?,
    private val tooltipList: List<Text>
) : Item(settings) {
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
            tooltip.add(
                Text.translatable("tooltip.stained-lenses.hold_crouch_desc",
                Text.translatable(MinecraftClient.getInstance().options.sneakKey.boundKeyTranslationKey)
                    .formatted(Formatting.GOLD)).formatted(Formatting.GRAY))
        }

        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)

        val screenHandlerFactory: NamedScreenHandlerFactory = object : NamedScreenHandlerFactory {
            override fun createMenu(
                syncId: Int,
                playerInventory: PlayerInventory,
                player: PlayerEntity
            ): ScreenHandler {
                return PouchScreenHandler(syncId, playerInventory, player, itemStack)
            }

            override fun getDisplayName(): Text {
                return Text.translatable("container.stained-lenses.attachement_pouch")
            }
        }

        user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.0f, 1.0f)
        user.openHandledScreen(screenHandlerFactory)
        return TypedActionResult.success(itemStack, true)
    }
}
package net.backupcup.stainedlenses.screens.spyglassScreen

import com.mojang.blaze3d.systems.RenderSystem
import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.items.LensItem
import net.backupcup.stainedlenses.items.ModuleItem
import net.backupcup.stainedlenses.registry.RegisterItems
import net.backupcup.stainedlenses.utils.DataHelper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import team.lodestar.lodestone.handlers.screenparticle.ParticleEmitterHandler

class SpyglassScreen(handler: SpyglassScreenHandler?,
                     inventory: PlayerInventory?,
                     title: Text?,
                     itemStack: ItemStack
) : HandledScreen<SpyglassScreenHandler>(handler, inventory, title), ParticleEmitterHandler.ItemParticleSupplier {
    private var TEXTURE = Identifier(StainedLenses.MOD_ID, "textures/gui/spyglass_tinkery.png")

    private var lensItemStack: ItemStack = DataHelper.getLensStack(itemStack)
    private var moduleItemStack: ItemStack = DataHelper.getModuleStack(itemStack)
    private var pouchItemStack: ItemStack = DataHelper.getPouchStack(itemStack)

    override fun init() {
        backgroundWidth = 172
        backgroundHeight = 188

        x = (width - backgroundWidth) / 2
        y = (height - backgroundHeight) / 2

        super.init()
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, TEXTURE)

        if (pouchItemStack.isOf(RegisterItems.ATTACHEMENT_POUCH)) {}
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)

        if (lensItemStack.isEmpty) context.drawTexture(TEXTURE, x + 116, y + 75, 0, 240, 16, 16)
        if (pouchItemStack.isEmpty) context.drawTexture(TEXTURE, x + 90, y + 75, 16, 240, 16, 16)

        if (pouchItemStack.isOf(RegisterItems.ATTACHEMENT_POUCH)) context.drawTexture(TEXTURE, x + 172, y + 98, 172, 98, 42, 87)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        RenderSystem.setShaderTexture(0, TEXTURE)

        if (!moduleItemStack.isEmpty && moduleItemStack.item is ModuleItem) {
            context.drawTexture((moduleItemStack.item as ModuleItem).GUITexture,
                x + (moduleItemStack.item as ModuleItem).GUIPos.x, y + (moduleItemStack.item as ModuleItem).GUIPos.y,
                0f, 0f, 64, 64, 64, 64)
        }

        if (!lensItemStack.isEmpty && lensItemStack.item is LensItem) {
            context.drawTexture((lensItemStack.item as LensItem).GUITexture, x + 12, y + 33, 0f, 0f,
                64, 64, 64, 64)
        }

        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun isClickOutsideBounds(mouseX: Double, mouseY: Double, left: Int, top: Int, button: Int): Boolean {
        return mouseX < left.toDouble() || mouseY < top.toDouble() ||
                mouseX >= (left + this.backgroundWidth + if (pouchItemStack.isOf(RegisterItems.ATTACHEMENT_POUCH)) 44 else 0).toDouble() ||
                mouseY >= (top + this.backgroundHeight).toDouble()
    }

    //overridden for not rendering the Inventory texts
    override fun drawForeground(context: DrawContext?, mouseX: Int, mouseY: Int) {}

    fun updateScreenData(lensStack: ItemStack, moduleStack: ItemStack, pouchStack: ItemStack) {
        this.lensItemStack = lensStack
        this.moduleItemStack = moduleStack
        this.pouchItemStack = pouchStack
    }
}
package net.backupcup.stainedlenses.screens.pouchScreen

import com.mojang.blaze3d.systems.RenderSystem
import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.items.LensItem
import net.backupcup.stainedlenses.items.ModuleItem
import net.backupcup.stainedlenses.utils.DataHelper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import team.lodestar.lodestone.handlers.screenparticle.ParticleEmitterHandler

class PouchScreen(handler: PouchScreenHandler?,
                     inventory: PlayerInventory?,
                     title: Text?
) : HandledScreen<PouchScreenHandler>(handler, inventory, title), ParticleEmitterHandler.ItemParticleSupplier {
    private var TEXTURE = Identifier(StainedLenses.MOD_ID, "textures/gui/attachement_pouch.png")

    override fun init() {
        backgroundWidth = 168
        backgroundHeight = 122

        x = (width - backgroundWidth) / 2
        y = (height - backgroundHeight) / 2

        super.init()
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, TEXTURE)

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        RenderSystem.setShaderTexture(0, TEXTURE)

        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    //overridden for not rendering the Inventory texts
    override fun drawForeground(context: DrawContext?, mouseX: Int, mouseY: Int) {}
}
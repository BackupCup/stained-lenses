package net.backupcup.stainedlenses.rendering

import net.backupcup.stainedlenses.StainedLenses
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import team.lodestar.lodestone.systems.postprocess.PostProcessor

class NightVisionShader: PostProcessor() {
    companion object {
        val INSTANCE: NightVisionShader = NightVisionShader()
    }

    override fun getPostChainLocation(): Identifier {
        return Identifier(StainedLenses.MOD_ID, "night_vision")
    }

    override fun beforeProcess(viewModelStack: MatrixStack?) {}

    override fun afterProcess() {}
}
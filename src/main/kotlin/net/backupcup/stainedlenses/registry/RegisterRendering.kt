package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.rendering.NightVisionShader
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler


object RegisterRendering {
    fun registerAll() {
        PostProcessHandler.addInstance(NightVisionShader.INSTANCE)
    }
}
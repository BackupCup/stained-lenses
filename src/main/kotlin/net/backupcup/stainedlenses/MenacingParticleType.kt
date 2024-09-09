package net.backupcup.stainedlenses

import net.fabricmc.fabric.impl.client.particle.FabricSpriteProviderImpl
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import team.lodestar.lodestone.systems.particle.world.LodestoneWorldParticle
import team.lodestar.lodestone.systems.particle.world.options.WorldParticleOptions
import team.lodestar.lodestone.systems.particle.world.type.LodestoneWorldParticleType

class MenacingParticleType: LodestoneWorldParticleType() {

    companion object {
        val sprite = Identifier(StainedLenses.MOD_ID, "textures/particle/menacing_sign.png")
    }
}
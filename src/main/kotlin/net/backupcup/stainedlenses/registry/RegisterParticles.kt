package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import team.lodestar.lodestone.systems.particle.world.type.LodestoneWorldParticleType


object RegisterParticles {
    val MENACING_PARTICLE = LodestoneWorldParticleType()

    fun registerCParticles() {
        ParticleFactoryRegistry.getInstance().register(MENACING_PARTICLE) { sprite: FabricSpriteProvider -> LodestoneWorldParticleType.Factory(sprite)}
    }

    fun registerSParticles() {
        Registry.register(Registries.PARTICLE_TYPE, Identifier(StainedLenses.MOD_ID, "menacing_sign"), MENACING_PARTICLE)
    }
}
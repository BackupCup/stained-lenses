package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.World

object RegisterDamageTypes {
    val SUNFIRE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier(StainedLenses.MOD_ID, "sunfire"))

    fun of(world: World, key: RegistryKey<DamageType>): DamageSource {
        return DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(key))
    }
}
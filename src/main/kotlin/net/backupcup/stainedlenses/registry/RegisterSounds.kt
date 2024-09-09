package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

object RegisterSounds {
    private val soundMap: MutableMap<SoundEvent, Identifier> = mutableMapOf()

    private fun registerSound(sound: SoundEvent, id: String): SoundEvent {
        soundMap[sound] = Identifier(StainedLenses.MOD_ID, id)
        return sound
    }

    val SPYGLASS_ZOOM: SoundEvent = registerSound(SoundEvent.of(Identifier(StainedLenses.MOD_ID, "spyglass_zoom")), "spyglass_zoom")
    val SUNFIRE_BEAM_LOOP: SoundEvent = registerSound(SoundEvent.of(Identifier(StainedLenses.MOD_ID, "sunfire_beam_loop")), "sunfire_beam_loop")
    val SUNFIRE_CHARGE_LOOP: SoundEvent = registerSound(SoundEvent.of(Identifier(StainedLenses.MOD_ID, "sunfire_charge_loop")), "sunfire_charge_loop")

    fun registerSounds() {
        soundMap.forEach { (sound: SoundEvent) -> Registry.register(Registries.SOUND_EVENT, sound.id, sound) }
    }
}
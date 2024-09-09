package net.backupcup.stainedlenses.utils

import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.random.Random

class LoopableSoundInstance(
    var player: PlayerEntity,
    sound: SoundEvent,
    category: SoundCategory,
    random: Random
): MovingSoundInstance(sound, category, random) {
    init {
        this.repeat = true
        this.repeatDelay = 0
    }

    override fun tick() {
        if (player.isRemoved) {
            this.setDone()
            return
        }
        this.x = player.x
        this.y = player.y
        this.z = player.z

        this.volume = ((player as FocalLensClient).getCharge().toFloat() / 255f) * 0.75f
    }
}
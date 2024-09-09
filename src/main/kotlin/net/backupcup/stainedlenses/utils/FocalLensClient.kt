package net.backupcup.stainedlenses.utils

interface FocalLensClient {
    fun getHeldCharge(): Int
    fun setHeldCharge(charge: Int)

    fun getCharge(): Int
    fun setCharge(charge: Int)
    fun shouldBeam(): Boolean

    fun getBeamSound(): LoopableSoundInstance
    fun getChargeSound(): LoopableSoundInstance
}
package net.backupcup.stainedlenses.utils

interface ZoomUtil {
    fun setZoom(zoom: Float)
    fun getZoom(): Float
    fun getMaxZoom(): Float
    fun getPlaysoundCooldown(): Long
    fun setPlaysoundCooldown(cooldown: Long)
}
package net.backupcup.stainedlenses.utils

interface FocalLensServer {
    fun shouldBeam(): Boolean
    fun setShouldBeam(shouldBeam: Boolean)
}
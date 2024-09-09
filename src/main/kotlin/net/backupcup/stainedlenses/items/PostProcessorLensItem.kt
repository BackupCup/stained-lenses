package net.backupcup.stainedlenses.items

import net.minecraft.text.Text
import net.minecraft.util.Identifier

class PostProcessorLensItem(
    settings: Settings?,
    GUITexture: Identifier,
    tooltipList: List<Text>,
    zoomLimit: Float,
    override val postProcessor: Identifier
) : LensItem(settings, GUITexture, tooltipList, zoomLimit
), LensPostProcessorUtil
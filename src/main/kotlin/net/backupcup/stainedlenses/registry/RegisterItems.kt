package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.items.*
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector2i

object RegisterItems {
    private val itemMap: MutableMap<Item, Identifier> = mutableMapOf()

    /**
     * Puts the item that needs to be registered, and it's id into a HashMap
     * @param item the Item that needs to be registered
     * @param id the Identifier that the register will use (Includes MOD_ID and requires the id itself)
     * @return the Item itself for putting it into a variable for future usage
     */
    private fun registerItem(item: Item, id: String): Item {
        itemMap[item] = Identifier(StainedLenses.MOD_ID, id)
        return item
    }

    val ATTACHEMENT_POUCH = registerItem(
        PouchItem(
            Item.Settings() .maxCount(1),
            listOf(Text.translatable("item.stained-lenses.attachement_pouch.desc"))),
        "attachement_pouch"
    )

    //LENSES
    val DEFAULT_LENS = registerItem(
        LensItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/default_lens.png"),
            listOf(Text.translatable("item.stained-lenses.default_lens.desc")), 0.2f),
        "default_lens"
    )

    val ENTITY_LENS = registerItem(
        LensItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/entity_lens.png"),
            listOf(Text.translatable("item.stained-lenses.entity_lens.desc")), 0.5f),
        "entity_lens"
    )

    val MARKED_LENS = registerItem(
        LensItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/marked_lens.png"),
            listOf(Text.translatable("item.stained-lenses.marked_lens.desc")), 0.25f),
        "marked_lens"
    )

    val ETHEREAL_LENS = registerItem(
        LensItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/ethereal_lens.png"),
            listOf(Text.translatable("item.stained-lenses.ethereal_lens.desc")), 0.25f),
        "ethereal_lens"
    )

    val FOCAL_LENS = registerItem(
        FocalLensItem(
            Item.Settings() .maxCount(1) .maxDamage(256) .maxDamageIfAbsent(256),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/focal_lens.png"),
            listOf(), 0.1f),
        "focal_lens"
    )

    //POST PROCESSOR LENSES
    val SULFUR_SIGHT_LENS = registerItem(
        PostProcessorLensItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/sulfur_sight_lens.png"),
            listOf(Text.translatable("item.stained-lenses.sulfur_sight_lens.desc")), 0.25f,
            Identifier("shaders/post/creeper.json")),
        "sulfur_sight_lens"
    )
    val PHANTOM_VISION_LENS = registerItem(
        PostProcessorLensItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/phantom_vision_lens.png"),
            listOf(Text.translatable("item.stained-lenses.phantom_vision_lens.desc")), 0.25f,
            Identifier("shaders/post/invert.json")),
        "phantom_vision_lens"
    )
    val SILK_SIGHT_LENS = registerItem(
        PostProcessorLensItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/lenses/silk_sight_lens.png"),
            listOf(Text.translatable("item.stained-lenses.silk_sight_lens.desc")), 0.25f,
            Identifier("shaders/post/spider.json")),
        "silk_sight_lens"
    )

    //MODULES
    val ZOOM_WHEEL = registerItem(
        ModuleItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/modules/zoom_wheel.png"), Vector2i(41, 34),
            listOf(Text.translatable("tooltip.stained-lenses.zoom_wheel.desc"))),
        "zoom_wheel"
    )
    val DISTANCE_MEASURER = registerItem(
        ModuleItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/modules/distance_measurer.png"), Vector2i(54, 56),
            listOf(Text.translatable("tooltip.stained-lenses.distance_measurer.desc"))),
        "distance_measurer"
    )
    val PROXIMITY_SPOTTER = registerItem(
        ModuleItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/modules/proximity_spotter.png"), Vector2i(24, 22),
            listOf(Text.translatable("tooltip.stained-lenses.proximity_spotter.desc"))),
        "proximity_spotter"
    )
    val VIBRATION_SILENCER = registerItem(
        ModuleItem(
            Item.Settings() .maxCount(1),
            Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/modules/vibration_silencer.png"), Vector2i(96, 45),
            listOf(Text.translatable("tooltip.stained-lenses.vibration_silencer.desc"))),
        "vibration_silencer"
    )


    fun registerItems() {
        itemMap.forEach { (item: Item?, identifier: Identifier?) -> Registry.register(Registries.ITEM, identifier, item) }
    }
}
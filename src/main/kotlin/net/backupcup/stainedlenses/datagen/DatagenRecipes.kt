package net.backupcup.stainedlenses.datagen

import net.backupcup.stainedlenses.registry.RegisterBlocks
import net.backupcup.stainedlenses.registry.RegisterItems
import net.backupcup.stainedlenses.registry.RegisterTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.tag.ItemTags
import java.util.function.Consumer

class DatagenRecipes(output: FabricDataOutput?) : FabricRecipeProvider(output) {
    override fun generate(exporter: Consumer<RecipeJsonProvider>?) {
//        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.DEFAULT_LENS)
//            .pattern("").input('', Items.GLASS)
//
//            .criterion(hasItem(Items.GLASS), conditionsFromItem(Items.GLASS))
//            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.ATTACHEMENT_POUCH)
            .pattern("HHH")
            .pattern("ICL")
            .pattern("LLL")

            .input('C', Items.CHEST).input('I', Items.IRON_INGOT)
            .input('L', Items.LEATHER).input('H', Items.RABBIT_HIDE)

            .criterion(hasItem(RegisterItems.ATTACHEMENT_POUCH), conditionsFromItem(RegisterItems.ATTACHEMENT_POUCH))
            .criterion(hasItem(Items.CHEST), conditionsFromItem(Items.CHEST))
            .offerTo(exporter)

        //BLOCKS
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, RegisterBlocks.ETHEREAL_BLOCK, 4)
            .pattern(" P ")
            .pattern("EGE")
            .pattern(" P ")

            .input('G', RegisterTags.C_GLASS)
            .input('E', Items.ECHO_SHARD).input('P', RegisterTags.C_GLASS_PANES)

            .criterion(hasItem(RegisterBlocks.ETHEREAL_BLOCK), conditionsFromItem(RegisterBlocks.ETHEREAL_BLOCK))
            .criterion(hasItem(Items.GLASS), conditionsFromItem(Items.GLASS))
            .criterion(hasItem(Items.ECHO_SHARD), conditionsFromItem(Items.ECHO_SHARD))
            .offerTo(exporter)

        //LENSES
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.DEFAULT_LENS)
            .pattern("PGP")

            .input('G', RegisterTags.C_GLASS).input('P', RegisterTags.C_GLASS_PANES)

            .criterion(hasItem(RegisterItems.DEFAULT_LENS), conditionsFromItem(RegisterItems.DEFAULT_LENS))
            .criterion(hasItem(Items.GLASS), conditionsFromItem(Items.GLASS))
            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.ENTITY_LENS)
            .pattern(" N ")
            .pattern("SLC")
            .pattern(" N ")

            .input('L', RegisterItems.DEFAULT_LENS).input('S', RegisterTags.C_HEADS)
            .input('C', Items.RECOVERY_COMPASS).input('N', Items.ENDER_EYE)

            .criterion(hasItem(RegisterItems.ENTITY_LENS), conditionsFromItem(RegisterItems.ENTITY_LENS))
            .criterion(hasItem(RegisterItems.DEFAULT_LENS), conditionsFromItem(RegisterItems.DEFAULT_LENS))
            .criterion(hasItem(Items.ECHO_SHARD), conditionsFromItem(Items.ECHO_SHARD))
            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.ETHEREAL_LENS)
            .pattern(" B ")
            .pattern("ELE")
            .pattern(" B ")

            .input('L', RegisterItems.DEFAULT_LENS)
            .input('E', Items.ECHO_SHARD).input('B', Items.EXPERIENCE_BOTTLE)

            .criterion(hasItem(RegisterItems.ETHEREAL_LENS), conditionsFromItem(RegisterItems.ETHEREAL_LENS))
            .criterion(hasItem(RegisterItems.DEFAULT_LENS), conditionsFromItem(RegisterItems.DEFAULT_LENS))
            .criterion(hasItem(Items.ECHO_SHARD), conditionsFromItem(Items.ECHO_SHARD))
            .offerTo(exporter)

        //SHADER LENSES
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.SULFUR_SIGHT_LENS)
            .pattern("IIS")
            .pattern("ILI")
            .pattern("SII")

            .input('L', RegisterItems.DEFAULT_LENS)
            .input('S', Items.TNT).input('I', Items.GUNPOWDER)

            .criterion(hasItem(RegisterItems.SULFUR_SIGHT_LENS), conditionsFromItem(RegisterItems.SULFUR_SIGHT_LENS))
            .criterion(hasItem(RegisterItems.DEFAULT_LENS), conditionsFromItem(RegisterItems.DEFAULT_LENS))
            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.PHANTOM_VISION_LENS)
            .pattern("III")
            .pattern("SLS")
            .pattern("III")

            .input('L', RegisterItems.DEFAULT_LENS)
            .input('I', Items.PHANTOM_MEMBRANE).input('S', Items.ENDER_PEARL)

            .criterion(hasItem(RegisterItems.PHANTOM_VISION_LENS), conditionsFromItem(RegisterItems.PHANTOM_VISION_LENS))
            .criterion(hasItem(RegisterItems.DEFAULT_LENS), conditionsFromItem(RegisterItems.DEFAULT_LENS))
            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.SILK_SIGHT_LENS)
            .pattern("ESE")
            .pattern("ELE")
            .pattern("ESE")

            .input('L', RegisterItems.DEFAULT_LENS)
            .input('E', Items.SPIDER_EYE).input('S', Items.STRING)

            .criterion(hasItem(RegisterItems.SILK_SIGHT_LENS), conditionsFromItem(RegisterItems.SILK_SIGHT_LENS))
            .criterion(hasItem(RegisterItems.DEFAULT_LENS), conditionsFromItem(RegisterItems.DEFAULT_LENS))
            .offerTo(exporter)

        //MODULES
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.ZOOM_WHEEL)
            .pattern("NIN")
            .pattern("CCC")

            .input('C', Items.COPPER_INGOT)
            .input('I', Items.IRON_INGOT).input('N', Items.IRON_NUGGET)

            .criterion(hasItem(RegisterItems.ZOOM_WHEEL), conditionsFromItem(RegisterItems.ZOOM_WHEEL))
            .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.DISTANCE_MEASURER)
            .pattern("RCC")
            .pattern("ORC")
            .pattern("RCC")

            .input('C', Items.COPPER_INGOT)
            .input('O', Items.OBSERVER).input('R', Items.REDSTONE)

            .criterion(hasItem(RegisterItems.DISTANCE_MEASURER), conditionsFromItem(RegisterItems.DISTANCE_MEASURER))
            .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.PROXIMITY_SPOTTER)
            .pattern("ISI")
            .pattern(" R ")
            .pattern("CRC")

            .input('C', Items.COPPER_INGOT)
            .input('I', Items.IRON_INGOT).input('S', Items.SCULK_SENSOR).input('R', Items.REDSTONE)

            .criterion(hasItem(RegisterItems.PROXIMITY_SPOTTER), conditionsFromItem(RegisterItems.PROXIMITY_SPOTTER))
            .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
            .offerTo(exporter)

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, RegisterItems.VIBRATION_SILENCER)
            .pattern("WSW")
            .pattern("CCC")

            .input('C', Items.COPPER_INGOT).input('S', Items.SCULK_SENSOR).input('W', ItemTags.WOOL)

            .criterion(hasItem(RegisterItems.VIBRATION_SILENCER), conditionsFromItem(RegisterItems.VIBRATION_SILENCER))
            .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
            .offerTo(exporter)
    }
}
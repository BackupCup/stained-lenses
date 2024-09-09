package net.backupcup.stainedlenses.mixin.client;

import net.backupcup.stainedlenses.items.LensItem;
import net.backupcup.stainedlenses.items.LensPostProcessorUtil;
import net.backupcup.stainedlenses.utils.DataHelper;
import net.backupcup.stainedlenses.utils.PostProcessorUtil;
import net.backupcup.stainedlenses.utils.ZoomUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements PostProcessorUtil {
    @Unique boolean hasPostProcessor = false;
    @Override
    public boolean getPostFlag() {return this.hasPostProcessor;}
    @Override
    public void setPostFlag(boolean flag) {this.hasPostProcessor = flag;}

    @Shadow abstract void loadPostProcessor(Identifier id);

    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void stainedLenses$modifySpyglassFOV(CallbackInfoReturnable<Double> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player != null) {
            ItemStack itemStack = player.getActiveItem();

            if (DataHelper.INSTANCE.getLensStack(itemStack).isEmpty() && player.isUsingSpyglass()) {
                cir.setReturnValue((double) client.options.getFov().getValue());
            }

            if (DataHelper.INSTANCE.getLensStack(itemStack).getItem() instanceof LensItem) {
                if(player.isUsingSpyglass() &&
                        ((ZoomUtil)player).getZoom() < ((LensItem)DataHelper.INSTANCE.getLensStack(itemStack).getItem()).getZoomLimit()) {
                    ((ZoomUtil)player).setZoom(((LensItem)DataHelper.INSTANCE.getLensStack(itemStack).getItem()).getZoomLimit());
                }
            }

            if (DataHelper.INSTANCE.getLensStack(itemStack).getItem() instanceof LensPostProcessorUtil && player.isUsingSpyglass() &&
                    client.options.getPerspective().isFirstPerson()) {
                Identifier postProcessorIdentifier = ((LensPostProcessorUtil)DataHelper.INSTANCE.getLensStack(itemStack).getItem()).getPostProcessor();

                if (!this.hasPostProcessor) {
                    this.hasPostProcessor = true;
                    loadPostProcessor(postProcessorIdentifier);
                }
            }
        }
    }
}


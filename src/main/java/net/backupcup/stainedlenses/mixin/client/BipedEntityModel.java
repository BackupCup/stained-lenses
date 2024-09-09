package net.backupcup.stainedlenses.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(net.minecraft.client.render.entity.model.BipedEntityModel.class)
public class BipedEntityModel {
    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart head;

    @ModifyArg(method = "positionLeftArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"), index = 1)
    private float stainedLenses$stopSpyglassRestrictLeft(float value) {
        BipedEntityModel model = (BipedEntityModel)(Object)this;

        if (model.head.pitch < 0) model.leftArm.roll = model.head.pitch * 0.25f;
        return -3.5f;
    }

    @ModifyArg(method = "positionRightArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"), index = 1)
    private float stainedLenses$stopSpyglassRestrictRight(float value) {
        BipedEntityModel model = (BipedEntityModel)(Object)this;

        if (model.head.pitch < 0) model.rightArm.roll = -model.head.pitch * 0.25f;
        return -3.5f;
    }
}

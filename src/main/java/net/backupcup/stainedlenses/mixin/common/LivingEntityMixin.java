package net.backupcup.stainedlenses.mixin.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.backupcup.stainedlenses.registry.RegisterItems;
import net.backupcup.stainedlenses.utils.DataHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow protected ItemStack activeItemStack;
//
    @WrapWithCondition(method = "clearActiveItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/world/event/GameEvent;)V"))
    private boolean stainedLenses$onClearActiveItem(LivingEntity instance, GameEvent gameEvent) {
        if (activeItemStack.isOf(Items.SPYGLASS)) return !DataHelper.INSTANCE.getModuleStack(activeItemStack).isOf(RegisterItems.INSTANCE.getVIBRATION_SILENCER());
        return true;
    }

    @WrapWithCondition(method = "setCurrentHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/world/event/GameEvent;)V"))
    private boolean stainedLenses$onSetCurrentHand(LivingEntity instance, GameEvent gameEvent) {
        if (activeItemStack.isOf(Items.SPYGLASS)) return !DataHelper.INSTANCE.getModuleStack(activeItemStack).isOf(RegisterItems.INSTANCE.getVIBRATION_SILENCER());
        return true;
    }
}

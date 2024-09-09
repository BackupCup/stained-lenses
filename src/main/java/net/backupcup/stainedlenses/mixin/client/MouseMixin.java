package net.backupcup.stainedlenses.mixin.client;

import net.backupcup.stainedlenses.registry.RegisterItems;
import net.backupcup.stainedlenses.registry.RegisterSounds;
import net.backupcup.stainedlenses.utils.DataHelper;
import net.backupcup.stainedlenses.utils.ZoomUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;

    @Unique private ItemStack spyglassStack;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void stainedLenses$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;

            if (player == null) return;
            if (player.isUsingSpyglass()) {
                if (DataHelper.INSTANCE.getModuleStack(player.getActiveItem()).isOf(RegisterItems.INSTANCE.getZOOM_WHEEL())) {
                    ZoomUtil zoomPlayer = (ZoomUtil)player;

                    if (vertical > 0 && zoomPlayer.getZoom() > zoomPlayer.getMaxZoom()) {
                        float newZoom = Math.max(zoomPlayer.getMaxZoom(), Math.min(1f, zoomPlayer.getZoom() - 0.025f));
                        zoomPlayer.setZoom(newZoom);

                        if (System.currentTimeMillis() - zoomPlayer.getPlaysoundCooldown() > 100) {
                            player.playSound(
                                    RegisterSounds.INSTANCE.getSPYGLASS_ZOOM(), SoundCategory.PLAYERS,
                                    0.25f, zoomPlayer.getZoom() + 0.5f);
                            zoomPlayer.setPlaysoundCooldown(System.currentTimeMillis());
                        }

                    } else if (vertical < 0 && zoomPlayer.getZoom() < 1f) {
                        float newZoom = Math.max(zoomPlayer.getMaxZoom(), Math.min(1f, zoomPlayer.getZoom() + 0.025f));
                        zoomPlayer.setZoom(newZoom);

                        if (System.currentTimeMillis() - zoomPlayer.getPlaysoundCooldown() > 100) {
                            player.playSound(
                                    RegisterSounds.INSTANCE.getSPYGLASS_ZOOM(), SoundCategory.PLAYERS,
                                    0.25f, zoomPlayer.getZoom() + 0.5f);
                            zoomPlayer.setPlaysoundCooldown(System.currentTimeMillis());
                        }
                    }

                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V", shift = At.Shift.BEFORE))
    private void stainedLenses$storeSpyglass(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (client.player.isUsingSpyglass()) {
            this.spyglassStack = client.player.getActiveItem();
        }
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V", shift = At.Shift.AFTER))
    private void stainedLenses$stopSpyglass(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (this.spyglassStack == null) return;
        if (this.spyglassStack.isOf(Items.SPYGLASS)) {
            this.spyglassStack.onStoppedUsing(client.player.getEntityWorld(), client.player, client.player.getItemUseTimeLeft());
            client.player.clearActiveItem();
            this.spyglassStack = null;
        }
    }
}
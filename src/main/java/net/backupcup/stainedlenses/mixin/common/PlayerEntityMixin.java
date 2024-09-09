package net.backupcup.stainedlenses.mixin.common;

import net.backupcup.stainedlenses.utils.FocalLensServer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements FocalLensServer {
    boolean shouldBeam = false;
    @Override public void setShouldBeam(boolean shouldBeam) { this.shouldBeam = shouldBeam; }
    @Override public boolean shouldBeam() { return this.shouldBeam; }
}

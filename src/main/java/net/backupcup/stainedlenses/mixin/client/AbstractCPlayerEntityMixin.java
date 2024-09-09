package net.backupcup.stainedlenses.mixin.client;

import net.backupcup.stainedlenses.StainedLenses;
import net.backupcup.stainedlenses.items.LensItem;
import net.backupcup.stainedlenses.registry.RegisterItems;
import net.backupcup.stainedlenses.registry.RegisterSounds;
import net.backupcup.stainedlenses.utils.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayerEntity.class)
public class AbstractCPlayerEntityMixin implements ZoomUtil, EntityListUtil, FocalLensClient {

    @Unique List<LivingEntity> visibleEntityList;
    @Override
    public List<LivingEntity> getList() { return this.visibleEntityList; }
    @Override
    public void setList(List<LivingEntity> list) { this.visibleEntityList = list; }

    @Unique float currentZoom = 0f;
    @Override
    public void setZoom(float zoom) {this.currentZoom = zoom;}
    @Override
    public float getZoom() {return this.currentZoom;}

    @Unique Pair<Float, Float> zoomBorder = new Pair<>(1f, 1f);
    @Override
    public float getMaxZoom() {return this.zoomBorder.getLeft();}

    @Unique long playsoundCooldown = 0;
    @Override
    public long getPlaysoundCooldown() {return this.playsoundCooldown;}
    @Override
    public void setPlaysoundCooldown(long cooldown) {this.playsoundCooldown = cooldown;}

    @Unique int charge = 0;
    @Unique int heldCharge = 0;
    @Override public int getCharge() { return this.charge; }
    @Override public void setCharge(int charge) { this.charge = charge; }
    @Override public int getHeldCharge() { return this.heldCharge; }
    @Override public void setHeldCharge(int charge) { this.heldCharge = charge; }

    @Unique boolean isBeaming = false;
    @Override public boolean shouldBeam() { return this.isBeaming; }

    @Unique
    LoopableSoundInstance beamSound = new LoopableSoundInstance((PlayerEntity)(Object)this,
            RegisterSounds.INSTANCE.getSUNFIRE_BEAM_LOOP(), SoundCategory.PLAYERS, Random.createThreadSafe());
    @Override public LoopableSoundInstance getBeamSound() { return this.beamSound; }

    @Unique
    LoopableSoundInstance chargeupSound = new LoopableSoundInstance((PlayerEntity)(Object)this,
            RegisterSounds.INSTANCE.getSUNFIRE_CHARGE_LOOP(), SoundCategory.PLAYERS, Random.createThreadSafe());
    @Override public LoopableSoundInstance getChargeSound() { return this.chargeupSound; }

    @Unique
    private static Pair<List<LivingEntity>, Vec3d> raycastLivingEntities(World world, Vec3d startPos, Vec3d direction, double maxDistance) {
        List<LivingEntity> intersectedEntities = new ArrayList<>();

        Vec3d endPos = startPos.add(direction.multiply(maxDistance));
        RaycastContext context = new RaycastContext(
                startPos,
                endPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                null
        );
        BlockHitResult blockHitResult = world.raycast(context);

        if (blockHitResult.getType() != HitResult.Type.MISS) {
            endPos = blockHitResult.getPos();
        }

        Box boundingBox = new Box(startPos, endPos).expand(2.0);
        List<Entity> entities = world.getOtherEntities(null, boundingBox);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && isEntityIntersectedByRay(entity, startPos, endPos)) {
                intersectedEntities.add(livingEntity);
            }
        }

        return new Pair<>(intersectedEntities, endPos);
    }

    @Unique
    private static boolean isEntityIntersectedByRay(Entity entity, Vec3d startPos, Vec3d endPos) {
        Box boundingBox = entity.getBoundingBox();
        return boundingBox.raycast(startPos, endPos).isPresent();
    }

    @Inject(method = "getFovMultiplier", at = @At("HEAD"), cancellable = true)
    private void stainedLenses$changeFOVMultiplier(CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack itemStack = player.getActiveItem();

        if (!player.isUsingSpyglass()) return;

        if (DataHelper.INSTANCE.getLensStack(itemStack).isEmpty()) cir.setReturnValue(1f);

        float savedZoom = DataHelper.INSTANCE.getLensStack(itemStack).getOrCreateNbt().getFloat("savedZoom");

       this.visibleEntityList = SpyglassUtils.INSTANCE.getVisibleEntitiesThroughSpyglass(player);

        if (!DataHelper.INSTANCE.getLensStack(itemStack).isEmpty()) {
            float lensZoom = DataHelper.INSTANCE.getLensStack(itemStack).getItem() instanceof LensItem ?
                    ((LensItem) DataHelper.INSTANCE.getLensStack(itemStack).getItem()).getZoomLimit() : 1f;

            if (this.zoomBorder != null) this.zoomBorder.setLeft(lensZoom);
                                    else this.zoomBorder = new Pair<>(lensZoom, 1f);

            if (this.currentZoom == 0f) this.currentZoom = savedZoom;
            cir.setReturnValue(this.currentZoom);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void stainedLenses$focalLensTick(CallbackInfo ci) {


        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (!player.isUsingSpyglass()) {
            if (this.beamSound != null) beamSound = null;
            if (this.chargeupSound != null) chargeupSound = null;

            return;
        } else {
            if (this.beamSound == null) beamSound = new LoopableSoundInstance((PlayerEntity)(Object)this,
                        RegisterSounds.INSTANCE.getSUNFIRE_BEAM_LOOP(), SoundCategory.PLAYERS, Random.createThreadSafe());
            if (this.chargeupSound == null) chargeupSound = new LoopableSoundInstance((PlayerEntity)(Object)this,
                        RegisterSounds.INSTANCE.getSUNFIRE_CHARGE_LOOP(), SoundCategory.PLAYERS, Random.createThreadSafe());
        }

        ItemStack lensStack = DataHelper.INSTANCE.getLensStack(player.getStackInHand(player.getActiveHand()));
        if (!lensStack.isOf(RegisterItems.INSTANCE.getFOCAL_LENS())) return;

        if (MinecraftClient.getInstance().options.attackKey.isPressed() &&
                !MinecraftClient.getInstance().isPaused()) {
            if (this.charge < (lensStack.getMaxDamage()-lensStack.getDamage()-this.heldCharge)) this.charge =
                    Math.min(this.charge+2, lensStack.getMaxDamage()-lensStack.getDamage());

            if (!MinecraftClient.getInstance().getSoundManager().isPlaying(this.chargeupSound)) {
                MinecraftClient.getInstance().getSoundManager().play(this.chargeupSound);
            }

            if (this.isBeaming) {
                this.isBeaming = false;

                if (MinecraftClient.getInstance().getSoundManager().isPlaying(this.beamSound)) {
                    MinecraftClient.getInstance().getSoundManager().stop(this.beamSound);
                }

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(this.isBeaming);
                buf.writeInt(0);
                ClientPlayNetworking.send(new Identifier(StainedLenses.MOD_ID, "focal_lens_update"), buf);
            }
        }

        if (!MinecraftClient.getInstance().mouse.wasLeftButtonClicked() &&
                ((FocalLensClient)player).getCharge() > 0 && !MinecraftClient.getInstance().isPaused()) {
            this.charge--;
            this.heldCharge++;

            renderParticles(player, lensStack, Random.createThreadSafe());

            if (!this.isBeaming) {
                this.isBeaming = true;

                MinecraftClient.getInstance().getSoundManager().play(this.beamSound);
                if (MinecraftClient.getInstance().getSoundManager().isPlaying(this.chargeupSound)) {
                    MinecraftClient.getInstance().getSoundManager().stop(this.chargeupSound);
                }

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(this.isBeaming);
                ClientPlayNetworking.send(new Identifier(StainedLenses.MOD_ID, "focal_lens_update"), buf);
            }
        }

        if ((this.charge == 0 || this.heldCharge == 0) && this.isBeaming) {
            this.isBeaming = false;

            if (MinecraftClient.getInstance().getSoundManager().isPlaying(this.beamSound)) {
                MinecraftClient.getInstance().getSoundManager().stop(this.beamSound);
            }
            
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(this.isBeaming);
            buf.writeInt(this.heldCharge);
            ClientPlayNetworking.send(new Identifier(StainedLenses.MOD_ID, "focal_lens_update"), buf);
            this.heldCharge = 0;
        }
    }

    @Unique
    private void renderParticles(PlayerEntity entity, ItemStack lensStack, Random random) {
        Vec3d startPos = entity.getEyePos();
        Vec3d direction = entity.getRotationVec(1.0F);
        double maxDistance = (lensStack.getMaxDamage()-lensStack.getDamage()) / 4f;

        Pair<List<LivingEntity>, Vec3d> raycastPair = raycastLivingEntities(entity.getEntityWorld(), startPos, direction, maxDistance);
        Vec3d endPos = raycastPair.getRight();

        Pair<Color, Color> colorPair = new Pair<>(new Color(0xFFE0BC), new Color(0xE6904E));

        raycastPair.getLeft().forEach(livingEntity -> {
            Vec3d entityBoxLength = new Vec3d(
                    livingEntity.getBoundingBox(livingEntity.getPose()).getXLength()/2,
                    livingEntity.getBoundingBox(livingEntity.getPose()).getYLength()/2,
                    livingEntity.getBoundingBox(livingEntity.getPose()).getZLength()/2
            );
            Vec3d particlePos = new Vec3d(
                    livingEntity.getPos().x + entityBoxLength.x,
                    livingEntity.getPos().y + entityBoxLength.y,
                    livingEntity.getPos().z + entityBoxLength.z
            );

            WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
                    .setScaleData(GenericParticleData.create(0.25f, 0).build())
                    .setTransparencyData(GenericParticleData.create(0.25f, 0f).build())
                    .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build())
                    .setSpinData(SpinParticleData.create(0.2f, 0.4f).setSpinOffset((entity.getEntityWorld().getTime() * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build())
                    .setLifetime(20)
                    .setRandomOffset(entityBoxLength.x *1.5, entityBoxLength.y *1.5, entityBoxLength.z *1.5)
                    .addMotion(0, 0.01f, 0)
                    .enableNoClip()
                    .spawn(livingEntity.getEntityWorld(), particlePos.x, particlePos.y, particlePos.z);
        });

        Vec3d randomPos = startPos.add(endPos.subtract(startPos).multiply(random.nextFloat()));
        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
                .setScaleData(GenericParticleData.create(0.25f, 0).build())
                .setTransparencyData(GenericParticleData.create(0.5f, 0f).build())
                .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build())
                .setSpinData(SpinParticleData.create(0.2f, 0.4f).setSpinOffset((entity.getEntityWorld().getTime() * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build())
                .setLifetime(20)
                .setRandomOffset(0.5f)
                .addMotion(0, 0.01f, 0)
                .enableNoClip()
                .spawn(entity.getEntityWorld(), randomPos.x, randomPos.y, randomPos.z);

        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
                .setScaleData(GenericParticleData.create(0.25f, 0).build())
                .setTransparencyData(GenericParticleData.create(0.25f, 0f).build())
                .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build())
                .setSpinData(SpinParticleData.create(0.2f, 0.4f).setSpinOffset((entity.getEntityWorld().getTime() * 0.2f) % 6.28f).setEasing(Easing.QUARTIC_IN).build())
                .setLifetime(20)
                .setRandomOffset(1.5f)
                .addMotion(0, 0.01f, 0)
                .enableNoClip()
                .spawn(entity.getEntityWorld(), endPos.x, endPos.y, endPos.z);
    }
}

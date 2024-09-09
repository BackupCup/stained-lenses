package net.backupcup.stainedlenses.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.backupcup.stainedlenses.StainedLenses;
import net.backupcup.stainedlenses.registry.RegisterItems;
import net.backupcup.stainedlenses.registry.RegisterParticles;
import net.backupcup.stainedlenses.utils.DataHelper;
import net.backupcup.stainedlenses.utils.FocalLensClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.RenderHandler;
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.particle.world.type.LodestoneWorldParticleType;
import team.lodestar.lodestone.systems.rendering.LodestoneRenderType;
import team.lodestar.lodestone.systems.rendering.VFXBuilders;
import team.lodestar.lodestone.systems.rendering.rendeertype.RenderTypeToken;

import java.awt.*;

@Environment(EnvType.CLIENT)
@Mixin(PlayerHeldItemFeatureRenderer.class)
public class PlayerHeldItemFeatureRendererMixin {
    @Unique private static RenderTypeToken getRenderTypeToken(String path) {return RenderTypeToken.createToken(new Identifier(StainedLenses.MOD_ID, path));}
    @Unique private static Vector3f createVec3f(float x, float y, float z) {return new Vector3f(x, y, z);}

    @Unique
    private static final LodestoneRenderType TRAIL_UP = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(
            getRenderTypeToken("textures/vfx/concentrated_trail_up.png"));
    @Unique
    private static final LodestoneRenderType TRAIL_DOWN = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(
            getRenderTypeToken("textures/vfx/concentrated_trail_down.png"));
    @Unique
    private static final LodestoneRenderType TRAIL_LEFT = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(
            getRenderTypeToken("textures/vfx/concentrated_trail_left.png"));
    @Unique
    private static final LodestoneRenderType TRAIL_RIGHT = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(
            getRenderTypeToken("textures/vfx/concentrated_trail_right.png"));
    @Unique
    private static final LodestoneRenderType CIRCLE = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(
            getRenderTypeToken("textures/vfx/light_circle.png"));

    @WrapOperation(method = "renderSpyglass", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"))
    private float stainedLenses$spyglassPitchUnlock(float value, float min, float max, Operation<Float> original, @Local float f) {
        return f;
    }

    @Inject(method = "renderSpyglass",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void stainedLenses$renderBeam(LivingEntity entity, ItemStack stack, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ItemStack lensItem = DataHelper.INSTANCE.getLensStack(stack);

        if (!lensItem.isOf(RegisterItems.INSTANCE.getFOCAL_LENS())) return;
        float lensDurability = ((FocalLensClient)entity).getCharge();

        float size = 1f;

        float height = 0.125f;
        float width = 0.125f;
        float startingLength = 1.5f;

        Vec3d start = entity.getCameraPosVec(1.0F);
        Vec3d direction = entity.getRotationVec(1.0F).normalize();
        Vec3d end = start.add(direction.multiply(lensDurability / 4f + startingLength));
        HitResult blockHitResult = entity.getWorld().raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));

        float length = lensDurability / 4f + startingLength;
        if (blockHitResult.getType() == HitResult.Type.BLOCK) length = Math.min(length, (float) entity.getPos().distanceTo(blockHitResult.getPos()));

        Vector3f[] topFace = new Vector3f[]{
                createVec3f(-width, height, startingLength), createVec3f(width, height, startingLength),
                createVec3f(width, height, length), createVec3f(-width, height, length)};

        Vector3f[] bottomFace = new Vector3f[]{
                createVec3f(-width, -height, length), createVec3f(width, -height, length),
                createVec3f(width, -height, startingLength), createVec3f(-width, -height, startingLength)};

        Vector3f[] leftFace = new Vector3f[]{
                createVec3f(-width, height, startingLength), createVec3f(-width, height, length),
                createVec3f(-width, -height, length), createVec3f(-width, -height, startingLength)};

        Vector3f[] rightFace = new Vector3f[]{
                createVec3f(width, height, length), createVec3f(width, height, startingLength),
                createVec3f(width, -height, startingLength), createVec3f(width, -height, length)};

        matrices.push();

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

            VFXBuilders.WorldVFXBuilder builder = VFXBuilders.createWorld();
            builder.replaceBufferSource(RenderHandler.LATE_DELAYED_RENDER.getTarget())
                    .setRenderType(TRAIL_UP)
                    .setColor(new Color(lensItem.getItemBarColor()))
                    .setAlpha((lensDurability - 1) / 385f);

            if (((FocalLensClient)entity).shouldBeam()) {
                builder.renderQuad(matrices, topFace,    size);

                builder.setRenderType(TRAIL_DOWN);
                builder.renderQuad(matrices, bottomFace, size);

                builder.setRenderType(TRAIL_RIGHT);
                builder.renderQuad(matrices, leftFace,   size);

                builder.setRenderType(TRAIL_LEFT);
                builder.renderQuad(matrices, rightFace,  size);
            }

            for (float i = 1f; i < 3; i += 0.5f) {
                renderCircle(builder, matrices, (lensDurability - 1) / 255f / i, lensItem.getItemBarColor(), entity.getWorld().getTime() * i, width, height, i);
            }

            Pair<Color, Color> menacingColor = new Pair<>(new Color(0xa884f3), new Color(0x45293f));

            if (entity.getEntityWorld().getTime() % 2 == 0 && Random.createThreadSafe().nextBetween(0, 33) == 33) {
                WorldParticleBuilder.create(RegisterParticles.INSTANCE.getMENACING_PARTICLE())
                        .setScaleData(GenericParticleData.create(0.5f, 0).build())
                        .setSpinData(SpinParticleData.createRandomDirection(Random.createThreadSafe(), 0.0125f).build())
                        .setTransparencyData(GenericParticleData.create(1f, 0.25f).build())
                        .setColorData(ColorParticleData.create(menacingColor.getLeft(), menacingColor.getRight()).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build())
                        .setLifetime(40)
                        .addMotion(0 + Random.createThreadSafe().nextBetween(-5, 5) / 200f, 0.0625f, 0 + Random.createThreadSafe().nextBetween(-5, 5) / 200f)
                        .enableNoClip()
                        .spawn(entity.getEntityWorld(),
                                entity.getPos().x + Random.createThreadSafe().nextBetween(-15, 15) / 15f,
                                entity.getBoundingBox().getYLength()/2f + entity.getPos().y  + Random.createThreadSafe().nextBetween(-(int)entity.getBoundingBox().getYLength()*5, 25) / 15f,
                                entity.getPos().z + Random.createThreadSafe().nextBetween(-15, 15) / 15f);
            }

        matrices.pop();
    }

    private void renderCircle(
            VFXBuilders.WorldVFXBuilder builder, MatrixStack matrices,
            float alpha, int color, float rotationOffset,
            float width, float height, float circleSizeModifier) {
        Vector3f[] backFace = new Vector3f[]{
                createVec3f(-width*3f, -height*3f, 0),
                createVec3f(width*3f, -height*3f, 0),
                createVec3f(width*3f, height*3f, 0),
                createVec3f(-width*3f, height*3f, 0)};

        Vector3f[] frontFace = new Vector3f[]{
                createVec3f(-width*3f, height*3f, 0),
                createVec3f(width*3f, height*3f, 0),
                createVec3f(width*3f, -height*3f, 0),
                createVec3f(-width*3f, -height*3f, 0)};

        builder.setAlpha(alpha);
        builder.setColor(new Color(color));
        builder.setRenderType(CIRCLE);

        matrices.push();
            matrices.translate(0, 0, 1.25f * circleSizeModifier);
            matrices.scale(1/circleSizeModifier, 1/circleSizeModifier, 1/circleSizeModifier);

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffset));
            builder.renderQuad(matrices, backFace, 3f);

            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(rotationOffset * 2));
            builder.renderQuad(matrices, frontFace, 3f);
        matrices.pop();
    }
}

package net.backupcup.stainedlenses.mixin.client;

import net.backupcup.stainedlenses.StainedLenses;
import net.backupcup.stainedlenses.registry.RegisterItems;
import net.backupcup.stainedlenses.utils.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.screenparticle.ScreenParticleHandler;
import team.lodestar.lodestone.registry.common.particle.LodestoneScreenParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.ScreenParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleDataBuilder;
import team.lodestar.lodestone.systems.particle.screen.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    ScreenParticleHolder screenDetectorHolder = new ScreenParticleHolder();
    @Unique
    ScreenParticleHolder screenChargeHolder = new ScreenParticleHolder();

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Shadow private int scaledWidth;

    @Shadow private int scaledHeight;

    @Unique
    private float getRotationAngleToEntity(PlayerEntity player, Entity entity) {
        Vec3d direction = player.getRotationVec(1.0F);

        Vec3d entityPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
        Vec3d playerPos = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d playerToEntity = entityPos.subtract(playerPos).normalize();

        double angle = Math.atan2(playerToEntity.z, playerToEntity.x) - Math.atan2(direction.z, direction.x);
        angle = Math.toDegrees(angle);
        if (angle < 0) angle += 360.0;
        else if (angle >= 360.0) angle -= 360.0;

        return (float) angle;
    }

    @Unique
    private List<LivingEntity> getNearbyLivingEntities(World world, PlayerEntity player, double range) {
        List<LivingEntity> nearbyEntities = new ArrayList<>();
        Box box = new Box(
                player.getX() - range, player.getY() - range, player.getZ() - range,
                player.getX() + range, player.getY() + range, player.getZ() + range
        );

        List<Entity> entities = world.getOtherEntities(player, box);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) nearbyEntities.add((LivingEntity) entity);
        }

        return nearbyEntities;
    }

    //3RAYCASTING FOR DISTANCE MEASUREMENTS
    @Unique
    private static HitResult raycast(World world, PlayerEntity player, double maxDistance) {
        Vec3d start = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F).normalize();
        Vec3d end = start.add(direction.multiply(maxDistance));

        HitResult blockHitResult = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        EntityHitResult entityHitResult = raycastEntities(world, player, start, end);

        if (entityHitResult != null && (blockHitResult == null || start.distanceTo(entityHitResult.getPos()) < start.distanceTo(blockHitResult.getPos())))
            return entityHitResult;

        return blockHitResult;
    }

    @Unique
    private static EntityHitResult raycastEntities(World world, PlayerEntity player, Vec3d start, Vec3d end) {
        EntityHitResult closestEntityHitResult = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : world.getOtherEntities(player, new Box(start, end).expand(1.0))) {
            if (entity instanceof LivingEntity) {
                Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
                Optional<Vec3d> optional = entityBox.raycast(start, end);
                if (optional.isPresent()) {
                    double distance = start.distanceTo(optional.get());
                    if (distance < closestDistance) {
                        closestEntityHitResult = new EntityHitResult(entity, optional.get());
                        closestDistance = distance;
                    }
                }
            }
        }

        return closestEntityHitResult;
    }

    @Unique
    private static float handleRaycastResult(HitResult hitResult, PlayerEntity player) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;

            return (float) player.getPos().distanceTo(entityHitResult.getPos());
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;

            return (float) player.getPos().distanceTo(Vec3d.ofCenter(blockHitResult.getBlockPos()));
        } else return -1f;
    }

    @ModifyArg(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIFFIIII)V", ordinal = 0))
    private Identifier stainedLenses$renderSpyglassOverlay(Identifier id) {
        if (DataHelper.INSTANCE.getLensStack(client.player.getActiveItem()).isEmpty() && client.player.isUsingSpyglass())
            return new Identifier(StainedLenses.MOD_ID, "textures/gui/spyglass_scope_no_lens.png");
        return id;
    }

    @Inject(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(Lnet/minecraft/client/render/RenderLayer;IIIIII)V", ordinal = 3, shift = At.Shift.AFTER))
    private void stainedLenses$tooltipInformation(DrawContext context, float scale, CallbackInfo ci) {
        ItemStack stack = client.player.getActiveItem();

        ItemStack lensStack = DataHelper.INSTANCE.getLensStack(stack);
        ArrayList<Text> lensTranslation = new ArrayList<>();
        lensTranslation.add(Text.translatable("tooltip.stained-lenses.installed_lens"));
        lensTranslation.add(Text.translatable("tooltip.stained-lenses.display_line",
                Text.translatable(!lensStack.isEmpty() ? lensStack.getTranslationKey() : "tooltip.stained-lenses.none")).formatted(Formatting.BLUE));

        ItemStack moduleStack = DataHelper.INSTANCE.getModuleStack(stack);
        ArrayList<Text> additionalTranslation = new ArrayList<>();
        additionalTranslation.add(Text.translatable("tooltip.stained-lenses.installed_module"));
        additionalTranslation.add(Text.translatable("tooltip.stained-lenses.display_line",
                Text.translatable(!moduleStack.isEmpty() ? moduleStack.getTranslationKey() : "tooltip.stained-lenses.none")).formatted(Formatting.GOLD));

        if (moduleStack.getItem() == RegisterItems.INSTANCE.getDISTANCE_MEASURER() && client.world != null) {
            double maxDistance = MinecraftClient.getInstance().options.getViewDistance().getValue() * 16;
            HitResult hitResult = raycast(client.world, MinecraftClient.getInstance().player, maxDistance);

            float measuredDistance = Math.round(10f * handleRaycastResult(hitResult, MinecraftClient.getInstance().player)) / 10f;
            context.drawTooltip(getTextRenderer(),
                    Text.translatable("tooltip.stained-lenses.distance",
                            measuredDistance == -1f ? Text.translatable("tooltip.stained-lenses.infinite").getString() : measuredDistance),
                    2, context.getScaledWindowHeight() - 8 - 28*3 - 13);
        }

        context.drawTooltip(getTextRenderer(),
                Text.translatable("tooltip.stained-lenses.zoom", (float)Math.round(10 / client.player.getFovMultiplier()) / 10),
                2, context.getScaledWindowHeight() - 8 - 28*2 - 13);
        context.drawTooltip(getTextRenderer(),
                lensTranslation,
                2, context.getScaledWindowHeight() - 8 - 28*2 + 1);
        context.drawTooltip(getTextRenderer(),
                additionalTranslation,
                2, context.getScaledWindowHeight() - 36);
    }

    @Inject(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIFFIIII)V", shift = At.Shift.AFTER))
    private void stainedLenses$drawProximityOverlay(DrawContext context, float scale, CallbackInfo ci) {
        if (client.world == null || client.player == null) return;
        if (!DataHelper.INSTANCE.getModuleStack(client.player.getActiveItem()).isOf(RegisterItems.INSTANCE.getPROXIMITY_SPOTTER())) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int textureWidth = 32;
        int textureHeight = 32;
        int textureX = textureWidth / 2;
        int textureY = textureHeight / 2;

        MatrixStack matrices = context.getMatrices();

        getNearbyLivingEntities(client.world, client.player, 16.0).forEach(livingEntity -> {
            matrices.push();
                matrices.translate(screenWidth / 2f, screenHeight / 2f, 0f);

                float angle = getRotationAngleToEntity(client.player, livingEntity);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));

                matrices.push();
                    matrices.translate(-textureX, -(textureY+screenHeight / 3f), 0f);

                    double distance = client.player.distanceTo(livingEntity);
                    Map<Pair<Integer, Integer>, String> ranges = new HashMap<>();
                        ranges.put(new Pair<>(12, 16), "proximity_marker_3.png");
                        ranges.put(new Pair<>(8, 12), "proximity_marker_2.png");
                        ranges.put(new Pair<>(4, 8), "proximity_marker_1.png");
                        ranges.put(new Pair<>(0, 4), "proximity_marker_0.png");

                    AtomicReference<String> fileName = new AtomicReference<>("proximity_marker_3.png");
                    ranges.forEach((range, string) -> {
                        if (distance <= range.getRight() && distance >= range.getLeft()) {
                            fileName.set(string);
                        }
                    });
                    Identifier TEXTURE = new Identifier(StainedLenses.MOD_ID, "textures/gui/additionals/modules/proximity_markers/" + fileName);

                    context.drawTexture(TEXTURE, 0, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
                matrices.pop();

            matrices.pop();
        });
    }

    @Inject(method = "renderSpyglassOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIFFIIII)V", shift = At.Shift.AFTER))
    private void stainedLenses$renderDetectorParticles(DrawContext context, float scale, CallbackInfo ci) {
        if (client.world == null || client.player == null) return;
        ItemStack lensStack = DataHelper.INSTANCE.getLensStack(client.player.getActiveItem());

        if (!lensStack.isOf(RegisterItems.INSTANCE.getENTITY_LENS())) return;

        float gameTime = client.world.getTime();

        final SpinParticleDataBuilder spinDataBuilder = SpinParticleData.create(0, 1).setSpinOffset(0.025f * gameTime % 6.28f).setEasing(Easing.EXPO_IN_OUT);

        if (ScreenParticleHandler.canSpawnParticles) screenDetectorHolder.tick();
        ScreenParticleHandler.renderParticles(screenDetectorHolder);

        if (MinecraftClient.getInstance().isPaused()) return;

        List<LivingEntity> entityList = SpyglassUtils.INSTANCE.getVisibleEntitiesThroughSpyglass(client.player);
        entityList.forEach(livingEntity -> {

            Triple<Float, Float, Float> screenPos = SpyglassUtils.INSTANCE.projectToPlayerView(livingEntity.getX(), livingEntity.getY() + livingEntity.getBoundingBox().getYLength()/2f, livingEntity.getZ());
            float transparency = screenPos.getThird();
            float particleScale = screenPos.getThird() / ((ZoomUtil)client.player).getZoom();
            Pair<Color, Color> colorPair = new Pair<>(new Color(0x20AC6D), new Color(0xFFFFFF));
            if (livingEntity instanceof HostileEntity) colorPair.setLeft(new Color(0xDC001E));
            if (livingEntity instanceof PlayerEntity) colorPair.setLeft(new Color(0xE6904E));

            ScreenParticleBuilder.create(LodestoneScreenParticleRegistry.STAR, screenDetectorHolder)
                    .setTransparencyData(GenericParticleData.create(0.11f * transparency, 0f).setEasing(Easing.QUINTIC_IN).build())
                    .setLifetime(3)

                    .setScaleData(GenericParticleData.create((float) (particleScale*3 + Math.sin(gameTime * 0.05f) * 0.125f), 0).build())

                    .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).setCoefficient(1.25f).build())
                    .setSpinData(spinDataBuilder.build())
                    .spawn(scaledWidth/2f + screenPos.getFirst() / ((ZoomUtil)client.player).getZoom(), scaledHeight/2f - screenPos.getSecond() / ((ZoomUtil)client.player).getZoom())
                    .addActor(particle -> {
                        Triple<Float, Float, Float> tickPos = SpyglassUtils.INSTANCE.projectToPlayerView(livingEntity.getX(), livingEntity.getY() + livingEntity.getBoundingBox().getYLength()/2f, livingEntity.getZ());
                        particle.x = tickPos.getFirst();
                        particle.y = tickPos.getSecond();
                    });

            ScreenParticleBuilder.create(LodestoneScreenParticleRegistry.WISP, screenDetectorHolder)
                    .setTransparencyData(GenericParticleData.create(0.11f * transparency, 0f).setEasing(Easing.QUINTIC_IN).build())
                    .setLifetime(3)

                    .setScaleData(GenericParticleData.create((float) (particleScale*3 - Math.sin(gameTime * 0.075f) * 0.125f), 0).build())

                    .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).build())
                    .setSpinData(spinDataBuilder.setSpinOffset(0.785f - 0.01f * gameTime % 6.28f).build())
                    .spawn(scaledWidth/2f + screenPos.getFirst() / ((ZoomUtil)client.player).getZoom(), scaledHeight/2f - screenPos.getSecond() / ((ZoomUtil)client.player).getZoom())
                    .addActor(particle -> {
                        Triple<Float, Float, Float> tickPos = SpyglassUtils.INSTANCE.projectToPlayerView(livingEntity.getX(), livingEntity.getY() + livingEntity.getBoundingBox().getYLength()/2f, livingEntity.getZ());
                        particle.x = tickPos.getFirst();
                        particle.y = tickPos.getSecond();
                    });
        });
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("TAIL"))
    private void stainedLenses$sunfireLensCharge(DrawContext context, float scale, CallbackInfo ci) {
        if (client.world == null || client.player == null) return;
        float gameTime = client.world.getTime();

        ItemStack spyglassStack = client.player.getActiveItem();
        ItemStack lensStack = DataHelper.INSTANCE.getLensStack(spyglassStack);

        if (!lensStack.isOf(RegisterItems.INSTANCE.getFOCAL_LENS())) return;
        Random random = Random.create();
        final SpinParticleDataBuilder spinDataBuilder = SpinParticleData.create(0, 1).setSpinOffset(0.025f * (gameTime + random.nextBetween(-75, 75)) % 6.28f).setEasing(Easing.EXPO_IN_OUT);

        Identifier TEXTURE_BG_OVERALL = new Identifier(StainedLenses.MOD_ID, "textures/gui/sunfire_charge_meter_background_overall.png");
        Identifier TEXTURE_BG_HELD = new Identifier(StainedLenses.MOD_ID, "textures/gui/sunfire_charge_meter_background_held.png");
        Identifier TEXTURE_OL = new Identifier(StainedLenses.MOD_ID, "textures/gui/sunfire_charge_meter_overlay.png");
        context.drawTexture(TEXTURE_BG_OVERALL, scaledWidth - (50 + 10), scaledHeight - (142 + 10), 1, 1, 50, 142, 256, 256);
        context.drawTexture(TEXTURE_BG_HELD, scaledWidth - (50 + 10), scaledHeight - (142 + 10), 1, 1, 50, 142, 256, 256);

        if (ScreenParticleHandler.canSpawnParticles) screenChargeHolder.tick();
        ScreenParticleHandler.renderParticles(screenChargeHolder);

        Pair<Color, Color> colorPairOverall = new Pair<>(new Color(0xE6904E), new Color(0xFFE0BC));
        int heightLimitOverall = (int) (((lensStack.getMaxDamage() - lensStack.getDamage() - ((FocalLensClient)client.player).getHeldCharge()) / 256.0) * 123);

        if (!MinecraftClient.getInstance().isPaused()) {
            spawnParticles(heightLimitOverall, 45, 25, gameTime, colorPairOverall, random, spinDataBuilder);
        }

        Pair<Color, Color> colorPairHeld = new Pair<>(new Color(0xDC001E), new Color(0xFFE0BC));
        int heightLimitHeld = (int) ((((FocalLensClient)client.player).getCharge() / 255.0) * 123);

        if (!MinecraftClient.getInstance().isPaused()) {
            spawnParticles(heightLimitHeld, 32, 25, gameTime, colorPairHeld, random, spinDataBuilder);
        }

        context.drawTexture(TEXTURE_OL, scaledWidth - (50 + 10), scaledHeight - (142 + 10), 1, 1, 50, 142, 256, 256);
    }

    //TODO: OPTIMIZE
    @Unique
    public void spawnParticles(int heightLimit, int widthOffset, int heightOffset, float gameTime,
                               Pair<Color, Color> colorPair, Random random, SpinParticleDataBuilder spinDataBuilder) {

        for (int y = 0; y < heightLimit / 6; y++) {
            for (int x = 0; x < 2; x++) {
                float startingX = scaledWidth - widthOffset;
                float startingY = scaledHeight - heightOffset;

                float particleX = startingX + x * 4;
                float particleY = startingY - y * 6;

                ScreenParticleBuilder.create(LodestoneScreenParticleRegistry.STAR, screenChargeHolder)
                        .setTransparencyData(GenericParticleData.create(0.025f * 0.75f, 0f).setEasing(Easing.QUINTIC_IN).build())
                        .setLifetime(random.nextBetween(6, 10))
                        .setRandomOffset(4f, 4f)

                        .setScaleData(GenericParticleData.create((float) (0.5f * 3 + Math.sin(gameTime * 0.075f) * 0.125f), 0).build())
                        .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).setCoefficient(1.25f).build())
                        .setSpinData(spinDataBuilder.build())
                        .spawn(particleX, particleY);

                ScreenParticleBuilder.create(LodestoneScreenParticleRegistry.WISP, screenChargeHolder)
                        .setTransparencyData(GenericParticleData.create(0.025f * 0.5f, 0f).setEasing(Easing.QUINTIC_IN).build())
                        .setLifetime(random.nextBetween(6, 10))
                        .setRandomOffset(4f, 4f)

                        .setScaleData(GenericParticleData.create((float) (0.25f * 3 + Math.sin(gameTime * 0.05f) * 0.125f), 0).build())
                        .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).setCoefficient(1.25f).build())
                        .setSpinData(spinDataBuilder.build())
                        .spawn(particleX, particleY);
            }
        }
    }
}
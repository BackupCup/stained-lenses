package net.backupcup.stainedlenses.mixin.common;

import net.backupcup.stainedlenses.registry.RegisterDamageTypes;
import net.backupcup.stainedlenses.registry.RegisterItems;
import net.backupcup.stainedlenses.screens.spyglassScreen.SpyglassScreenHandler;
import net.backupcup.stainedlenses.utils.DataHelper;
import net.backupcup.stainedlenses.utils.FocalLensServer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Item.class)
public class ItemMixin {
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

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void stainedLenses$SpyglassItemBarTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (world.isClient || !(entity instanceof PlayerEntity)) return;

        int lightLevel = world.getLightLevel(LightType.SKY, entity.getBlockPos()) - world.getAmbientDarkness();

        if (world.getTime() % 60 == 0) {
            if (((PlayerEntity) entity).currentScreenHandler instanceof SpyglassScreenHandler) {
                ItemStack lensStack = ((PlayerEntity) entity).currentScreenHandler.getSlot(0).getStack();
                if (lensStack.isOf(RegisterItems.INSTANCE.getFOCAL_LENS()) && lightLevel == 15) {
                    if (lensStack.getDamage() > 1) {
                        lensStack.setDamage(lensStack.getDamage()-1);
                        return;
                    }
                }
            }

            if (stack.isOf(Items.SPYGLASS) && lightLevel == 15 && !((FocalLensServer)entity).shouldBeam()) {
                if (DataHelper.INSTANCE.getLensStack(stack).isOf(RegisterItems.INSTANCE.getFOCAL_LENS())) {
                    ItemStack lensStack = DataHelper.INSTANCE.getLensStack(stack);

                    if (lensStack.getDamage() > 1) {
                        lensStack.setDamage(lensStack.getDamage()-1);
                        DataHelper.INSTANCE.storeItemStackInAnother("lensSlot", lensStack, stack);
                    }
                }
            }
        }

        if (((FocalLensServer)entity).shouldBeam()) {
            ItemStack lensStack = DataHelper.INSTANCE.getLensStack(stack);
            if (lensStack.isOf(RegisterItems.INSTANCE.getFOCAL_LENS())) {
                Vec3d startPos = entity.getEyePos();
                Vec3d direction = entity.getRotationVec(1.0F);
                double maxDistance = (lensStack.getMaxDamage()-lensStack.getDamage()) / 4f;

                float damage = ((float)maxDistance / 64f);

                Pair<List<LivingEntity>, Vec3d> raycastPair = raycastLivingEntities(world, startPos, direction, maxDistance);
                BlockPos endPos = new BlockPos((int)raycastPair.getRight().x, (int)raycastPair.getRight().y, (int)raycastPair.getRight().z);

                if (world.getBlockState(endPos).isIn(BlockTags.CAMPFIRES) ||
                    world.getBlockState(endPos).isIn(BlockTags.CANDLES) ||
                    world.getBlockState(endPos).isIn(BlockTags.CANDLE_CAKES)) {
                    if (!world.getBlockState(endPos).get(Properties.LIT)) {
                        world.setBlockState(endPos, world.getBlockState(endPos).with(Properties.LIT, true));
                        world.playSound(null, endPos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
                    }
                }

                raycastPair.getLeft().forEach((livingEntity) -> {
                    livingEntity.setAttacker((LivingEntity) entity);
                    livingEntity.setOnFireFor(Math.round(damage));

                    livingEntity.damage(RegisterDamageTypes.INSTANCE.of(livingEntity.getEntityWorld(), RegisterDamageTypes.INSTANCE.getSUNFIRE()), damage);
                });
            }
        }
    }
}

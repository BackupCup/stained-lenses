package net.backupcup.stainedlenses.mixin.client;

import io.netty.buffer.Unpooled;
import net.backupcup.stainedlenses.registry.RegisterItems;
import net.backupcup.stainedlenses.registry.RegisterPackets;
import net.backupcup.stainedlenses.utils.DataHelper;
import net.backupcup.stainedlenses.utils.FocalLensClient;
import net.backupcup.stainedlenses.utils.PostProcessorUtil;
import net.backupcup.stainedlenses.utils.ZoomUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpyglassItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.screenparticle.ParticleEmitterHandler;
import team.lodestar.lodestone.registry.common.particle.LodestoneScreenParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.ScreenParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleDataBuilder;
import team.lodestar.lodestone.systems.particle.screen.ScreenParticleHolder;

import java.awt.*;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(SpyglassItem.class)
public class SpyglassItemMixin extends Item implements ParticleEmitterHandler.ItemParticleSupplier, FabricItem {
    public SpyglassItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    private void stainedLenses$saveLensZoom(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (!(user instanceof PlayerEntity) || !user.getEntityWorld().isClient) return;

        if (MinecraftClient.getInstance().getSoundManager().isPlaying(((FocalLensClient)user).getBeamSound())) {
            MinecraftClient.getInstance().getSoundManager().stop(((FocalLensClient)user).getBeamSound());
        }
        if (MinecraftClient.getInstance().getSoundManager().isPlaying(((FocalLensClient)user).getChargeSound())) {
            MinecraftClient.getInstance().getSoundManager().stop(((FocalLensClient)user).getChargeSound());
        }

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeItemStack(stack);
        buf.writeFloat(((ZoomUtil)user).getZoom());

        ((ZoomUtil)user).setZoom(0f);
        ((FocalLensClient)user).setCharge(0);

        ClientPlayNetworking.send(RegisterPackets.INSTANCE.getRECIEVE_LENS_ZOOM(), buf);

        if (MinecraftClient.getInstance().gameRenderer.getPostProcessor() != null) {
            ((PostProcessorUtil)MinecraftClient.getInstance().gameRenderer).setPostFlag(false);
            MinecraftClient.getInstance().gameRenderer.getPostProcessor().close();
        }

        PacketByteBuf beamBuf = PacketByteBufs.create();
        beamBuf.writeBoolean(false);
        beamBuf.writeInt(((FocalLensClient)user).getHeldCharge());
        ClientPlayNetworking.send(RegisterPackets.INSTANCE.getFOCAL_LENS_UPDATE(), beamBuf);
    }

    @Override
    public void spawnLateParticles(ScreenParticleHolder target, World world, float partialTick, ItemStack stack, float x, float y) {
        ItemStack lensStack = DataHelper.INSTANCE.getLensStack(stack);

        if (!lensStack.isOf(RegisterItems.INSTANCE.getFOCAL_LENS())) return;

        float transparency = 1f - (lensStack.getDamage()-1f) / lensStack.getMaxDamage();
        float gameTime = world.getTime() + partialTick;
        Pair<Color, Color> colorPair = new Pair<>(new Color(0xE6904E), new Color(0xFFE0BC));

        final SpinParticleDataBuilder spinDataBuilder = SpinParticleData.create(0, 1).setSpinOffset(0.025f * gameTime % 6.28f).setEasing(Easing.EXPO_IN_OUT);
        ScreenParticleBuilder.create(LodestoneScreenParticleRegistry.STAR, target)
                .setTransparencyData(GenericParticleData.create(0.11f * transparency, 0f).setEasing(Easing.QUINTIC_IN).build())
                .setLifetime(7)
                .setRandomOffset(0.05f)

                .setScaleData(GenericParticleData.create((float) (transparency + Math.sin(gameTime * 0.05f) * 0.125f), 0).build())
                .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).setCoefficient(1.25f).build())
                .setSpinData(spinDataBuilder.build())
                .spawnOnStack(5, -5)

                .setScaleData(GenericParticleData.create((float) (transparency - Math.sin(gameTime * 0.075f) * 0.125f), 0).build())
                .setColorData(ColorParticleData.create(colorPair.getLeft(), colorPair.getRight()).build())
                .setSpinData(spinDataBuilder.setSpinOffset(0.785f - 0.01f * gameTime % 6.28f).build())
                .spawnOnStack(5, -5);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        InputUtil.Key boundSneakKey = ((KeybindingAccessor)MinecraftClient.getInstance().options.sneakKey).getBoundKey();

        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), boundSneakKey.getCode())) {
            tooltip.add(Text.translatable("tooltip.stained-lenses.installed_lens").formatted(Formatting.GRAY));
            ItemStack lensStack = DataHelper.INSTANCE.getLensStack(stack);
            Text lensString = !lensStack.isEmpty() ? Text.translatable(lensStack.getTranslationKey()) :
                Text.translatable("tooltip.stained-lenses.none");
            tooltip.add(Text.translatable("tooltip.stained-lenses.display_line", lensString).formatted(Formatting.BLUE));

            tooltip.add(Text.translatable("tooltip.stained-lenses.installed_module").formatted(Formatting.GRAY));
            ItemStack moduleStack = DataHelper.INSTANCE.getModuleStack(stack);
            Text additionalString = !moduleStack.isEmpty() ? Text.translatable(moduleStack.getTranslationKey()) :
                    Text.translatable("tooltip.stained-lenses.none");
            tooltip.add(Text.translatable("tooltip.stained-lenses.display_line", additionalString).formatted(Formatting.GOLD));
        } else {
            tooltip.add(Text.translatable("tooltip.stained-lenses.hold_crouch",
                    Text.translatable(MinecraftClient.getInstance().options.sneakKey.getBoundKeyTranslationKey())
                            .formatted(Formatting.GOLD)).formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        if (MinecraftClient.getInstance().player == null) return false;
        return DataHelper.INSTANCE.getLensStack(stack).isOf(RegisterItems.INSTANCE.getFOCAL_LENS()) && !MinecraftClient.getInstance().player.isUsingSpyglass();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        ItemStack lensStack = DataHelper.INSTANCE.getLensStack(stack);
        return Math.round(13f - lensStack.getDamage() * 13f / lensStack.getMaxDamage());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return DataHelper.INSTANCE.getLensStack(stack).getItemBarColor();
    }
}

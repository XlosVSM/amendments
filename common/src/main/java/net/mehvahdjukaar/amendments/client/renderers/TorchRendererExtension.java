package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.ThinAirCompat;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TorchRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer {

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        //model.rightArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -0.5f, 1);
        if (ClientConfigs.HOLDING_ANIMATION_FIXED.get()) {
            model.rightArm.xRot = -1.3f;
        } else {
            model.rightArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        }
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        //model.leftArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -1f, 0.5);
        if (ClientConfigs.HOLDING_ANIMATION_FIXED.get()) {
            model.leftArm.xRot = -1.3f;
        } else {
            model.leftArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        }
        return true;
    }


    @Override
    public <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> void renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack, HumanoidArm humanoidArm,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        if (!stack.isEmpty()) {
            // same as renderHandWithItem.

            // This could have been replaced by a model replace using block model for hand
            poseStack.pushPose();
            parentModel.translateToHand(humanoidArm, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean left = humanoidArm == HumanoidArm.LEFT;
            poseStack.translate((float) (left ? -1 : 1) / 16.0F, 0.125F, -0.625F);

            poseStack.scale(01f, 1, 1f);

            poseStack.translate(0, 3 / 16f, 2 / 16f);

            //TODO: add particle

            renderLanternModel(entity, stack, poseStack, bufferSource, light, left);
            poseStack.popPose();

            //FlameParticle f = new FlameParticle(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
        }
    }

    private static void renderLanternModel(LivingEntity entity, ItemStack itemStack, PoseStack poseStack,
                                           MultiBufferSource buffer, int light, boolean left) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BlockState state = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
        if (CompatHandler.THIN_AIR) {
            var newState = ThinAirCompat.maybeSetAirQuality(state, entity.getEyePosition(), entity.level());
            if (newState != null) {
                state = newState;
            }
        }
        if (state.hasProperty(LanternBlock.HANGING)) state = state.setValue(LanternBlock.HANGING, false);
        var model = mc.getBlockRenderer().getBlockModel(state);
        itemRenderer.render(itemStack, ItemDisplayContext.NONE, left, poseStack,
                buffer, light, OverlayTexture.NO_OVERLAY, model);
    }


}


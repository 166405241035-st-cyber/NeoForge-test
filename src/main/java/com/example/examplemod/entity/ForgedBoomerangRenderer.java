package com.example.examplemod.entity;

import com.example.examplemod.*;
import com.example.examplemod.block.*;
import com.example.examplemod.entity.*;
import com.example.examplemod.item.*;
import com.example.examplemod.skill.*;
import com.example.examplemod.skill.client.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public class ForgedBoomerangRenderer extends EntityRenderer<ForgedBoomerangEntity> {
    private final ItemRenderer itemRenderer;

    public ForgedBoomerangRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ForgedBoomerangEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.getDisplayStack().isEmpty()) return;

        poseStack.pushPose();
        // Spin the actual forged ItemStack while it flies. Because the renderer uses
        // the stack itself, future texture/model changes are picked up automatically.
        float spin = (entity.tickCount + partialTick) * 28.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.scale(1.15F, 1.15F, 1.15F);
        this.itemRenderer.renderStatic(entity.getDisplayStack(), ItemDisplayContext.GROUND,
                packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ForgedBoomerangEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

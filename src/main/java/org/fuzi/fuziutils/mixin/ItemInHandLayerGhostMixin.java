package org.fuzi.fuziutils.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import org.fuzi.fuziutils.renderer.FreecamRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerGhostMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fuziutils$hideItemOnGhost(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                            LivingEntity livingEntity, float limbSwing, float limbSwingAmount,
                                            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                                            CallbackInfo ci) {
        if (FreecamRenderer.renderingGhost) {
            ci.cancel();
        }
    }
}

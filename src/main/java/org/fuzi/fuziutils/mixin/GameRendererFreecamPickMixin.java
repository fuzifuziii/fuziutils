package org.fuzi.fuziutils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.fuzi.fuziutils.FreecamEntity;
import org.fuzi.fuziutils.FuziUtilsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public abstract class GameRendererFreecamPickMixin {

    @Redirect(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity fuziutils$pickFromRealPlayer(Minecraft instance) {
        if (FuziUtilsClient.freecamActive && instance.getCameraEntity() instanceof FreecamEntity) {
            return instance.player;
        }
        return instance.getCameraEntity();
    }
}

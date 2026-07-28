package org.fuzi.fuziutils.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.fuzi.fuziutils.renderer.FreecamRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererGhostHeadMixin {

    @Inject(method = "setModelProperties", at = @At("TAIL"))
    private void fuziutils$forceHeadOnlyForGhost(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (!FreecamRenderer.renderingGhost) return;

        PlayerRenderer self = (PlayerRenderer) (Object) this;
        self.getModel().setAllVisible(false);
        self.getModel().head.visible = true;
        self.getModel().hat.visible = true;
    }
}

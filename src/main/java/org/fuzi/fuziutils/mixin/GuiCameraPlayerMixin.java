package org.fuzi.fuziutils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.fuzi.fuziutils.FreecamEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Gui.class)
public abstract class GuiCameraPlayerMixin {

    @Shadow
    private Minecraft minecraft;

    @Overwrite
    private Player getCameraPlayer() {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player;
        }
        if (this.minecraft.getCameraEntity() instanceof FreecamEntity) {
            return this.minecraft.player;
        }
        return null;
    }
}

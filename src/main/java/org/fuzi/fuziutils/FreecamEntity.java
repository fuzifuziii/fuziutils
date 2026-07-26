package org.fuzi.fuziutils;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FreecamEntity extends net.minecraft.world.entity.Entity {

    private static final EntityDimensions FREECAM_DIMENSIONS = EntityDimensions.scalable(0.6f, 1.8f).withEyeHeight(1.62f);

    private static final double MAX_STEP = 0.2;

    public FreecamEntity(Level level, Player owner) {
        super(EntityType.ITEM, level);
        this.noPhysics = false;
        this.noCulling = true;
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());

        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return FREECAM_DIMENSIONS;
    }

    @Override
    public float getViewXRot(float partialTicks) {
        return this.getXRot();
    }

    @Override
    public float getViewYRot(float partialTick) {
        return this.getYRot();
    }

    @Override
    public float maxUpStep() {
        return 0.6f;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {}

    public void moveCamera(Vec3 delta) {
        double length = delta.length();
        if (length < 1.0E-7) return;

        int steps = Math.max(1, (int) Math.ceil(length / MAX_STEP));
        Vec3 step = delta.scale(1.0 / steps);

        for (int i = 0; i < steps; i++) {
            this.move(MoverType.SELF, step);
        }
    }
}

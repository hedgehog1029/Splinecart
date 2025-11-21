package io.github.foundationgames.splinecart.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.foundationgames.splinecart.entity.TrackFollowerEntity;
import io.github.foundationgames.splinecart.util.SUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Unique private boolean onTrackFollower = false;

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, ordinal = 0, target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void splinecart$rotateEntitiesOnTrackFollower(Entity entity, double x, double y, double z, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo info) {
        if (entity instanceof TrackFollowerEntity) return;

        Entity vehicle = entity;
        while (vehicle != null) {
            vehicle = vehicle.getVehicle();

            if (vehicle instanceof TrackFollowerEntity trackFollower) {
                var rotation = new Quaternionf();
                trackFollower.getClientOrientation(rotation, tickDelta);

                matrices.pushPose();
                onTrackFollower = true;

                var dv3d = entity.getPosition(tickDelta).subtract(trackFollower.getPosition(tickDelta));
                var diff = new Vector3d(dv3d.x(), dv3d.y(), dv3d.z());
                matrices.translate(-diff.x(), -diff.y(), -diff.z());

                matrices.mulPose(rotation);

                matrices.translate(diff.x(), diff.y(), diff.z());
                matrices.mulPose(SUtil.BACKWARDS);

                return;
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void splinecart$undoTransform(Entity entity, double x, double y, double z, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo info) {
        if (onTrackFollower) {
            onTrackFollower = false;
            matrices.popPose();
        }
    }
}

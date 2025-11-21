package io.github.foundationgames.splinecart.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.foundationgames.splinecart.SplinecartClient;
import io.github.foundationgames.splinecart.block.entity.TrackTiesBlockEntityRenderer;
import io.github.foundationgames.splinecart.entity.TrackFollowerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ConcurrentModificationException;
import java.util.Set;

@Mixin(value = {LevelRenderer.class}, priority = 1500)
public class WorldRendererMixin {
    @Shadow
    @Final
    private Set<BlockEntity> globalBlockEntities;
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
            require = 0, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/SectionOcclusionGraph;consumeFrustumUpdate()Z"))
    private boolean splinecart$updateChunkOcclusionCullingWhileOnTrack(boolean old) {
        if (SplinecartClient.CFG_ROTATE_CAMERA.get()) {
            var entity = minecraft.cameraEntity;
            while (entity != null) {
                entity = entity.getVehicle();

                if (entity instanceof TrackFollowerEntity) {
                    return true;
                }
            }
        }

        return old;
    }

    @Inject(method = "blockChanged(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V", at = @At("TAIL"))
    private void splinecart$updateBlockEntityVbos(BlockGetter world, BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        if (SplinecartClient.CFG_VBOS.get()) {
            var chunkPos = SectionPos.of(pos);
            try {
                TrackTiesBlockEntityRenderer.queueVboRebuildsForChunkUpdate(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), globalBlockEntities);
            } catch (ConcurrentModificationException ignored) {}
        }
    }
}

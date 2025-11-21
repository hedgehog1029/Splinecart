package io.github.foundationgames.splinecart.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.foundationgames.splinecart.Splinecart;
import io.github.foundationgames.splinecart.SplinecartClient;
import io.github.foundationgames.splinecart.block.TrackTiesBlockEntity;
import io.github.foundationgames.splinecart.util.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.Set;

public class TrackTiesBlockEntityRenderer implements BlockEntityRenderer<TrackTiesBlockEntity> {
    public static final int WHITE = 0xFFFFFFFF;
    public static final Vector3f WHITEF = new Vector3f(1, 1, 1);
    public static final ResourceLocation TRACK_TEXTURE = Splinecart.id("textures/track.png");
    public static final ResourceLocation TRACK_OVERLAY_TEXTURE = Splinecart.id("textures/track_overlay.png");
    public static final ResourceLocation POSE_TEXTURE_DEBUG = Splinecart.id("textures/debug.png");

    public TrackTiesBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(TrackTiesBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        entity.clientTime += tickDelta;

        if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            matrices.pushPose();

            matrices.translate(0.5, 0.5, 0.5);
            var buffer = vertexConsumers.getBuffer(RenderType.entityCutoutNoCull(POSE_TEXTURE_DEBUG));
            renderDebug(entity.pose(), matrices.last(), buffer);

            matrices.popPose();
        }

        int trackResolution = SplinecartClient.CFG_TRACK_RESOLUTION.get();
        int segs = trackResolution * Math.max((int) entity.estimatedTrackLength(), 2);
        var nextE = entity.next();
        var prevE = entity.prev();

        matrices.pushPose();

        var pos = entity.getBlockPos();
        matrices.translate(-pos.getX(), -pos.getY(), -pos.getZ());

        var overlayColor = new Vector3f(WHITEF);
        float[] overlayVOffset = {0};

        int power = entity.power();

        if (nextE != null) {
            var trackType = entity.nextType();

            if (trackType.overlay != null) {
                power = Math.max(entity.power(), nextE.power());
                trackType.overlay.calculateEffects(power, entity.clientTime, overlayColor, overlayVOffset);
            }
        }


        if (!(entity.geometry instanceof ClientTrackGeometry geo &&
                geo.render(matrices, light, overlay, segs,
                        overlayVOffset[0], overlayColor,
                        power, trackResolution,
                        getTexture(), getTrackOverlayTexture(),
                        entity, prevE, nextE)
        )) {
            TrackRenderer.renderTrack(matrices.last(), matrices.last(),
                    TrackRenderer.immediateBuf(vertexConsumers, getTexture(), RenderType::entityCutoutNoCullZOffset),
                    TrackRenderer.immediateBuf(vertexConsumers, getTrackOverlayTexture(), RenderType::entityCutoutNoCull),
                    overlay, light, segs,
                    overlayVOffset[0], overlayColor,
                    entity, prevE, nextE);
        }

        matrices.popPose();
    }

    protected ResourceLocation getTexture() {
        return TRACK_TEXTURE;
    }

    protected ResourceLocation getTrackOverlayTexture() {
        return TRACK_OVERLAY_TEXTURE;
    }

    @Override
    public boolean shouldRenderOffScreen(TrackTiesBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return SplinecartClient.CFG_TRACK_RENDER_DISTANCE.get() * 16;
    }

    private static void renderDebug(Pose pose, PoseStack.Pose entry, VertexConsumer buffer) {
        var posMat = entry.pose();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                posMat.setRowColumn(x, y, (float) pose.basis().getRowColumn(x, y));
            }
        }

        buffer.addVertex(entry, 1, 0, 1).setColor(WHITE).setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(entry, 0, 1, 0);
        buffer.addVertex(entry, 0, 0, 1).setColor(WHITE).setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(entry, 0, 1, 0);
        buffer.addVertex(entry, 0, 0, 0).setColor(WHITE).setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(entry, 0, 1, 0);
        buffer.addVertex(entry, 1, 0, 0).setColor(WHITE).setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(entry, 0, 1, 0);
    }

    public static void queueVboRebuildsForChunkUpdate(int sectionX, int sectionY, int sectionZ, Set<BlockEntity> blockEntities) {
        for (var be : blockEntities) if (be instanceof TrackTiesBlockEntity ties) {
            if (ties.geometry.isInChunk(sectionX, sectionY, sectionZ)) {
                ties.geometry.needsRebuild = true;
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(TrackTiesBlockEntity blockEntity) {
        var thisTiePos = blockEntity.getBlockPos();
        var nextTiePos = blockEntity.nextPos();
        if (nextTiePos != null) {
            return AABB.of(BoundingBox.fromCorners(thisTiePos, nextTiePos));
        }

        return new AABB(thisTiePos);
    }
}

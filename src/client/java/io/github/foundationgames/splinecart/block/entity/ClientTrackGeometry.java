package io.github.foundationgames.splinecart.block.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import io.github.foundationgames.splinecart.SplinecartClient;
import io.github.foundationgames.splinecart.block.TrackGeometry;
import io.github.foundationgames.splinecart.block.TrackTiesBlockEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.function.Supplier;

public class ClientTrackGeometry extends TrackGeometry {
    private VertexBuffer trackVbo = null;
    private VertexBuffer overlayVbo = null;

    private int lastKnownPowerState = -1;
    private int lastKnownTrackResolution = -1;

    public ClientTrackGeometry(TrackTiesBlockEntity trackTies) {
        super(trackTies);

        this.needsRebuild = true;
    }

    public boolean render(PoseStack matrices,
                          int light, int overlay, int segs, float olVOffset, Vector3fc olColor,
                          int powerState, int trackResolution,
                          ResourceLocation trackTexture, ResourceLocation overlayTexture,
                          TrackTiesBlockEntity curr, TrackTiesBlockEntity prevE, TrackTiesBlockEntity nextE) {
        if (!SplinecartClient.CFG_VBOS.get()) {
            this.needsRebuild = true;
            return false;
        }

        if (powerState != lastKnownPowerState) {
            this.needsRebuild = true;
        }

        if (trackResolution != lastKnownTrackResolution) {
            this.needsRebuild = true;
        }

        TrackRenderer.BufferProvider trackBuffer = null;
        TrackRenderer.BufferProvider overlayBuffer = null;
        VertexBuffer trackVbo = null;
        VertexBuffer overlayVbo = null;
        if (this.needsRebuild) {
            if (this.trackVbo != null) {
                this.trackVbo.close();
                this.trackVbo = null;
            }
            if (this.overlayVbo != null) {
                this.overlayVbo.close();
                this.overlayVbo = null;
            }

            trackVbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            trackBuffer = TrackRenderer.vboBuf(trackVbo, Tesselator.getInstance());

            overlayVbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            overlayBuffer = TrackRenderer.vboBuf(overlayVbo, Tesselator.getInstance());

            this.resetBounds();
            var currSec = SectionPos.of(curr.getBlockPos());
            var nextSec = nextE != null ? SectionPos.of(nextE.getBlockPos()) : currSec;

            this.minSectionX = Math.min(currSec.getX(), nextSec.getX());
            this.minSectionY = Math.min(currSec.getY(), nextSec.getY());
            this.minSectionZ = Math.min(currSec.getZ(), nextSec.getZ());
            this.maxSectionX = Math.max(currSec.getX(), nextSec.getX());
            this.maxSectionY = Math.max(currSec.getY(), nextSec.getY());
            this.maxSectionZ = Math.max(currSec.getZ(), nextSec.getZ());

            this.needsRebuild = false;
        }

        var trackTransform = new PoseStack();

        matrices.pushPose();
        trackTransform.pushPose();
        int status = TrackRenderer.renderTrack(trackTransform.last(), trackTransform.last(),
                trackBuffer, overlayBuffer,
                overlay, light, segs,
                0, olColor,
                curr, prevE, nextE);
        matrices.popPose();
        trackTransform.popPose();

        boolean hasTrackGeo = (status & 0b01) > 0;
        boolean hasOverlayGeo = (status & 0b10) > 0;

        if (trackVbo != null) {
            this.trackVbo = hasTrackGeo ? trackVbo : null;
        }
        if (overlayVbo != null) {
            this.overlayVbo = hasOverlayGeo ? overlayVbo : null;
        }

        matrices.pushPose();

        var posMatrix = new Matrix4f().set(RenderSystem.getModelViewMatrix());
        posMatrix.mul(matrices.last().pose());

        var fog = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogEnd(999999999);

        if (this.trackVbo != null) {
            drawVbo(this.trackVbo, posMatrix, () -> RenderType.entityCutoutNoCullZOffset(trackTexture),
                    GameRenderer::getRendertypeEntityCutoutNoCullZOffsetShader);
        }

        if (this.overlayVbo != null) {
            drawVbo(this.overlayVbo, posMatrix, () -> SplinecartClient.renderLayerEntityCutoutNoCullUvTransform(overlayTexture, 0, olVOffset),
                    SplinecartClient::getProgramEntityCutoutNoCullUvTransform);
        }

        RenderSystem.setShaderFogEnd(fog);

        matrices.popPose();

        this.lastKnownPowerState = powerState;
        this.lastKnownTrackResolution = trackResolution;

        return true;
    }

    public static void drawVbo(VertexBuffer vbo, Matrix4f transform, Supplier<RenderType> renderLayer, Supplier<ShaderInstance> shader) {
        var layer = renderLayer.get();
        layer.setupRenderState();

        var program = shader.get();

        vbo.bind();
        vbo.drawWithShader(transform, RenderSystem.getProjectionMatrix(), program);
        VertexBuffer.unbind();

        layer.clearRenderState();
    }

    @Override
    public void close() {
        super.close();

        if (this.trackVbo != null) {
            trackVbo.close();
        }
        if (this.overlayVbo != null) {
            overlayVbo.close();
        }

        this.trackVbo = null;
        this.overlayVbo = null;
    }
}

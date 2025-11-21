package io.github.foundationgames.splinecart;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.splinecart.block.TrackGeometry;
import io.github.foundationgames.splinecart.block.entity.ClientTrackGeometry;
import io.github.foundationgames.splinecart.block.entity.TrackTiesBlockEntityRenderer;
import io.github.foundationgames.splinecart.config.Config;
import io.github.foundationgames.splinecart.config.ConfigOption;
import io.github.foundationgames.splinecart.util.SUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.IOException;

public class SplinecartClient {
    public static final Config CONFIG = new Config("splinecart_client",
            () -> FMLPaths.CONFIGDIR.get().resolve("splinecart").resolve("splinecart_client.properties"));

    public static final ConfigOption.BooleanOption CFG_ROTATE_CAMERA = CONFIG.optBool("rotate_camera", true);
    public static final ConfigOption.BooleanOption CFG_VBOS = CONFIG.optBool("vbos", false);
    public static final ConfigOption.IntOption CFG_TRACK_RESOLUTION = CONFIG.optInt("track_resolution", 3, 1, 16);
    public static final ConfigOption.IntOption CFG_TRACK_RENDER_DISTANCE = CONFIG.optInt("track_render_distance", 8, 4, 32);

    public static ShaderInstance entityCutoutNoCullUvTransformProgram;

    private final SplinecartHud hud = new SplinecartHud();

    public void onInitializeClient() {
        SUtil.TICK_DELTA = () -> Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

        try {
            CONFIG.load();
        } catch (IOException e) {
            Splinecart.LOGGER.error("Error loading client config on mod init", e);
        }

        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        TrackGeometry.CONSTRUCTOR = ClientTrackGeometry::new;
    }

    public static ShaderInstance getProgramEntityCutoutNoCullUvTransform() {
        return entityCutoutNoCullUvTransformProgram;
    }

    public static RenderType renderLayerEntityCutoutNoCullUvTransform(ResourceLocation texture, float x, float y) {
        return RenderType.create(
                "splinecart_entity_cutout_no_cull_uv_transform",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(SplinecartClient::getProgramEntityCutoutNoCullUvTransform))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(x, y))
                        .setTransparencyState(RenderType.NO_TRANSPARENCY)
                        .setCullState(RenderType.NO_CULL)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setOverlayState(RenderType.OVERLAY)
                        .createCompositeState(false)
        );
    }

    @SubscribeEvent
    public void registerShaders(RegisterShadersEvent event) {
        try {
            var shader = new ShaderInstance(
                    event.getResourceProvider(),
                    Splinecart.id("splinecart_rendertype_entity_cutout_no_cull_uv_transform"),
                    DefaultVertexFormat.NEW_ENTITY
            );
            event.registerShader(shader, program -> entityCutoutNoCullUvTransformProgram = program);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers ev) {
        ev.registerBlockEntityRenderer(Splinecart.TRACK_TIES_BE.get(), TrackTiesBlockEntityRenderer::new);
        ev.registerEntityRenderer(Splinecart.TRACK_FOLLOWER.get(), NoopRenderer::new);
    }

    public void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("splinecartc")
                        .then(CONFIG.command(LiteralArgumentBuilder.literal("config"),
                                CommandSourceStack::sendSystemMessage))
        );
    }

    public void onRenderPost(RenderGuiEvent.Post event) {
        hud.onHudRender(event.getGuiGraphics(), event.getPartialTick());
    }
}
package io.github.foundationgames.splinecart;

import io.github.foundationgames.splinecart.block.TrackTiesBlock;
import io.github.foundationgames.splinecart.block.TrackTiesBlockEntity;
import io.github.foundationgames.splinecart.component.OriginComponent;
import io.github.foundationgames.splinecart.entity.TrackFollowerEntity;
import io.github.foundationgames.splinecart.item.TrackItem;
import io.github.foundationgames.splinecart.util.TrackProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Splinecart {
    public static final Logger LOGGER = LoggerFactory.getLogger("splinecart");

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("splinecart");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("splinecart");
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "splinecart");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "splinecart");
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "splinecart");

    public static final DeferredBlock<TrackTiesBlock> TRACK_TIES = BLOCKS.register("track_ties",
            () -> new TrackTiesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrackTiesBlockEntity>> TRACK_TIES_BE = BLOCK_ENTITY_TYPES.register("track_ties",
            () -> BlockEntityType.Builder.of(TrackTiesBlockEntity::new, TRACK_TIES.get()).build(null));

    public static final DeferredItem<BlockItem> TRACK_TIES_ITEM = ITEMS.registerSimpleBlockItem(TRACK_TIES, new Item.Properties()
            .component(DataComponents.LORE,
                    lore(Component.translatable("item.splinecart.track_ties.desc").withStyle(ChatFormatting.GRAY))
            ));
    public static final DeferredItem<TrackItem> TRACK = ITEMS.register("track", () ->
            new TrackItem(TrackType.DEFAULT, new Item.Properties().component(DataComponents.LORE,
                    lore(Component.translatable("item.splinecart.track.desc").withStyle(ChatFormatting.GRAY))
            )));
    public static final DeferredItem<TrackItem> CHAIN_DRIVE_TRACK = ITEMS.register("chain_drive_track", () ->
            new TrackItem(TrackType.CHAIN_DRIVE, new Item.Properties().component(DataComponents.LORE,
                    lore(Component.translatable("item.splinecart.chain_drive_track.desc").withStyle(ChatFormatting.GRAY))
            )));
    public static final DeferredItem<TrackItem> MAGNETIC_TRACK = ITEMS.register("magnetic_track", () ->
            new TrackItem(TrackType.MAGNETIC, new Item.Properties().component(DataComponents.LORE,
                    lore(Component.translatable("item.splinecart.magnetic_track.desc").withStyle(ChatFormatting.GRAY))
            )));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<OriginComponent>> ORIGIN_POS = DATA_COMPONENTS.register("origin",
            () -> DataComponentType.<OriginComponent>builder().persistent(OriginComponent.CODEC).build());

    public static final DeferredHolder<EntityType<?>, EntityType<TrackFollowerEntity>> TRACK_FOLLOWER = ENTITY_TYPES.register("track_follower",
            () -> EntityType.Builder.<TrackFollowerEntity>of(TrackFollowerEntity::new, MobCategory.MISC).updateInterval(2).sized(0.25f, 0.25f).build("track_follower"));

    public static final TagKey<EntityType<?>> CARTS = TagKey.create(Registries.ENTITY_TYPE, id("carts"));

    public static final CreativeModeTab SPLINECART_GROUP = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.splinecart"))
            .displayItems((ctx, e) -> {
                e.accept(TRACK_TIES_ITEM.get());
                e.accept(TRACK.get());
                e.accept(CHAIN_DRIVE_TRACK.get());
                e.accept(MAGNETIC_TRACK.get());
            })
            .icon(CHAIN_DRIVE_TRACK::toStack)
            .build();

    @SubscribeEvent
    public void register(RegisterEvent ev) {
        ev.register(Registries.CREATIVE_MODE_TAB, id("splinecart"), () -> SPLINECART_GROUP);

        ev.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.key(), id("track_progress"), () -> TrackProgress.DATA_HANDLER);
    }

    @SubscribeEvent
    public void modifyCreativeTabs(BuildCreativeModeTabContentsEvent ev) {
        if (ev.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            ev.accept(TRACK_TIES_ITEM.get());
            ev.accept(TRACK.get());
            ev.accept(CHAIN_DRIVE_TRACK.get());
            ev.accept(MAGNETIC_TRACK.get());
        }
    }

    public static ItemLore lore(Component lore) {
        return new ItemLore(List.of(lore));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("splinecart", path);
    }
}
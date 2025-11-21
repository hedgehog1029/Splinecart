package io.github.foundationgames.splinecart.util;

import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.util.function.BiFunction;
import java.util.function.DoubleSupplier;

public enum SUtil {;
    public static final Vector3f[] REDSTONE_COLOR_LUT = Util.make(new Vector3f[16], colors -> {
        for (int i = 0; i <= 15; i++) {
            float strength = (float)i / 15.0F;
            colors[i] = new Vector3f(
                    strength * 0.6f + (strength > 0.0f ? 0.4f : 0.3f),
                    Mth.clamp((strength * strength * 0.7f) - 0.5f, 0, 1),
                    Mth.clamp((strength * strength * 0.6f) - 0.7f, 0, 1)
            );
        }
    });

    public static final Quaternionf BACKWARDS = Axis.YP.rotation(Mth.PI);
    public static DoubleSupplier TICK_DELTA = () -> 0;

    public static void putBlockPos(CompoundTag nbt, @Nullable BlockPos pos, String key) {
        if (pos == null) {
            nbt.putIntArray(key, new int[0]);
        } else nbt.putIntArray(key, new int[] {pos.getX(), pos.getY(), pos.getZ()});
    }

    public static BlockPos getBlockPos(CompoundTag nbt, String key) {
        var arr = nbt.getIntArray(key);
        if (arr.length < 3) return null;

        return new BlockPos(arr[0], arr[1], arr[2]);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation id, BiFunction<ResourceLocation, ResourceKey<V>, T> obj) {
        ResourceKey<V> key = ResourceKey.create(registry.key(), id);
        return Registry.register(registry, id, obj.apply(id, key));
    }

    public static boolean failsSanityCheck(Vector3dc vec) {
        return Double.isNaN(vec.x()) || Double.isNaN(vec.y()) || Double.isNaN(vec.z());
    }

    public static boolean failsSanityCheck(Quaternionf rot) {
        return Double.isNaN(rot.x()) || Double.isNaN(rot.y()) || Double.isNaN(rot.z()) || Double.isNaN(rot.w());
    }
}

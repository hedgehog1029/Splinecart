package io.github.foundationgames.splinecart.mixin.client;

import io.github.foundationgames.splinecart.entity.TrackFollowerEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartSoundInstance.class)
public abstract class MovingMinecartSoundInstanceMixin extends AbstractTickableSoundInstance {
    @Shadow
    @Final
    private AbstractMinecart minecart;

    protected MovingMinecartSoundInstanceMixin(SoundEvent soundEvent, SoundSource soundCategory, RandomSource random) {
        super(soundEvent, soundCategory, random);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void splinecart$adjustSoundWhenOnTrack(CallbackInfo info) {
        if (!this.isStopped() && minecart.getVehicle() instanceof TrackFollowerEntity trackFollower) {
            float amp = trackFollower.getClientMotion().length();
            this.volume = Mth.lerp(Mth.clamp(amp, 0, 0.5f), 0, 0.7f);
        }
    }
}

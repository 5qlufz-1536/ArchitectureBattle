package com.saluf.architecturebattle.util;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public final class SoundHelper {

    private SoundHelper() {
    }

    public static void play(ServerPlayerEntity player, SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        RegistryEntry<SoundEvent> entry = resolve(soundEvent);
        long seed = player.getWorld().getRandom().nextLong();
        PlaySoundS2CPacket packet = new PlaySoundS2CPacket(entry, category, player.getX(), player.getY(), player.getZ(), volume, pitch, seed);
        player.networkHandler.sendPacket(packet);
    }

    private static RegistryEntry<SoundEvent> resolve(SoundEvent soundEvent) {
        RegistryEntry<SoundEvent> entry = Registries.SOUND_EVENT.getEntry(soundEvent);
        if (entry == null) {
            throw new IllegalStateException("Sound event is not registered: " + soundEvent);
        }
        return entry;
    }
}

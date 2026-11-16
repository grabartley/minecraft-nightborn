package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record StructureMusic(
    Identifier sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {

  public static final Codec<StructureMusic> CODEC =
      RecordCodecBuilder.<StructureMusic>create(
              instance ->
                  instance
                      .group(
                          Identifier.CODEC.fieldOf("sound").forGetter(StructureMusic::sound),
                          Codec.INT.fieldOf("min_delay").forGetter(StructureMusic::minDelay),
                          Codec.INT.fieldOf("max_delay").forGetter(StructureMusic::maxDelay),
                          Codec.BOOL
                              .optionalFieldOf("replace_current_music", true)
                              .forGetter(StructureMusic::replaceCurrentMusic))
                      .apply(instance, StructureMusic::new))
          .validate(StructureMusic::delaysInOrder);

  public static final PacketCodec<ByteBuf, StructureMusic> PACKET_CODEC =
      PacketCodec.tuple(
          Identifier.PACKET_CODEC,
          StructureMusic::sound,
          PacketCodecs.VAR_INT,
          StructureMusic::minDelay,
          PacketCodecs.VAR_INT,
          StructureMusic::maxDelay,
          PacketCodecs.BOOL,
          StructureMusic::replaceCurrentMusic,
          StructureMusic::new);

  public MusicSound toMusic() {
    return new MusicSound(
        RegistryEntry.of(SoundEvent.of(sound)), minDelay, maxDelay, replaceCurrentMusic);
  }

  static DataResult<StructureMusic> delaysInOrder(final StructureMusic music) {
    return music.minDelay >= 0 && music.minDelay <= music.maxDelay
        ? DataResult.success(music)
        : DataResult.error(() -> "Structure music needs 0 <= min_delay <= max_delay, got " + music);
  }
}

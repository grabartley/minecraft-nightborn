package com.grahambartley.nightborn.guild;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record ContractProgress(Identifier contract, int done) {

  public static final Codec<ContractProgress> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("contract").forGetter(ContractProgress::contract),
                      Codec.intRange(0, HunterContract.MOST_TARGETS)
                          .optionalFieldOf("done", 0)
                          .forGetter(ContractProgress::done))
                  .apply(instance, ContractProgress::new));

  public static final PacketCodec<ByteBuf, ContractProgress> PACKET_CODEC =
      PacketCodec.tuple(
          Identifier.PACKET_CODEC,
          ContractProgress::contract,
          PacketCodecs.VAR_INT,
          ContractProgress::done,
          ContractProgress::new);

  public ContractProgress {
    done = Math.clamp(done, 0, HunterContract.MOST_TARGETS);
  }

  public ContractProgress advanced() {
    return new ContractProgress(contract, done + 1);
  }
}

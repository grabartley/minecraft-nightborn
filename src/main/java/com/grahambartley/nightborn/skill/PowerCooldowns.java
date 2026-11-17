package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record PowerCooldowns(Map<Identifier, Long> readyAt) {

  public static final PowerCooldowns NONE = new PowerCooldowns(Map.of());

  public static final Codec<PowerCooldowns> CODEC =
      Codec.unboundedMap(Identifier.CODEC, Codec.LONG)
          .xmap(PowerCooldowns::new, PowerCooldowns::readyAt);

  public static final PacketCodec<ByteBuf, PowerCooldowns> PACKET_CODEC =
      PacketCodecs.<ByteBuf, Identifier, Long, Map<Identifier, Long>>map(
              HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.VAR_LONG)
          .xmap(PowerCooldowns::new, PowerCooldowns::readyAt);

  public PowerCooldowns {
    readyAt = Map.copyOf(readyAt);
  }

  public long ticksLeft(final Identifier power, final long now) {
    final Long at = readyAt.get(power);
    return at == null ? 0L : Math.max(0L, at - now);
  }

  public PowerCooldowns start(final Identifier power, final long now, final int ticks) {
    final Map<Identifier, Long> next = new HashMap<>();
    readyAt.forEach(
        (id, at) -> {
          if (at > now) {
            next.put(id, at);
          }
        });
    if (ticks > 0) {
      next.put(power, now + ticks);
    }
    return new PowerCooldowns(next);
  }

  public static int shortened(final int cooldownTicks, final double factor) {
    return (int) Math.max(0L, Math.round(cooldownTicks * Math.max(0.0, factor)));
  }

  public static float fraction(final long ticksLeft, final int cooldownTicks) {
    if (cooldownTicks <= 0 || ticksLeft <= 0) {
      return 0.0F;
    }
    return Math.min(1.0F, (float) ticksLeft / cooldownTicks);
  }
}

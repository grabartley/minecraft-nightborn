package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class FreePowers {

  public static final AttachmentType<Map<Identifier, Long>> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "free_powers"),
          builder ->
              builder
                  .persistent(Codec.unboundedMap(Identifier.CODEC, Codec.LONG))
                  .syncWith(
                      PacketCodecs.<ByteBuf, Identifier, Long, Map<Identifier, Long>>map(
                              HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.VAR_LONG)
                          .cast(),
                      AttachmentSyncPredicate.targetOnly()));

  private FreePowers() {}

  public static void register() {}

  public static void grant(final LivingEntity caster, final FreePower power) {
    final long until = caster.getWorld().getTime() + power.durationTicks();
    final Map<Identifier, Long> next = new HashMap<>(caster.getAttachedOrElse(TYPE, Map.of()));
    power.skills().forEach(skill -> next.put(skill, until));
    caster.setAttached(TYPE, Map.copyOf(next));
  }

  public static boolean free(final LivingEntity caster, @Nullable final Identifier skill) {
    if (skill == null) {
      return false;
    }
    final Long until = caster.getAttachedOrElse(TYPE, Map.of()).get(skill);
    return until != null && caster.getWorld().getTime() < until;
  }
}

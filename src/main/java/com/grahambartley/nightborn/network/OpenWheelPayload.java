package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record OpenWheelPayload(int entityId, SkillTree theme, Text title, List<WheelEntry> entries)
    implements CustomPayload {

  public static final int MOST_ENTRIES = 16;

  public static final CustomPayload.Id<OpenWheelPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "open_wheel"));

  public static final PacketCodec<RegistryByteBuf, OpenWheelPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          OpenWheelPayload::entityId,
          PacketCodecs.indexed(index -> SkillTree.values()[index], SkillTree::ordinal),
          OpenWheelPayload::theme,
          TextCodecs.REGISTRY_PACKET_CODEC,
          OpenWheelPayload::title,
          WheelEntry.CODEC.collect(PacketCodecs.toList(MOST_ENTRIES)),
          OpenWheelPayload::entries,
          OpenWheelPayload::new);

  public OpenWheelPayload {
    entries = List.copyOf(entries);
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}

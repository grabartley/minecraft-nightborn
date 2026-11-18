package com.grahambartley.nightborn.client.name;

import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.network.StageNamePayload;
import com.grahambartley.nightborn.rank.GuildRank;
import com.grahambartley.nightborn.rank.RankData;
import com.grahambartley.nightborn.rank.RankMark;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public final class StageNameView {

  private static final Map<Integer, Identifier> STAGES = new HashMap<>();
  private static final Map<Integer, Identifier> RANKS = new HashMap<>();

  private StageNameView() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        StageNamePayload.ID,
        (payload, context) -> {
          remember(STAGES, payload.entityId(), payload.stage());
          remember(RANKS, payload.entityId(), payload.rank());
        });
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          STAGES.clear();
          RANKS.clear();
        });
    ClientEntityEvents.ENTITY_UNLOAD.register(
        (entity, world) -> {
          STAGES.remove(entity.getId());
          RANKS.remove(entity.getId());
        });
  }

  static void remember(
      final Map<Integer, Identifier> known, final int entityId, final Optional<Identifier> id) {
    id.ifPresentOrElse(shown -> known.put(entityId, shown), () -> known.remove(entityId));
  }

  public static Text shown(final Entity entity, final Text name) {
    if (!(entity instanceof PlayerEntity)) {
      return name;
    }
    final Optional<TextColor> color =
        Optional.ofNullable(STAGES.get(entity.getId()))
            .flatMap(id -> VampireStages.stage(entity.getRegistryManager(), id))
            .flatMap(stage -> stage.look().nameColor());
    final Optional<RankMark> mark =
        Optional.ofNullable(RANKS.get(entity.getId()))
            .flatMap(id -> RankData.rank(entity.getRegistryManager(), id))
            .map(GuildRank::mark);
    return marked(coloured(name, color), mark);
  }

  static Text coloured(final Text name, final Optional<TextColor> color) {
    return color.<Text>map(c -> name.copy().styled(style -> style.withColor(c))).orElse(name);
  }

  static Text marked(final Text name, final Optional<RankMark> mark) {
    return mark.<Text>map(
            shown -> Text.empty().append(shown.text()).append(Text.literal(" ")).append(name))
        .orElse(name);
  }
}

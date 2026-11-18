package com.grahambartley.nightborn.client.name;

import com.grahambartley.nightborn.challenge.ChallengeData;
import com.grahambartley.nightborn.challenge.Feat;
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

  static final String TITLED = "nightborn.name.with_title";

  private static final Map<Integer, Identifier> STAGES = new HashMap<>();
  private static final Map<Integer, Identifier> RANKS = new HashMap<>();
  private static final Map<Integer, Identifier> TITLES = new HashMap<>();

  private StageNameView() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        StageNamePayload.ID,
        (payload, context) -> {
          remember(STAGES, payload.entityId(), payload.stage());
          remember(RANKS, payload.entityId(), payload.rank());
          remember(TITLES, payload.entityId(), payload.title());
        });
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          STAGES.clear();
          RANKS.clear();
          TITLES.clear();
        });
    ClientEntityEvents.ENTITY_UNLOAD.register(
        (entity, world) -> {
          STAGES.remove(entity.getId());
          RANKS.remove(entity.getId());
          TITLES.remove(entity.getId());
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
    final Optional<Text> title =
        Optional.ofNullable(TITLES.get(entity.getId()))
            .flatMap(
                id ->
                    entity.getRegistryManager().getOptional(ChallengeData.FEAT).map(r -> r.get(id)))
            .map(Feat::title);
    return marked(coloured(titled(name, title), color), mark);
  }

  static Text titled(final Text name, final Optional<Text> title) {
    return title.<Text>map(shown -> Text.translatable(TITLED, name, shown)).orElse(name);
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

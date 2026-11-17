package com.grahambartley.nightborn.client.name;

import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.network.StageNamePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public final class StageNameView {

  private static final Map<Integer, Identifier> STAGES = new HashMap<>();

  private StageNameView() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        StageNamePayload.ID,
        (payload, context) -> remember(STAGES, payload.entityId(), payload.stage()));
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> STAGES.clear());
  }

  static void remember(
      final Map<Integer, Identifier> stages, final int entityId, final Optional<Identifier> stage) {
    stage.ifPresentOrElse(id -> stages.put(entityId, id), () -> stages.remove(entityId));
  }

  public static Text shown(final Entity entity, final Text name) {
    if (!(entity instanceof PlayerEntity)) {
      return name;
    }
    final Optional<TextColor> color =
        Optional.ofNullable(STAGES.get(entity.getId()))
            .flatMap(id -> VampireStages.stage(entity.getRegistryManager(), id))
            .flatMap(stage -> stage.look().nameColor());
    return coloured(name, color);
  }

  static Text coloured(final Text name, final Optional<TextColor> color) {
    return color.<Text>map(c -> name.copy().styled(style -> style.withColor(c))).orElse(name);
  }
}

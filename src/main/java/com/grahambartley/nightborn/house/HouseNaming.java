package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.name.NameRules;
import com.grahambartley.nightborn.network.NameHousePayload;
import com.grahambartley.nightborn.network.OpenHouseNamingPayload;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class HouseNaming {

  static final String NAMED = "nightborn.house.named";
  static final String NOT_LORD = "nightborn.house.invite.not_lord";

  private HouseNaming() {}

  static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        NameHousePayload.ID,
        (payload, context) -> name(context.player(), payload.name(), payload.crest()));
  }

  public static boolean offer(final ServerPlayerEntity lord) {
    final Optional<House> house = Houses.get(lord.getServer()).ledBy(lord.getUuid());
    if (house.isEmpty()) {
      lord.sendMessage(Text.translatable(NOT_LORD), true);
      return false;
    }
    final HouseIdentity identity = house.get().identity();
    ServerPlayNetworking.send(
        lord,
        new OpenHouseNamingPayload(
            identity.name().orElseGet(() -> lord.getGameProfile().getName()),
            identity.crest().orElse(Crest.PLAIN)));
    return true;
  }

  public static boolean name(final ServerPlayerEntity lord, final String typed, final Crest crest) {
    final Houses houses = Houses.get(lord.getServer());
    final Optional<House> house = houses.ledBy(lord.getUuid());
    final NameRules.Verdict verdict = NameRules.check(typed);
    if (house.isEmpty() || !verdict.ok()) {
      return false;
    }
    houses.put(house.get().withIdentity(new HouseIdentity(verdict.name(), Optional.of(crest))));
    lord.sendMessage(Text.translatable(NAMED, verdict.name().orElseThrow()), false);
    lord.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0F, 0.8F);
    return true;
  }
}

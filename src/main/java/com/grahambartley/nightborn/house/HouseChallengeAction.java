package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.house.duel.HouseDuels;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HouseChallengeAction implements WheelAction<PlayerEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "house_challenge");
  static final Identifier ICON = Identifier.of("minecraft", "iron_sword");
  static final String LABEL = "nightborn.house.wheel.challenge";
  static final String DECLARED_TO_LORD = "nightborn.house.challenge.received";

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final PlayerEntity lord) {
    return verdict(player, lord) == ChallengeRules.Verdict.DECLARED;
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final PlayerEntity lord) {
    return List.of(WheelEntry.of(ID, ICON, Text.translatable(LABEL), false));
  }

  @Override
  public void perform(
      final ServerPlayerEntity player, final PlayerEntity lord, final WheelEntry chosen) {
    final ChallengeRules.Verdict verdict = verdict(player, lord);
    player.sendMessage(
        Text.translatable(
            verdict.key(),
            lord.getDisplayName(),
            ServerConfigService.current().houses().answerNights()),
        false);
    if (verdict != ChallengeRules.Verdict.DECLARED) {
      return;
    }
    final Houses houses = Houses.get(player.getServer());
    houses
        .ledBy(lord.getUuid())
        .ifPresent(
            house ->
                houses.put(
                    house.withChallenges(
                        house
                            .challenges()
                            .declared(
                                new Challenge(
                                    player.getUuid(), HouseDuels.night(player.getServer()))))));
    lord.sendMessage(
        Text.translatable(
            DECLARED_TO_LORD,
            player.getDisplayName(),
            ServerConfigService.current().houses().answerNights()),
        false);
    lord.playSoundToPlayer(SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 1.0F, 0.5F);
    player.playSoundToPlayer(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0F, 0.6F);
  }

  static ChallengeRules.Verdict verdict(final ServerPlayerEntity player, final PlayerEntity lord) {
    final Optional<House> house = Houses.get(player.getServer()).ledBy(lord.getUuid());
    final long night = HouseDuels.night(player.getServer());
    return ChallengeRules.declare(
        house.filter(led -> led.players().contains(player.getUuid())).isPresent(),
        StageRanks.of(player),
        StageRanks.of(lord),
        house.flatMap(led -> led.challenges().pending()).isPresent(),
        house.filter(led -> led.challenges().barred(player.getUuid(), night)).isPresent());
  }
}

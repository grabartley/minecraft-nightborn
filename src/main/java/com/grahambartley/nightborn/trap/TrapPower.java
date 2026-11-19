package com.grahambartley.nightborn.trap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public record TrapPower(
    float range,
    int durationTicks,
    int limit,
    int armTicks,
    float seenWithin,
    boolean stepped,
    float triggerRadius,
    boolean catchesBatsAndMist,
    int pinTicks,
    int playerPinTicks,
    float damage,
    RegistryKey<DamageType> damageType,
    List<StatusEffectInstance> effects,
    float size,
    Identifier look,
    TrapSpring spring) {

  public static final Codec<TrapPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 16.0F).fieldOf("range").forGetter(TrapPower::range),
                      Codec.intRange(20, 72000)
                          .fieldOf("duration_ticks")
                          .forGetter(TrapPower::durationTicks),
                      Codec.intRange(1, 16).fieldOf("limit").forGetter(TrapPower::limit),
                      Codec.intRange(0, 400)
                          .optionalFieldOf("arm_ticks", 0)
                          .forGetter(TrapPower::armTicks),
                      Codec.floatRange(0.0F, 64.0F)
                          .fieldOf("seen_within")
                          .forGetter(TrapPower::seenWithin),
                      Codec.BOOL.fieldOf("stepped").forGetter(TrapPower::stepped),
                      Codec.floatRange(0.0F, 8.0F)
                          .optionalFieldOf("trigger_radius", 0.0F)
                          .forGetter(TrapPower::triggerRadius),
                      Codec.BOOL
                          .optionalFieldOf("catches_bats_and_mist", true)
                          .forGetter(TrapPower::catchesBatsAndMist),
                      Codec.intRange(0, 400)
                          .optionalFieldOf("pin_ticks", 0)
                          .forGetter(TrapPower::pinTicks),
                      Codec.intRange(0, 400)
                          .optionalFieldOf("player_pin_ticks", 0)
                          .forGetter(TrapPower::playerPinTicks),
                      Codec.floatRange(0.0F, 40.0F)
                          .optionalFieldOf("damage", 0.0F)
                          .forGetter(TrapPower::damage),
                      RegistryKey.createCodec(RegistryKeys.DAMAGE_TYPE)
                          .fieldOf("damage_type")
                          .forGetter(TrapPower::damageType),
                      StatusEffectInstance.CODEC
                          .listOf()
                          .optionalFieldOf("effects", List.of())
                          .forGetter(TrapPower::effects),
                      Codec.floatRange(0.1F, 4.0F)
                          .optionalFieldOf("size", 0.5F)
                          .forGetter(TrapPower::size),
                      Identifier.CODEC.fieldOf("look").forGetter(TrapPower::look),
                      TrapSpring.CODEC.fieldOf("spring").forGetter(TrapPower::spring))
                  .apply(instance, TrapPower::new));

  public TrapPower {
    effects = List.copyOf(effects);
  }

  public int pinTicks(final boolean player) {
    return player ? playerPinTicks : pinTicks;
  }
}

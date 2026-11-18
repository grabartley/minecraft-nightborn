package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.SkillAttribute;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public record WingedFormPower(
    int durationTicks,
    List<SkillAttribute> attributes,
    float damageTaken,
    float roarRadius,
    int roarStaggerTicks,
    TagKey<EntityType<?>> roarStaggers,
    float scale,
    List<String> forcedBy) {

  public static final Codec<WingedFormPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(WingedFormPower::durationTicks),
                      SkillAttribute.CODEC
                          .listOf()
                          .optionalFieldOf("attributes", List.of())
                          .forGetter(WingedFormPower::attributes),
                      Codec.floatRange(0.2F, 1.0F)
                          .optionalFieldOf("damage_taken", 1.0F)
                          .forGetter(WingedFormPower::damageTaken),
                      Codec.floatRange(0.0F, MobPowerUse.MAX_RANGE)
                          .optionalFieldOf("roar_radius", 0.0F)
                          .forGetter(WingedFormPower::roarRadius),
                      Codec.intRange(0, 600)
                          .optionalFieldOf("roar_stagger_ticks", 0)
                          .forGetter(WingedFormPower::roarStaggerTicks),
                      TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE)
                          .fieldOf("roar_staggers")
                          .forGetter(WingedFormPower::roarStaggers),
                      Codec.floatRange(1.0F, 6.0F)
                          .optionalFieldOf("scale", 1.0F)
                          .forGetter(WingedFormPower::scale),
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("forced_by", List.of())
                          .forGetter(WingedFormPower::forcedBy))
                  .apply(instance, WingedFormPower::new));

  public WingedFormPower {
    attributes = List.copyOf(attributes);
    forcedBy = List.copyOf(forcedBy);
  }
}

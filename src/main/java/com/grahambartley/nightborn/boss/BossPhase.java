package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;

public record BossPhase(
    String name,
    int health,
    BossBar.Color color,
    PhaseStance stance,
    List<Identifier> attacks,
    int gapTicks,
    Optional<Identifier> music,
    PhaseBody body,
    PhaseDefence defence,
    Optional<PhaseVoice> voice,
    Optional<PhaseFlight> flight,
    Optional<PhaseAmbience> ambience,
    PhaseLook look) {

  static final String KEY = "nightborn.boss.phase.";

  static final Codec<BossBar.Color> COLOR =
      Codec.STRING.xmap(BossBar.Color::byName, BossBar.Color::getName);

  public static final Codec<BossPhase> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.fieldOf("name").forGetter(BossPhase::name),
                      Codec.intRange(1, 1000).fieldOf("health").forGetter(BossPhase::health),
                      COLOR.optionalFieldOf("color", BossBar.Color.RED).forGetter(BossPhase::color),
                      PhaseStance.CODEC
                          .optionalFieldOf("stance", PhaseStance.DEFAULT)
                          .forGetter(BossPhase::stance),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("attacks", List.of())
                          .forGetter(BossPhase::attacks),
                      Codec.intRange(1, 1200)
                          .optionalFieldOf("gap_ticks", 60)
                          .forGetter(BossPhase::gapTicks),
                      Identifier.CODEC.optionalFieldOf("music").forGetter(BossPhase::music),
                      PhaseBody.CODEC
                          .optionalFieldOf("body", PhaseBody.PLAIN)
                          .forGetter(BossPhase::body),
                      PhaseDefence.CODEC
                          .optionalFieldOf("defence", PhaseDefence.NONE)
                          .forGetter(BossPhase::defence),
                      PhaseVoice.CODEC.optionalFieldOf("voice").forGetter(BossPhase::voice),
                      PhaseFlight.CODEC.optionalFieldOf("flight").forGetter(BossPhase::flight),
                      PhaseAmbience.CODEC
                          .optionalFieldOf("ambience")
                          .forGetter(BossPhase::ambience),
                      PhaseLook.CODEC
                          .optionalFieldOf("look", PhaseLook.PLAIN)
                          .forGetter(BossPhase::look))
                  .apply(instance, BossPhase::new));

  public BossPhase {
    attacks = List.copyOf(attacks);
  }

  public String translationKey() {
    return KEY + name;
  }
}

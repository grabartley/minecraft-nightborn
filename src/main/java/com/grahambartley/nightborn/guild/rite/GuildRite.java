package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public record GuildRite(Timing timing, Clips clips, Sounds sounds, Light light, Reach reach) {

  public static final RegistryKey<Registry<GuildRite>> KEY =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "guild_rite"));
  public static final Identifier SENDING = Identifier.of(Nightborn.MOD_ID, "sending");

  static final int LONGEST_TICKS = 1200;
  static final int WIDEST_SEARCH = 8;

  public record Timing(int castTicks, int leaveTicks, int settleTicks, int waitTicks) {

    static final Codec<Timing> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.intRange(1, LONGEST_TICKS)
                            .fieldOf("cast_ticks")
                            .forGetter(Timing::castTicks),
                        Codec.intRange(1, LONGEST_TICKS)
                            .fieldOf("leave_ticks")
                            .forGetter(Timing::leaveTicks),
                        Codec.intRange(0, LONGEST_TICKS)
                            .fieldOf("settle_ticks")
                            .forGetter(Timing::settleTicks),
                        Codec.intRange(0, LONGEST_TICKS)
                            .fieldOf("wait_ticks")
                            .forGetter(Timing::waitTicks))
                    .apply(instance, Timing::new));
  }

  public record Clips(String cast, String leave, String arrive) {

    static final Codec<Clips> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.STRING.fieldOf("cast").forGetter(Clips::cast),
                        Codec.STRING.fieldOf("leave").forGetter(Clips::leave),
                        Codec.STRING.fieldOf("arrive").forGetter(Clips::arrive))
                    .apply(instance, Clips::new));
  }

  public record Cue(Identifier sound, float volume, float pitch, int delay) {

    static final Codec<Cue> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Identifier.CODEC.fieldOf("sound").forGetter(Cue::sound),
                        Codec.floatRange(0.0F, 4.0F)
                            .optionalFieldOf("volume", 1.0F)
                            .forGetter(Cue::volume),
                        Codec.floatRange(0.5F, 2.0F)
                            .optionalFieldOf("pitch", 1.0F)
                            .forGetter(Cue::pitch),
                        Codec.intRange(0, LONGEST_TICKS)
                            .optionalFieldOf("delay", 0)
                            .forGetter(Cue::delay))
                    .apply(instance, Cue::new));

    public static List<Cue> due(final List<Cue> cues, final int ticks) {
      return cues.stream().filter(cue -> cue.delay() == ticks).toList();
    }
  }

  public record Sounds(List<Cue> cast, List<Cue> leave, List<Cue> arrive) {

    static final Codec<Sounds> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Cue.CODEC.listOf().fieldOf("cast").forGetter(Sounds::cast),
                        Cue.CODEC.listOf().fieldOf("leave").forGetter(Sounds::leave),
                        Cue.CODEC.listOf().fieldOf("arrive").forGetter(Sounds::arrive))
                    .apply(instance, Sounds::new));
  }

  public record Light(Identifier look, float radius, float rise, int motes) {

    static final Codec<Light> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Identifier.CODEC.fieldOf("look").forGetter(Light::look),
                        Codec.floatRange(0.5F, 8.0F).fieldOf("radius").forGetter(Light::radius),
                        Codec.floatRange(0.0F, 8.0F).fieldOf("rise").forGetter(Light::rise),
                        Codec.intRange(0, 64).fieldOf("motes").forGetter(Light::motes))
                    .apply(instance, Light::new));
  }

  public record Reach(
      double breakDistance, int searchRadius, int searchRise, double householdReach) {

    static final Codec<Reach> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.doubleRange(0.5, 16.0)
                            .fieldOf("break_distance")
                            .forGetter(Reach::breakDistance),
                        Codec.intRange(0, WIDEST_SEARCH)
                            .fieldOf("search_radius")
                            .forGetter(Reach::searchRadius),
                        Codec.intRange(0, WIDEST_SEARCH)
                            .fieldOf("search_rise")
                            .forGetter(Reach::searchRise),
                        Codec.doubleRange(0.0, 32.0)
                            .fieldOf("household_reach")
                            .forGetter(Reach::householdReach))
                    .apply(instance, Reach::new));
  }

  public static final Codec<GuildRite> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Timing.CODEC.fieldOf("timing").forGetter(GuildRite::timing),
                      Clips.CODEC.fieldOf("clips").forGetter(GuildRite::clips),
                      Sounds.CODEC.fieldOf("sounds").forGetter(GuildRite::sounds),
                      Light.CODEC.fieldOf("light").forGetter(GuildRite::light),
                      Reach.CODEC.fieldOf("reach").forGetter(GuildRite::reach))
                  .apply(instance, GuildRite::new));

  public static Optional<GuildRite> sending(final DynamicRegistryManager registries) {
    return registries.getOptional(KEY).flatMap(rites -> rites.getOrEmpty(SENDING));
  }
}

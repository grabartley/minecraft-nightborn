package com.grahambartley.nightborn.mob.household;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public record Temperament(
    int weight, Map<Hobby, Integer> hobbies, int temper, IntProvider pastimeSeconds, int mourning) {

  public static final int HEAVIEST = 1000;
  public static final int LONGEST_PASTIME = 600;
  public static final int LONGEST_MOURNING = 1200;

  public static final Temperament EVEN =
      new Temperament(
          1,
          Map.of(Hobby.SIT, 1, Hobby.READ, 1, Hobby.CONVERSE, 1, Hobby.STROLL, 1),
          20,
          UniformIntProvider.create(30, 90),
          60);

  public static final Codec<Temperament> CODEC =
      RecordCodecBuilder.<Temperament>create(
              instance ->
                  instance
                      .group(
                          Codec.intRange(1, HEAVIEST)
                              .fieldOf("weight")
                              .forGetter(Temperament::weight),
                          Codec.unboundedMap(Hobby.CODEC, Codec.intRange(0, HEAVIEST))
                              .fieldOf("hobbies")
                              .forGetter(Temperament::hobbies),
                          Codec.intRange(0, 100).fieldOf("temper").forGetter(Temperament::temper),
                          IntProvider.createValidatingCodec(1, LONGEST_PASTIME)
                              .fieldOf("pastime_seconds")
                              .forGetter(Temperament::pastimeSeconds),
                          Codec.intRange(0, LONGEST_MOURNING)
                              .fieldOf("mourning_seconds")
                              .forGetter(Temperament::mourning))
                      .apply(instance, Temperament::new))
          .validate(Temperament::check);

  public Temperament {
    hobbies = Map.copyOf(hobbies);
  }

  private static DataResult<Temperament> check(final Temperament temperament) {
    return temperament.hobbies().values().stream().mapToInt(Integer::intValue).sum() > 0
        ? DataResult.success(temperament)
        : DataResult.error(() -> "a temperament needs at least one hobby with weight");
  }
}

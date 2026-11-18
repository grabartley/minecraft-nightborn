package com.grahambartley.nightborn.village;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

public record VillageRecord(
    int suspicion,
    long settledNight,
    long incidentNight,
    long charmNight,
    long giftNight,
    int giftsTonight,
    long sentNight,
    List<UUID> faces,
    List<TakenNeighbour> taken,
    List<BlockPos> wards) {

  public static final int MOST = 100;
  static final long NEVER = Long.MIN_VALUE;

  public static final Codec<VillageRecord> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, MOST)
                          .fieldOf("suspicion")
                          .forGetter(VillageRecord::suspicion),
                      Codec.LONG.fieldOf("settled_night").forGetter(VillageRecord::settledNight),
                      Codec.LONG.fieldOf("incident_night").forGetter(VillageRecord::incidentNight),
                      Codec.LONG.fieldOf("charm_night").forGetter(VillageRecord::charmNight),
                      Codec.LONG.fieldOf("gift_night").forGetter(VillageRecord::giftNight),
                      Codec.INT.fieldOf("gifts_tonight").forGetter(VillageRecord::giftsTonight),
                      Codec.LONG.fieldOf("sent_night").forGetter(VillageRecord::sentNight),
                      Uuids.INT_STREAM_CODEC
                          .listOf()
                          .fieldOf("faces")
                          .forGetter(VillageRecord::faces),
                      TakenNeighbour.CODEC
                          .listOf()
                          .fieldOf("taken")
                          .forGetter(VillageRecord::taken),
                      BlockPos.CODEC.listOf().fieldOf("wards").forGetter(VillageRecord::wards))
                  .apply(instance, VillageRecord::new));

  public VillageRecord {
    suspicion = Math.clamp(suspicion, 0, MOST);
    faces = suspicion == 0 ? List.of() : List.copyOf(faces);
    taken = List.copyOf(taken);
    wards = List.copyOf(wards);
  }

  public static VillageRecord fresh(final long night) {
    return new VillageRecord(
        0, night, NEVER, NEVER, NEVER, 0, NEVER, List.of(), List.of(), List.of());
  }

  public VillageRecord settled(
      final long night, final int keptNeighbours, final int perNeighbour, final int quietFade) {
    if (night <= settledNight) {
      return this;
    }
    final long passed = night - settledNight;
    final long quiet = passed - (incidentNight >= settledNight && incidentNight < night ? 1 : 0);
    final long level = suspicion + passed * keptNeighbours * perNeighbour - quiet * quietFade;
    return new VillageRecord(
        (int) Math.clamp(level, 0, MOST),
        night,
        incidentNight,
        charmNight,
        giftNight,
        giftsTonight,
        sentNight,
        faces,
        taken,
        wards);
  }

  public VillageRecord raised(final int amount, final long night, final Optional<UUID> face) {
    final List<UUID> known = new ArrayList<>(faces);
    face.filter(id -> !known.contains(id)).ifPresent(known::add);
    return new VillageRecord(
        suspicion + amount,
        settledNight,
        night,
        charmNight,
        giftNight,
        giftsTonight,
        sentNight,
        known,
        taken,
        wards);
  }

  public VillageRecord charmed(final long night, final int fade) {
    if (charmNight == night) {
      return this;
    }
    return new VillageRecord(
        suspicion - fade,
        settledNight,
        incidentNight,
        night,
        giftNight,
        giftsTonight,
        sentNight,
        faces,
        taken,
        wards);
  }

  public VillageRecord gifted(final long night, final int fade, final int perNight) {
    final int given = giftNight == night ? giftsTonight : 0;
    if (given >= perNight) {
      return this;
    }
    return new VillageRecord(
        suspicion - fade,
        settledNight,
        incidentNight,
        charmNight,
        night,
        given + 1,
        sentNight,
        faces,
        taken,
        wards);
  }

  public boolean knows(final UUID player) {
    return faces.contains(player);
  }

  public VillageRecord withTaken(final TakenNeighbour neighbour) {
    final List<TakenNeighbour> all = new ArrayList<>(taken);
    if (!all.contains(neighbour)) {
      all.add(neighbour);
    }
    return withTakenList(all);
  }

  public VillageRecord keepingTaken(final Predicate<TakenNeighbour> kept) {
    return withTakenList(taken.stream().filter(kept).toList());
  }

  private VillageRecord withTakenList(final List<TakenNeighbour> all) {
    return new VillageRecord(
        suspicion,
        settledNight,
        incidentNight,
        charmNight,
        giftNight,
        giftsTonight,
        sentNight,
        faces,
        all,
        wards);
  }

  public VillageRecord withWards(final List<BlockPos> placed) {
    return new VillageRecord(
        suspicion,
        settledNight,
        incidentNight,
        charmNight,
        giftNight,
        giftsTonight,
        sentNight,
        faces,
        taken,
        placed);
  }

  public VillageRecord sent(final long night) {
    return new VillageRecord(
        suspicion,
        settledNight,
        incidentNight,
        charmNight,
        giftNight,
        giftsTonight,
        night,
        faces,
        taken,
        wards);
  }

  public boolean idle() {
    return suspicion == 0 && taken.isEmpty() && wards.isEmpty();
  }
}

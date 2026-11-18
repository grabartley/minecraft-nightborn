package com.grahambartley.nightborn.house;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.GlobalPos;

public record House(
    UUID lord,
    Optional<GlobalPos> seat,
    Optional<GlobalPos> anchor,
    List<UUID> players,
    List<UUID> npcs,
    Optional<GlobalPos> store,
    HouseIdentity identity,
    HouseChallenges challenges,
    Map<UUID, Integer> ranks) {

  public static final Codec<House> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.CODEC.fieldOf("lord").forGetter(House::lord),
                      GlobalPos.CODEC.optionalFieldOf("seat").forGetter(House::seat),
                      GlobalPos.CODEC.optionalFieldOf("anchor").forGetter(House::anchor),
                      Uuids.CODEC
                          .listOf()
                          .optionalFieldOf("players", List.of())
                          .forGetter(House::players),
                      Uuids.CODEC
                          .listOf()
                          .optionalFieldOf("npcs", List.of())
                          .forGetter(House::npcs),
                      GlobalPos.CODEC.optionalFieldOf("store").forGetter(House::store),
                      HouseIdentity.CODEC
                          .optionalFieldOf("identity", HouseIdentity.NONE)
                          .forGetter(House::identity),
                      HouseChallenges.CODEC
                          .optionalFieldOf("challenges", HouseChallenges.NONE)
                          .forGetter(House::challenges),
                      Codec.unboundedMap(Uuids.STRING_CODEC, Codec.INT)
                          .optionalFieldOf("ranks", Map.of())
                          .forGetter(House::ranks))
                  .apply(instance, House::new));

  public House {
    players = List.copyOf(players);
    npcs = List.copyOf(npcs);
    ranks = Map.copyOf(ranks);
  }

  public static House founded(final UUID lord, final GlobalPos anchor) {
    return new House(
        lord,
        Optional.empty(),
        Optional.of(anchor),
        List.of(),
        List.of(),
        Optional.empty(),
        HouseIdentity.NONE,
        HouseChallenges.NONE,
        Map.of());
  }

  public int household() {
    return size() + 1;
  }

  public int size() {
    return players.size() + npcs.size();
  }

  public boolean hasMember(final UUID member) {
    return players.contains(member) || npcs.contains(member);
  }

  public boolean belongs(final UUID vampire) {
    return lord.equals(vampire) || hasMember(vampire);
  }

  public Stream<UUID> everyone() {
    return Stream.concat(Stream.of(lord), Stream.concat(players.stream(), npcs.stream()));
  }

  public Optional<GlobalPos> place(final Optional<GlobalPos> lordsCoffin) {
    return seat.or(() -> lordsCoffin).or(() -> anchor);
  }

  public int rank(final UUID member) {
    return ranks.getOrDefault(member, 0);
  }

  public House withPlayer(final UUID player) {
    return players.contains(player)
        ? this
        : new House(
            lord, seat, anchor, adding(players, player), npcs, store, identity, challenges, ranks);
  }

  public House withNpc(final UUID npc) {
    return npcs.contains(npc)
        ? this
        : new House(
            lord, seat, anchor, players, adding(npcs, npc), store, identity, challenges, ranks);
  }

  public House without(final UUID member) {
    final Map<UUID, Integer> kept = new HashMap<>(ranks);
    kept.remove(member);
    return new House(
        lord,
        seat,
        anchor,
        players.stream().filter(uuid -> !uuid.equals(member)).toList(),
        npcs.stream().filter(uuid -> !uuid.equals(member)).toList(),
        store,
        identity,
        challenges.forgetting(member),
        kept);
  }

  public House ranked(final UUID member, final int rank) {
    if (Optional.ofNullable(ranks.get(member)).filter(known -> known == rank).isPresent()) {
      return this;
    }
    final Map<UUID, Integer> known = new HashMap<>(ranks);
    known.put(member, rank);
    return new House(lord, seat, anchor, players, npcs, store, identity, challenges, known);
  }

  public House ledBy(final UUID heir, final boolean formerLordStays) {
    final List<UUID> members = players.stream().filter(uuid -> !uuid.equals(heir)).toList();
    final House passed =
        new House(
            heir,
            seat,
            anchor,
            formerLordStays ? Stream.concat(Stream.of(lord), members.stream()).toList() : members,
            npcs,
            store,
            identity,
            challenges.forgetting(heir).settled(),
            ranks);
    return formerLordStays ? passed : passed.without(lord);
  }

  public House withIdentity(final HouseIdentity named) {
    return new House(lord, seat, anchor, players, npcs, store, named, challenges, ranks);
  }

  public House withChallenges(final HouseChallenges next) {
    return new House(lord, seat, anchor, players, npcs, store, identity, next, ranks);
  }

  public House anchoredAt(final GlobalPos at) {
    return new House(
        lord, seat, Optional.of(at), players, npcs, store, identity, challenges, ranks);
  }

  public House seatedAt(final Optional<GlobalPos> structure) {
    return new House(lord, structure, anchor, players, npcs, store, identity, challenges, ranks);
  }

  public House storeToggled(final GlobalPos at) {
    return new House(
        lord,
        seat,
        anchor,
        players,
        npcs,
        store.filter(at::equals).isPresent() ? Optional.empty() : Optional.of(at),
        identity,
        challenges,
        ranks);
  }

  private static List<UUID> adding(final List<UUID> list, final UUID added) {
    final List<UUID> grown = new ArrayList<>(list);
    grown.add(added);
    return grown;
  }
}

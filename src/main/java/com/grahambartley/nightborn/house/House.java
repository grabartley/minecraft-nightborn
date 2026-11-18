package com.grahambartley.nightborn.house;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
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
    Optional<GlobalPos> store) {

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
                      GlobalPos.CODEC.optionalFieldOf("store").forGetter(House::store))
                  .apply(instance, House::new));

  public House {
    players = List.copyOf(players);
    npcs = List.copyOf(npcs);
  }

  public static House founded(final UUID lord, final GlobalPos anchor) {
    return new House(
        lord, Optional.empty(), Optional.of(anchor), List.of(), List.of(), Optional.empty());
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

  public House withPlayer(final UUID player) {
    return players.contains(player)
        ? this
        : new House(lord, seat, anchor, adding(players, player), npcs, store);
  }

  public House withNpc(final UUID npc) {
    return npcs.contains(npc)
        ? this
        : new House(lord, seat, anchor, players, adding(npcs, npc), store);
  }

  public House without(final UUID member) {
    return new House(
        lord,
        seat,
        anchor,
        players.stream().filter(uuid -> !uuid.equals(member)).toList(),
        npcs.stream().filter(uuid -> !uuid.equals(member)).toList(),
        store);
  }

  public House anchoredAt(final GlobalPos at) {
    return new House(lord, seat, Optional.of(at), players, npcs, store);
  }

  public House seatedAt(final Optional<GlobalPos> structure) {
    return new House(lord, structure, anchor, players, npcs, store);
  }

  public House storeToggled(final GlobalPos at) {
    return new House(
        lord,
        seat,
        anchor,
        players,
        npcs,
        store.filter(at::equals).isPresent() ? Optional.empty() : Optional.of(at));
  }

  private static List<UUID> adding(final List<UUID> list, final UUID added) {
    final List<UUID> grown = new ArrayList<>(list);
    grown.add(added);
    return grown;
  }
}

package com.grahambartley.nightborn.house;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.GlobalPos;

public final class HouseBook {

  private final Map<UUID, House> byLord = new LinkedHashMap<>();

  public Optional<House> ledBy(final UUID lord) {
    return Optional.ofNullable(byLord.get(lord));
  }

  public Optional<House> of(final UUID vampire) {
    return ledBy(vampire)
        .or(() -> byLord.values().stream().filter(house -> house.hasMember(vampire)).findFirst());
  }

  public Optional<House> holding(final GlobalPos seat) {
    return byLord.values().stream()
        .filter(house -> house.seat().filter(seat::equals).isPresent())
        .findFirst();
  }

  public boolean seatNear(final GlobalPos pos, final int reach) {
    return byLord.values().stream()
        .flatMap(house -> house.seat().stream())
        .anyMatch(
            seat ->
                seat.dimension().equals(pos.dimension())
                    && seat.pos().isWithinDistance(pos.pos(), reach));
  }

  public void put(final House house) {
    byLord.put(house.lord(), house);
  }

  public void remove(final UUID lord) {
    byLord.remove(lord);
  }

  public Collection<House> all() {
    return List.copyOf(byLord.values());
  }
}

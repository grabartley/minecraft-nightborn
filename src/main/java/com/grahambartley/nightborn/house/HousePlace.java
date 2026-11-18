package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.coffin.CoffinOwnersState;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;

public final class HousePlace {

  private HousePlace() {}

  public static Optional<GlobalPos> of(final MinecraftServer server, final House house) {
    final Optional<GlobalPos> lordsCoffin =
        house.seat().isPresent()
            ? Optional.empty()
            : CoffinOwnersState.get(server).coffinOf(house.lord());
    final Optional<GlobalPos> place = house.place(lordsCoffin);
    if (house.seat().isEmpty() && place.isPresent() && !place.equals(house.anchor())) {
      Houses.get(server).put(house.anchoredAt(place.get()));
    }
    return place;
  }
}

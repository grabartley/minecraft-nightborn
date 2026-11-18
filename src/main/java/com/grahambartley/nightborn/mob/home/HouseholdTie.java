package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.servant.Stance;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class HouseholdTie {

  static final String HEAD_KEY = "HouseholdHead";
  static final String LORD_KEY = "HouseLord";
  static final String STANCE_KEY = "HouseStance";
  static final String POST_KEY = "HousePost";
  static final String PROVIDER_KEY = "BringsBlood";
  static final String ERRAND_KEY = "ErrandNight";
  static final String FED_KEY = "ErrandFedNight";
  static final String CARRIED_KEY = "CarriedBlood";

  private boolean head;
  @Nullable private UUID lord;
  @Nullable private Stance stance;
  @Nullable private BlockPos post;
  private boolean provider;
  private long errandNight = NightRoutine.NO_NIGHT;
  private long fedNight = NightRoutine.NO_NIGHT;
  @Nullable private BloodQuality carried;

  public void becomeHead() {
    head = true;
  }

  public boolean head() {
    return head;
  }

  public Optional<UUID> lord() {
    return Optional.ofNullable(lord);
  }

  public void join(final UUID newLord) {
    lord = newLord;
    head = false;
    stand(null, null);
  }

  public void leave() {
    lord = null;
    stand(null, null);
  }

  public Optional<Stance> stance() {
    return Optional.ofNullable(stance);
  }

  public Optional<BlockPos> post() {
    return Optional.ofNullable(post);
  }

  public void order(final Stance next, final BlockPos at) {
    stand(next, at.toImmutable());
  }

  public void sendOut(final long night, final boolean providing) {
    stand(null, null);
    provider = providing;
    errandNight = night;
  }

  public boolean provider() {
    return provider;
  }

  public boolean onErrand(final long night) {
    return errandNight != NightRoutine.NO_NIGHT && errandNight == night && fedNight != night;
  }

  public boolean sentOutOn(final long night) {
    return errandNight == night;
  }

  public void fed(
      final long night, final Optional<BloodQuality> drawn, final boolean houseStoresBlood) {
    fedNight = night;
    if (houseStoresBlood && carried == null) {
      carried = drawn.orElse(null);
    }
  }

  public Optional<BloodQuality> carried() {
    return Optional.ofNullable(carried);
  }

  public void delivered() {
    carried = null;
  }

  public void write(final NbtCompound nbt) {
    nbt.putBoolean(HEAD_KEY, head);
    if (lord != null) {
      nbt.putUuid(LORD_KEY, lord);
    }
    if (stance != null) {
      nbt.putString(STANCE_KEY, stance.name());
    }
    if (post != null) {
      nbt.putLong(POST_KEY, post.asLong());
    }
    nbt.putBoolean(PROVIDER_KEY, provider);
    nbt.putLong(ERRAND_KEY, errandNight);
    nbt.putLong(FED_KEY, fedNight);
    if (carried != null) {
      nbt.putString(CARRIED_KEY, carried.asString());
    }
  }

  public void read(final NbtCompound nbt) {
    head = nbt.getBoolean(HEAD_KEY);
    lord = nbt.containsUuid(LORD_KEY) ? nbt.getUuid(LORD_KEY) : null;
    stance =
        nbt.contains(STANCE_KEY, NbtElement.STRING_TYPE)
            ? Stance.byName(nbt.getString(STANCE_KEY))
            : null;
    post =
        nbt.contains(POST_KEY, NbtElement.LONG_TYPE)
            ? BlockPos.fromLong(nbt.getLong(POST_KEY))
            : null;
    provider = nbt.getBoolean(PROVIDER_KEY);
    errandNight =
        nbt.contains(ERRAND_KEY, NbtElement.LONG_TYPE)
            ? nbt.getLong(ERRAND_KEY)
            : NightRoutine.NO_NIGHT;
    fedNight =
        nbt.contains(FED_KEY, NbtElement.LONG_TYPE) ? nbt.getLong(FED_KEY) : NightRoutine.NO_NIGHT;
    carried =
        nbt.contains(CARRIED_KEY, NbtElement.STRING_TYPE)
            ? Arrays.stream(BloodQuality.values())
                .filter(quality -> quality.asString().equals(nbt.getString(CARRIED_KEY)))
                .findFirst()
                .orElse(null)
            : null;
  }

  private void stand(@Nullable final Stance next, @Nullable final BlockPos at) {
    stance = next;
    post = at;
    provider = false;
    errandNight = NightRoutine.NO_NIGHT;
  }
}

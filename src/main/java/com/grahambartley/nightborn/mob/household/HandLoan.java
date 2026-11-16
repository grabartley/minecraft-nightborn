package com.grahambartley.nightborn.mob.household;

import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

public final class HandLoan {

  static final String KEY = "LentHand";
  static final String STACK_KEY = "Kept";
  static final String CHANCE_KEY = "DropChance";

  public record Kept(ItemStack stack, float dropChance) {}

  @Nullable private Kept kept;

  public void lend(final ItemStack held, final float dropChance) {
    if (kept == null) {
      kept = new Kept(held.copy(), dropChance);
    }
  }

  public Optional<Kept> take() {
    final Optional<Kept> taken = Optional.ofNullable(kept);
    kept = null;
    return taken;
  }

  public boolean lent() {
    return kept != null;
  }

  public void write(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    if (kept == null) {
      return;
    }
    final NbtCompound loan = new NbtCompound();
    loan.put(STACK_KEY, kept.stack().encodeAllowEmpty(registries));
    loan.putFloat(CHANCE_KEY, kept.dropChance());
    nbt.put(KEY, loan);
  }

  public void read(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    kept =
        nbt.contains(KEY, NbtElement.COMPOUND_TYPE)
            ? new Kept(
                ItemStack.fromNbtOrEmpty(registries, nbt.getCompound(KEY).getCompound(STACK_KEY)),
                nbt.getCompound(KEY).getFloat(CHANCE_KEY))
            : null;
  }
}

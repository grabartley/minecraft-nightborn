package com.grahambartley.nightborn.decor;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class WeaponRackBlockEntity extends BlockEntity {

  private final DefaultedList<ItemStack> hooks =
      DefaultedList.ofSize(RackHooks.COUNT, ItemStack.EMPTY);

  public WeaponRackBlockEntity(final BlockPos pos, final BlockState state) {
    super(DecorBlockEntities.WEAPON_RACK, pos, state);
  }

  public ItemStack get(final int hook) {
    return this.hooks.get(hook);
  }

  public List<ItemStack> held() {
    return this.hooks.stream().filter(stack -> !stack.isEmpty()).toList();
  }

  public boolean hang(final int hook, final ItemStack stack) {
    if (stack.isEmpty() || !this.hooks.get(hook).isEmpty()) {
      return false;
    }
    this.hooks.set(hook, stack);
    this.changed();
    return true;
  }

  public ItemStack take(final int hook) {
    final ItemStack taken = this.hooks.set(hook, ItemStack.EMPTY);
    if (!taken.isEmpty()) {
      this.changed();
    }
    return taken;
  }

  private void changed() {
    this.markDirty();
    if (this.world != null) {
      final BlockState state = this.getCachedState();
      this.world.updateListeners(this.pos, state, state, Block.NOTIFY_LISTENERS);
    }
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    for (int hook = 0; hook < RackHooks.COUNT; hook++) {
      this.hooks.set(hook, ItemStack.EMPTY);
    }
    Inventories.readNbt(nbt, this.hooks, registries);
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    Inventories.writeNbt(nbt, this.hooks, true, registries);
  }

  @Override
  public Packet<ClientPlayPacketListener> toUpdatePacket() {
    return BlockEntityUpdateS2CPacket.create(this);
  }

  @Override
  public NbtCompound toInitialChunkDataNbt(final RegistryWrapper.WrapperLookup registries) {
    return this.createNbt(registries);
  }
}

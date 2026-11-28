package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.network.NameServantPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

public final class NamingScreen extends NameEntryScreen<NamingLayout> {

  private final int entityId;

  public NamingScreen(final int entityId, final String suggestion) {
    super(
        Text.translatable("nightborn.naming.title"),
        Text.translatable("nightborn.naming.field"),
        suggestion);
    this.entityId = entityId;
  }

  @Override
  protected NamingLayout layout(final int screenWidth, final int screenHeight) {
    return NamingLayout.of(screenWidth, screenHeight);
  }

  @Override
  protected void submit(final String name) {
    ClientPlayNetworking.send(new NameServantPayload(entityId, name));
  }
}

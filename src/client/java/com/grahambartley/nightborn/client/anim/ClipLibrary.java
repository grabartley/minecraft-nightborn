package com.grahambartley.nightborn.client.anim;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.VampireLook;
import java.io.IOException;
import java.io.Reader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public final class ClipLibrary implements SimpleSynchronousResourceReloadListener {

  static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "vampire_clips");
  static final Identifier PLAYER_RIG =
      Identifier.of(Nightborn.MOD_ID, "geo/entity/vampire_male.geo.json");

  private static volatile ClipSet current = ClipSet.EMPTY;

  public static void register() {
    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
        .registerReloadListener(new ClipLibrary());
  }

  public static ClipSet current() {
    return current;
  }

  @Override
  public Identifier getFabricId() {
    return ID;
  }

  @Override
  public void reload(final ResourceManager manager) {
    try {
      current =
          new ClipSet(
              ClipFile.read(json(manager, VampireLook.animation())),
              RigGeometry.read(json(manager, PLAYER_RIG)));
    } catch (final IOException | RuntimeException e) {
      Nightborn.LOGGER.error("Could not load the vampire animation clips", e);
      current = ClipSet.EMPTY;
    }
  }

  private static JsonObject json(final ResourceManager manager, final Identifier id)
      throws IOException {
    try (Reader reader = manager.getResourceOrThrow(id).getReader()) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    }
  }
}

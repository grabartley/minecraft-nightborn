package com.grahambartley.nightborn.client.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grahambartley.nightborn.config.ConfigFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ClientStateJson {

  private static final String GORE = "gore";
  private static final String FORM_VIEWS = "formViews";
  private static final String SKIP_CEREMONY_CAMERA = "skipCeremonyCamera";

  public static final ConfigFormat<ClientState> FORMAT =
      ConfigFormat.of(ClientState.DEFAULTS, ClientStateJson::read, ClientStateJson::write);

  private ClientStateJson() {}

  public static ConfigFormat.Read<ClientState> read(final JsonObject root) {
    final List<String> problems = new ArrayList<>();
    final GoreLevel gore =
        readLevel(
            root.get(GORE),
            GORE,
            GoreLevel.values(),
            GoreLevel::id,
            GoreLevel::byId,
            ClientState.DEFAULTS.gore(),
            problems);
    final FormViewLevel formViews =
        readLevel(
            root.get(FORM_VIEWS),
            FORM_VIEWS,
            FormViewLevel.values(),
            FormViewLevel::id,
            FormViewLevel::byId,
            ClientState.DEFAULTS.formViews(),
            problems);
    final boolean skip = readSkip(root.get(SKIP_CEREMONY_CAMERA), problems);
    return new ConfigFormat.Read<>(new ClientState(gore, formViews, skip), problems);
  }

  public static JsonObject write(final ClientState state) {
    final JsonObject root = new JsonObject();
    root.addProperty(GORE, state.gore().id());
    root.addProperty(FORM_VIEWS, state.formViews().id());
    root.addProperty(SKIP_CEREMONY_CAMERA, state.skipCeremonyCamera());
    return root;
  }

  private static <L> L readLevel(
      final JsonElement element,
      final String name,
      final L[] levels,
      final Function<L, String> id,
      final Function<String, Optional<L>> byId,
      final L fallback,
      final List<String> problems) {
    if (element == null) {
      return fallback;
    }
    final Optional<L> level =
        element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
            ? byId.apply(element.getAsString())
            : Optional.empty();
    if (level.isEmpty()) {
      final String known = Arrays.stream(levels).map(id).collect(Collectors.joining(", "));
      problems.add(name + " is not one of " + known + ", using " + id.apply(fallback));
    }
    return level.orElse(fallback);
  }

  private static boolean readSkip(final JsonElement element, final List<String> problems) {
    final boolean fallback = ClientState.DEFAULTS.skipCeremonyCamera();
    if (element == null) {
      return fallback;
    }
    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
      return element.getAsBoolean();
    }
    problems.add(SKIP_CEREMONY_CAMERA + " is not true or false, using " + fallback);
    return fallback;
  }
}

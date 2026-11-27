package com.grahambartley.nightborn.formview;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public final class Forms {

  private static final Map<Identifier, Predicate<Entity>> FORMS = new LinkedHashMap<>();

  private Forms() {}

  public static void register(final Identifier id, final Predicate<Entity> on) {
    if (FORMS.putIfAbsent(id, on) != null) {
      throw new IllegalStateException("form " + id + " is already registered");
    }
  }

  public static boolean known(final Identifier id) {
    return FORMS.containsKey(id);
  }

  public static boolean on(final Identifier id, final Entity entity) {
    return Optional.ofNullable(FORMS.get(id)).map(form -> form.test(entity)).orElse(false);
  }
}

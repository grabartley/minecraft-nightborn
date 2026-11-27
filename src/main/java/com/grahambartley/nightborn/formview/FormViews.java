package com.grahambartley.nightborn.formview;

import com.grahambartley.nightborn.Nightborn;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class FormViews {

  public static final RegistryKey<Registry<FormView>> VIEWS =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "form_view"));

  private FormViews() {}

  public static void register() {
    DynamicRegistries.registerSynced(VIEWS, FormView.CODEC);
  }

  public static Stream<FormView> all(final DynamicRegistryManager registries) {
    return registries.getOptional(VIEWS).stream().flatMap(Registry::stream);
  }

  public static Optional<FormView> pick(
      final Stream<FormView> views, final Predicate<Identifier> formOn) {
    return views
        .filter(view -> formOn.test(view.form()))
        .max(Comparator.comparingInt(FormView::priority));
  }
}

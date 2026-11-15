package com.grahambartley.nightborn;

import com.grahambartley.nightborn.screen.NightbornSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class NightbornModMenu implements ModMenuApi {

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return NightbornSettingsScreen::new;
  }
}

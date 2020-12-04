package net.fabricmc.loader.game;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class GameProviders {

    private static ImmutableList<GameProvider> provider = ImmutableList.of();

    private GameProviders() {}

    public static List<GameProvider> create() {
        return GameProviders.provider;
    }

    public static void setProvider(GameProvider provider) {
        GameProviders.provider = ImmutableList.of(provider);
    }

}

package dev.falllazy.lazyChat.mute.storages;

import dev.falllazy.lazyChat.mute.MuteInfo;
import java.util.Map;
import java.util.UUID;

public interface MuteStorage {
    Map<UUID, MuteInfo> loadMutes();

    void saveMute(UUID uuid, MuteInfo info);

    void removeMute(UUID uuid);

    boolean isMuted(UUID uuid);

    MuteInfo getMuteInfo(UUID uuid);

    void close();
}
package dev.falllazy.lazyChat.mute;

import dev.falllazy.lazyChat.LazyChat;
import dev.falllazy.lazyChat.mute.storages.MuteStorage;
import dev.falllazy.lazyChat.mute.storages.SqliteMuteStorage;
import dev.falllazy.lazyChat.mute.storages.YamlMuteStorage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuteManager {
    private final MuteStorage storage;
    private final Map<UUID, MuteInfo> mutes = new ConcurrentHashMap<>();

    public MuteManager(LazyChat plugin, boolean yaml) {
        if (yaml) { // для старых хранилищ мутов ввиду перехода на изначально дб для мута
            storage = new YamlMuteStorage(plugin);
        } else {
            storage = new SqliteMuteStorage(plugin);
        }
        reload();
    }

    public void reload() {
        mutes.clear();
        Map<UUID, MuteInfo> loaded = storage.loadMutes();
        loaded.entrySet().removeIf(entry -> System.currentTimeMillis() > entry.getValue().expiry());
        mutes.putAll(loaded);
    }

    public void mute(UUID uuid, long durationMillis, String reason) {
        long expiry = System.currentTimeMillis() + durationMillis;
        MuteInfo info = new MuteInfo(expiry, reason);
        mutes.put(uuid, info);
        storage.saveMute(uuid, info);
    }

    public void unmute(UUID uuid) {
        mutes.remove(uuid);
        storage.removeMute(uuid);
    }

    public boolean isMuted(UUID uuid) {
        MuteInfo info = mutes.get(uuid);
        if (info == null) return false;
        if (System.currentTimeMillis() > info.expiry()) {
            unmute(uuid); // автоматическое снятие, дапс
            return false;
        }
        return true;
    }

    public String getMuteReason(UUID uuid) {
        MuteInfo info = mutes.get(uuid);
        if (info == null) return null;
        if (System.currentTimeMillis() > info.expiry()) {
            unmute(uuid);
            return null;
        }
        return info.reason();
    }

    public long getMuteExpiry(UUID uuid) {
        MuteInfo info = mutes.get(uuid);
        if (info == null) return -1;
        if (System.currentTimeMillis() > info.expiry()) {
            unmute(uuid);
            return -1;
        }
        return info.expiry();
    }

    public void close() {
        storage.close();
    }
}
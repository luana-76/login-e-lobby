package com.survivorauth.server;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;

/** Contas salvas em <mundo>/survivorauth/accounts.json (nunca contém a senha, só o hash). */
public final class AccountStore {
    public record Account(String salt, String hash, int iterations, long created) {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Account>>() {}.getType();

    private final Path file;
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public AccountStore(Path file) {
        this.file = file;
        load();
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    public boolean has(String name) {
        return accounts.containsKey(key(name));
    }

    public Account get(String name) {
        return accounts.get(key(name));
    }

    public void put(String name, Account account) {
        accounts.put(key(name), account);
        save();
    }

    public boolean remove(String name) {
        boolean removed = accounts.remove(key(name)) != null;
        if (removed) save();
        return removed;
    }

    private void load() {
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            Map<String, Account> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) accounts.putAll(loaded);
        } catch (Exception e) {
            LOGGER.error("[SurvivorAuth] Falha ao ler {}", file, e);
        }
    }

    private synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp)) {
                GSON.toJson(accounts, MAP_TYPE, writer);
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("[SurvivorAuth] Falha ao salvar {}", file, e);
        }
    }
}

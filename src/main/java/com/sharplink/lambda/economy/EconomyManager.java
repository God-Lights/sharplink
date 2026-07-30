package com.sharplink.lambda.economy;

import com.sharplink.lambda.LambdaPlugin;
import com.sharplink.lambda.currency.CurrencySettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class EconomyManager {

    private final LambdaPlugin plugin;
    private final File dataFile;
    private final Map<UUID, Long> balances = new ConcurrentHashMap<>();
    private final CurrencySettings currencySettings;

    public EconomyManager(LambdaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "balances.yml");
        this.currencySettings = new CurrencySettings(plugin.getConfig());
    }

    public CurrencySettings getCurrencySettings() {
        return currencySettings;
    }

    public void load() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : yaml.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                balances.put(uuid, yaml.getLong(key));
            } catch (IllegalArgumentException ignored) {
                // 잘못된 키는 건너뜀
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            yaml.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "잔액 데이터를 저장하지 못했습니다.", e);
        }
    }

    private void saveAsync() {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
        } else {
            save();
        }
    }

    public boolean hasAccount(UUID uuid) {
        return balances.containsKey(uuid);
    }

    public long getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0L);
    }

    public void createAccount(UUID uuid, long startingBalance) {
        balances.put(uuid, startingBalance);
        saveAsync();
    }

    public void setBalance(UUID uuid, long amount) {
        balances.put(uuid, Math.max(0, amount));
        saveAsync();
    }

    public void deposit(UUID uuid, long amount) {
        if (amount <= 0) {
            return;
        }
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean withdraw(UUID uuid, long amount) {
        if (amount <= 0) {
            return false;
        }
        long balance = getBalance(uuid);
        if (balance < amount) {
            return false;
        }
        setBalance(uuid, balance - amount);
        return true;
    }
}

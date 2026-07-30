package com.sharplink.lambda.currency;

import org.bukkit.configuration.file.FileConfiguration;

public final class CurrencySettings {

    private final String name;
    private final String symbol;
    private final String code;
    private final long startingBalance;

    public CurrencySettings(FileConfiguration config) {
        this.name = config.getString("currency.name", "람다");
        this.symbol = config.getString("currency.symbol", "λ");
        this.code = config.getString("currency.code", "RBL");
        this.startingBalance = config.getLong("starting-balance", 1000);
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCode() {
        return code;
    }

    public long getStartingBalance() {
        return startingBalance;
    }

    public String format(long amount) {
        return String.format("%,d%s (%s)", amount, symbol, code);
    }
}

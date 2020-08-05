package com.dicemc.marketplace.util.compat;

import java.util.UUID;

public interface GrandEconomyInterop {
	double getBalance(UUID uuid, Boolean isPlayer);
	boolean addToBalance(UUID uuid, double amount, Boolean isPlayer);
	boolean setBalance(UUID uuid, double amount, Boolean isPlayer);
	boolean takeFromBalance(UUID uuid, double amount, Boolean isPlayer);
}

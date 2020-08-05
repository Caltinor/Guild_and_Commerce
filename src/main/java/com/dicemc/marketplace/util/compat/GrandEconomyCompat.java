package com.dicemc.marketplace.util.compat;

import java.util.UUID;

import the_fireplace.grandeconomy.api.GrandEconomyApi;

public class GrandEconomyCompat implements GrandEconomyInterop{

	@Override
	public double getBalance(UUID uuid, Boolean isPlayer) {
		return GrandEconomyApi.getBalance(uuid, isPlayer);
	}

	@Override
	public boolean addToBalance(UUID uuid, double amount, Boolean isPlayer) {
		return GrandEconomyApi.addToBalance(uuid, amount, isPlayer);
	}

	@Override
	public boolean setBalance(UUID uuid, double amount, Boolean isPlayer) {
		return GrandEconomyApi.setBalance(uuid, amount, isPlayer);
	}

	@Override
	public boolean takeFromBalance(UUID uuid, double amount, Boolean isPlayer) {
		return GrandEconomyApi.takeFromBalance(uuid, amount, isPlayer);
	}

}

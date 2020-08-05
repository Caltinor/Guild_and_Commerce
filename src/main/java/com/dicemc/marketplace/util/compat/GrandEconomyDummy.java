package com.dicemc.marketplace.util.compat;

import java.util.UUID;

public class GrandEconomyDummy implements GrandEconomyInterop{

	@Override
	public double getBalance(UUID uuid, Boolean isPlayer) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean addToBalance(UUID uuid, double amount, Boolean isPlayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setBalance(UUID uuid, double amount, Boolean isPlayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean takeFromBalance(UUID uuid, double amount, Boolean isPlayer) {
		// TODO Auto-generated method stub
		return false;
	}

}

package com.dicemc.marketplace.core;

import java.util.UUID;

public class Account {
	public UUID owner;
	public double balance;
	
	public Account (UUID player, double amount) {
		balance = amount;
		owner = player;
	}
}

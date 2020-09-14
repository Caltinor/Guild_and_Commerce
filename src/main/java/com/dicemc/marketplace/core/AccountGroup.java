package com.dicemc.marketplace.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.datasaver.AccountSaver;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class AccountGroup {
	public List<Account> accountList;
	public String groupName;
	private final AccountSaver manager;
	
	public AccountGroup (AccountSaver manager, String name) {
		groupName = name;
		accountList = new ArrayList<Account>();
		accountList.add(new Account(Reference.NIL, 0));
		this.manager = manager;
	}
	
	public NBTTagCompound writeToNBT (NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < accountList.size(); i++) {
			NBTTagCompound snbt = new NBTTagCompound();
			snbt.setUniqueId("owner", accountList.get(i).owner);
			snbt.setDouble("balance", accountList.get(i).balance);
			list.appendTag(snbt);
		}
		System.out.println("write "+list.tagCount());
		nbt.setTag(groupName, list);
		return nbt;
	}
	public void readFromNBT(NBTTagList nbt) {
		System.out.println("read "+nbt.tagCount());
		accountList.clear();
		for (int i = 0; i < nbt.tagCount(); i++) {
			accountList.add(new Account(nbt.getCompoundTagAt(i).getUniqueId("owner"), nbt.getCompoundTagAt(i).getDouble("balance")));
		}
	}
	
	public String addAccount (UUID owner, double amount) {
		if (accountExists(owner)) return "An account already exists for this entity";
		else if (Main.useGrandEconomy) {
			Main.interop.setBalance(owner, amount, true);
			return "Grand Economy Used";
		}
		else {
			accountList.add(new Account(owner, amount));
			manager.markDirty();
			return "Account added for entity:" + owner.toString();
		}
	}
	
	public void removeAccount (UUID owner) {
		if (Main.useGrandEconomy) return;
		for (int i = 0; i < accountList.size(); i++) {
			if (accountList.get(i).owner.equals(owner)) {
				System.out.println("Guild Transfer to Server on delete $"+String.valueOf(transferPlayers(accountList.get(i).owner, accountList.get(0).owner, accountList.get(i).balance)));
				accountList.remove(i);
			}
		}
	}
	
	public boolean accountExists(UUID player) {
		if (Main.useGrandEconomy) {if (Main.interop.getBalance(player, true) != 0) return true;}
		else if (getBalance(player) != -1337) {return true;}
		return false;
	}
	
	public double getBalance (UUID player) {
		if (Main.useGrandEconomy) {
			return Main.interop.getBalance(player, true);			
		}
		for (int i = 0; i < accountList.size(); i++) {
			if (accountList.get(i).owner.equals(player)) {return accountList.get(i).balance;}
		}
		return -1337;
	}
	
	public void setBalance (UUID player, double amount) {
		if (Main.useGrandEconomy) {
			Main.interop.setBalance(player, amount, true);
			return;
		}
		for (int i = 0; i < accountList.size(); i++) {
			if (accountList.get(i).owner.equals(player)) {
				accountList.get(i).balance = amount;
				manager.markDirty();
				break;
			}
		}
	}
	
	public void addBalance (UUID player, double amount) {
		if (Main.useGrandEconomy && amount >= 0) {
			Main.interop.addToBalance(player, amount, true);
			return;
		}
		if (Main.useGrandEconomy && amount < 0) {
			Main.interop.takeFromBalance(player, Math.abs(amount), true);
			return;
		}
		for (int i = 0; i < accountList.size(); i++) {
			if (accountList.get(i).owner.equals(player)) {
				accountList.get(i).balance += amount;
				manager.markDirty();
				break;
			}
		}
	}
	
	public boolean transferPlayers (UUID fromPlayer, UUID toPlayer, double amount) {
		if (Main.useGrandEconomy) {
			if (Main.interop.takeFromBalance(fromPlayer, amount, true)) {
				Main.interop.addToBalance(toPlayer, amount, true);
			}
		}
		if (getBalance(fromPlayer) >= amount) {
			addBalance(fromPlayer, (-1 * amount));
			addBalance(toPlayer, amount);
			manager.markDirty();
			return true;
		}
		return false;
	}
}

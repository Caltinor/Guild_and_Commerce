package com.dicemc.marketplace.util.datasaver;

import com.dicemc.marketplace.core.AccountGroup;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class AccountSaver extends WorldSavedData {
	private static final String DATA_NAME = Reference.MOD_ID + "_AccountsData";
	public final AccountGroup PLAYERS = new AccountGroup(this, "Player Accounts", true);
	public final AccountGroup GUILDS = new AccountGroup(this, "Guild Accounts", false);

	public AccountSaver(String name) { super(name); }
	
	public AccountSaver() {super(DATA_NAME); }
	
	public AccountGroup getPlayers() {return PLAYERS;}
	
	public AccountGroup getGuilds() {return GUILDS;}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		PLAYERS.readFromNBT(nbt.getTagList("playeraccounts", Constants.NBT.TAG_COMPOUND));
		GUILDS.readFromNBT(nbt.getTagList("guildaccounts", Constants.NBT.TAG_COMPOUND));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < PLAYERS.accountList.size(); i++) {
			list.appendTag(PLAYERS.writeToNBT(new NBTTagCompound(), i));
		}
		compound.setTag("playeraccounts", list);
		list = new NBTTagList();
		for (int i = 0; i < GUILDS.accountList.size(); i++) {
			list.appendTag(GUILDS.writeToNBT(new NBTTagCompound(), i));
		}
		compound.setTag("guildaccounts", list);
		return compound;
	}

	public static AccountSaver get(World world) {
		MapStorage storage = world.getMapStorage();
		AccountSaver instance = (AccountSaver) storage.getOrLoadData(AccountSaver.class, DATA_NAME);
		if (instance == null) {
			instance = new AccountSaver();
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}
}

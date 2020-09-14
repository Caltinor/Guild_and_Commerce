package com.dicemc.marketplace.util.datasaver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	private final AccountGroup PLAYERS = new AccountGroup(this, "Player Accounts");
	private final AccountGroup GUILDS = new AccountGroup(this, "Guild Accounts");
	public static Map<UUID, Double> debt = new HashMap<UUID, Double>();

	public AccountSaver(String name) { super(name); }
	
	public AccountSaver() {super(DATA_NAME); }
	
	public AccountGroup getPlayers() {return PLAYERS;}
	
	public AccountGroup getGuilds() {return GUILDS;}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		PLAYERS.readFromNBT(nbt.getCompoundTag(PLAYERS.groupName).getTagList(PLAYERS.groupName, Constants.NBT.TAG_COMPOUND));
		GUILDS.readFromNBT(nbt.getCompoundTag(GUILDS.groupName).getTagList(GUILDS.groupName, Constants.NBT.TAG_COMPOUND));
		NBTTagList list = nbt.getCompoundTag("debt").getTagList("debt", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) { debt.put(list.getCompoundTagAt(i).getUniqueId("UUID"), list.getCompoundTagAt(i).getDouble("amount")); }
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag(PLAYERS.groupName, PLAYERS.writeToNBT(new NBTTagCompound()));
		compound.setTag(GUILDS.groupName, GUILDS.writeToNBT(new NBTTagCompound()));
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		if (debt.size() > 0) {
			for (Map.Entry<UUID, Double> entry : debt.entrySet()) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setUniqueId("UUID", entry.getKey());
				snbt.setDouble("amount", entry.getValue());
				list.appendTag(snbt);
			}
		}
		nbt.setTag("debt", list);
		compound.setTag("debt", nbt);
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

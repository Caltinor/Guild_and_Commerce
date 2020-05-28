package com.dicemc.marketplace.util.datasaver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.core.AccountGroup;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;

public class GuildSaver extends WorldSavedData {
	private static final String DATA_NAME = Reference.MOD_ID + "_GuildsData";
	public final List<Guild> GUILDS = new ArrayList<Guild>();

	public GuildSaver(String name) { super(name);}
	
	public GuildSaver() {super(DATA_NAME);}
	
	public int guildIndexFromName(String name) {
		for (int i = 0; i < GUILDS.size(); i++) {if (GUILDS.get(i).guildName.equalsIgnoreCase(name)) return i;	}
		return -1;
	}
	public int guildIndexFromUUID(UUID id) {
		for (int i = 0; i < GUILDS.size(); i++) {if (GUILDS.get(i).guildID.equals(id)) return i;	}
		return -1;
	}
	public String guildNamefromUUID(UUID guild) {
		for (int i = 0; i < GUILDS.size(); i++)	{if (GUILDS.get(i).guildID.equals(guild)) return GUILDS.get(i).guildName;}
		return "Guild N/A";
	}
	public UUID guildUUIDfromName(String name) {
		for (int i = 0; i < GUILDS.size(); i++) {if (GUILDS.get(i).guildName.equalsIgnoreCase(name)) return GUILDS.get(i).guildID;}
		return Reference.NIL;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("guilds", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound snbt = new NBTTagCompound();
			snbt = list.getCompoundTagAt(i);
			GUILDS.add(new Guild(snbt));
		}		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < GUILDS.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setUniqueId("guildID", GUILDS.get(i).guildID);
			nbt.setString("guildname", GUILDS.get(i).guildName);
			nbt.setBoolean("open", GUILDS.get(i).openToJoin);
			nbt.setDouble("tax", GUILDS.get(i).guildTax);
			nbt.setString("perm0", GUILDS.get(i).permLevels.getOrDefault(0, "Leader"));
			nbt.setString("perm1", GUILDS.get(i).permLevels.getOrDefault(1, "Dignitary"));
			nbt.setString("perm2", GUILDS.get(i).permLevels.getOrDefault(2, "Trustee"));
			nbt.setString("perm3", GUILDS.get(i).permLevels.getOrDefault(3, "Member"));
			nbt.setInteger("perm4", GUILDS.get(i).permissions.getOrDefault("setname", 3));
			nbt.setInteger("perm5", GUILDS.get(i).permissions.getOrDefault("setopen", 3));
			nbt.setInteger("perm6", GUILDS.get(i).permissions.getOrDefault("settax", 3));
			nbt.setInteger("perm7", GUILDS.get(i).permissions.getOrDefault("setperms", 3));
			nbt.setInteger("perm8", GUILDS.get(i).permissions.getOrDefault("setinvite", 3));
			nbt.setInteger("perm9", GUILDS.get(i).permissions.getOrDefault("setkick", 3));
			nbt.setInteger("perm10", GUILDS.get(i).permissions.getOrDefault("setclaim", 3));
			nbt.setInteger("perm11", GUILDS.get(i).permissions.getOrDefault("setsell", 3));
			nbt.setInteger("perm12", GUILDS.get(i).permissions.getOrDefault("setwithdraw", 3));
			nbt.setInteger("perm13", GUILDS.get(i).permissions.getOrDefault("setpromotedemote", 3));
			NBTTagList lnbt = new NBTTagList();
			for (Map.Entry<UUID, Integer> entry : GUILDS.get(i).members.entrySet()) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setUniqueId("UUID", entry.getKey());
				snbt.setInteger("permLevel", entry.getValue());
				lnbt.appendTag(snbt);
			}
			nbt.setTag("members", lnbt);
			lnbt = new NBTTagList();
			for (ChunkPos c : GUILDS.get(i).coreLand) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setLong("chunk", c.asLong(c.x, c.z));
				lnbt.appendTag(snbt);
			}
			nbt.setTag("coreland", lnbt);
			lnbt = new NBTTagList();
			for (ChunkPos c : GUILDS.get(i).outpostLand) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setLong("outland", c.asLong(c.x, c.z));
				lnbt.appendTag(snbt);
			}
			nbt.setTag("outpostland", lnbt);
			list.appendTag(nbt);
		}
		compound.setTag("guilds", list);
		return compound;
	}
	public static ChunkPos chunkFromLong(long longIn) {
		int x = (int)longIn;
		int z = (int)(longIn >> 32);
		return new ChunkPos(x, z);
	}
	
	public static GuildSaver get(World world) {
		MapStorage storage = world.getMapStorage();
		GuildSaver instance = (GuildSaver) storage.getOrLoadData(GuildSaver.class, DATA_NAME);
		if (instance == null) {
			instance = new GuildSaver();
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}
}

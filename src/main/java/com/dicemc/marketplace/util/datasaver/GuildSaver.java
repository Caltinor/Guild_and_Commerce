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
			list.appendTag(GUILDS.get(i).toNBT());
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

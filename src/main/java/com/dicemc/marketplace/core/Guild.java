package com.dicemc.marketplace.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;

public class Guild {
	public String guildName;
	public final UUID guildID;
	public boolean openToJoin;
	public boolean isAdmin;
	public double guildTax;
	public List<ChunkPos> coreLand = new ArrayList<ChunkPos>();
	public List<ChunkPos> outpostLand = new ArrayList<ChunkPos>();
	public Map<Integer, String> permLevels = new HashMap<Integer, String>();
	public Map<String, Integer> permissions = new HashMap<String, Integer>();
	public Map<UUID, Integer> members = new HashMap<UUID, Integer>();
	
	//this constructor is necessary for readFromNBT to properly load the data.
	private void baseConstr() {
		permLevels.put(0, new TextComponentTranslation("core.guild.rank0").getFormattedText());
		permLevels.put(1, new TextComponentTranslation("core.guild.rank1").getFormattedText());
		permLevels.put(2, new TextComponentTranslation("core.guild.rank2").getFormattedText());
		permLevels.put(3, new TextComponentTranslation("core.guild.rank3").getFormattedText());
		openToJoin = false;
		guildTax = 0;
		isAdmin = false;
		permissions.put("setname", 3);
		permissions.put("setopen", 3);
		permissions.put("settax", 3);
		permissions.put("setperms", 3);
		permissions.put("setinvite", 3);
		permissions.put("setkick", 3);
		permissions.put("setclaim", 3);
		permissions.put("setsell", 3);
		permissions.put("setwithdraw", 3);
		permissions.put("setpromotedemote", 3);
		permissions.put("managesublet", 3);
	}
	
	public Guild(UUID id) {
		guildID = id;
		guildName = "Unnamed Guild" + guildID.toString();
		baseConstr();
	}
	public Guild(String name) {
		guildName = name;
		guildID = UUID.randomUUID();
		baseConstr();
	}
	public Guild(NBTTagCompound nbt) {
		guildID = nbt.getUniqueId("guildID");
		guildName = nbt.getString("guildname");
		guildTax = nbt.getDouble("tax");
		openToJoin = nbt.getBoolean("open");
		isAdmin = nbt.getBoolean("isadmin");
		permLevels.put(0, nbt.getString("perm0"));
		permLevels.put(1, nbt.getString("perm1"));
		permLevels.put(2, nbt.getString("perm2"));
		permLevels.put(3, nbt.getString("perm3"));
		NBTTagList lnbt = new NBTTagList();
		lnbt = nbt.getTagList("permissions", Constants.NBT.TAG_COMPOUND);
		for (int x= 0; x < lnbt.tagCount(); x++) {
			permissions.put(lnbt.getCompoundTagAt(x).getString("key"), lnbt.getCompoundTagAt(x).getInteger("value"));
		}
		lnbt = nbt.getTagList("members", Constants.NBT.TAG_COMPOUND);
		for (int x= 0; x < lnbt.tagCount(); x++) {
			members.put(lnbt.getCompoundTagAt(x).getUniqueId("UUID"), lnbt.getCompoundTagAt(x).getInteger("permLevel"));
		}
		lnbt = nbt.getTagList("coreland", Constants.NBT.TAG_COMPOUND);
		for (int x= 0; x < lnbt.tagCount(); x++) {
			coreLand.add(chunkFromLong(lnbt.getCompoundTagAt(x).getLong("chunk"))); 
		}
		lnbt = nbt.getTagList("outpostland", Constants.NBT.TAG_COMPOUND);
		for (int x= 0; x < lnbt.tagCount(); x++) {
			outpostLand.add(chunkFromLong(lnbt.getCompoundTagAt(x).getLong("outland")));
		}
	}
	
	public boolean addMember(UUID player, int permLevel) {
		members.put(player, permLevel);
		return true;
	}
	
	public boolean removeMember(UUID player) {
		if (members.remove(player) != null) {
			members.remove(player);
			return true;
		}
		return false;
	}
	public void removeLand(ChunkPos ck) {
		for (int i = 0; i < coreLand.size(); i++) {
			if (coreLand.get(i).equals(ck)) coreLand.remove(i); }
		for (int i = 0; i < outpostLand.size(); i++) {
			if (outpostLand.get(i).equals(ck)) outpostLand.remove(i);}
	}
	public int countPermLevels(int level) {
		int count = 0;
		for (UUID m : members.keySet()) {if (members.get(m) == level) count++; }
		return count;
	}
	public double taxableWorth(World world) {
		int mbr = 0;
		double totalCount = coreLand.size() + outpostLand.size();
		for (Map.Entry<UUID, Integer> entry : members.entrySet()) {if (entry.getValue() != -1) mbr++;}
		double worth = 0;
		for (ChunkPos c : outpostLand) {
			ChunkCapability cap = world.getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
			worth += cap.getPrice();
		}
		for (ChunkPos c : coreLand) {
			ChunkCapability cap = world.getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
			worth += cap.getPrice();
		}
		int coreCount = (coreLand.size()-(mbr*Main.ModConfig.CHUNKS_PER_MEMBER));
		double taxable =  coreCount > 0 ? coreCount + outpostLand.size() : outpostLand.size();		
		double proportion = totalCount != 0 ? (taxable/totalCount) : 0;
		worth = worth * proportion;		
		return worth;
	}
	public double guildWorth(World world) {
		double worth = 0;
		for (ChunkPos c : outpostLand) {
			ChunkCapability cap = world.getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
			if (!cap.getForSale()) worth += cap.getPrice();
		}
		for (ChunkPos c : coreLand) {
			ChunkCapability cap = world.getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
			if (!cap.getForSale()) worth += cap.getPrice();
		}
		return worth;
	}
	
	public void promoteMember(UUID member) { if (members.get(member) > 0) members.put(member, members.get(member)-1);}
	
	public void demoteMember(UUID member) { if (members.get(member) < 3) members.put(member, members.get(member)+1);}
	
	public List<String> listMembers(int permLevel, MinecraftServer server) {
		List<String> list = new ArrayList<String>();
		for (Map.Entry<UUID, Integer> entry : members.entrySet()) {
			if (entry.getValue() == permLevel) {
				list.add(server.getPlayerProfileCache().getProfileByUUID(entry.getKey()).getName());
			}
		}
		return list;
	}
	
	public NBTTagCompound toNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setUniqueId("guildID", guildID);
			nbt.setString("guildname", guildName);
			nbt.setBoolean("open", openToJoin);
			nbt.setBoolean("isadmin", isAdmin);
			nbt.setDouble("tax", guildTax);
			nbt.setString("perm0", permLevels.getOrDefault(0, new TextComponentTranslation("core.guild.rank0").getFormattedText()));
			nbt.setString("perm1", permLevels.getOrDefault(1, new TextComponentTranslation("core.guild.rank1").getFormattedText()));
			nbt.setString("perm2", permLevels.getOrDefault(2, new TextComponentTranslation("core.guild.rank2").getFormattedText()));
			nbt.setString("perm3", permLevels.getOrDefault(3, new TextComponentTranslation("core.guild.rank3").getFormattedText()));
			NBTTagList lnbt = new NBTTagList();
			for (Map.Entry<String, Integer> entry : permissions.entrySet()) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setString("key", entry.getKey());
				snbt.setInteger("value", entry.getValue());
				lnbt.appendTag(snbt);
			}
			nbt.setTag("permissions", lnbt);
			lnbt = new NBTTagList();
			for (Map.Entry<UUID, Integer> entry : members.entrySet()) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setUniqueId("UUID", entry.getKey());
				snbt.setInteger("permLevel", entry.getValue());
				lnbt.appendTag(snbt);
			}
			nbt.setTag("members", lnbt);
			lnbt = new NBTTagList();
			for (ChunkPos c : coreLand) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setLong("chunk", c.asLong(c.x, c.z));
				lnbt.appendTag(snbt);
			}
			nbt.setTag("coreland", lnbt);
			lnbt = new NBTTagList();
			for (ChunkPos c : outpostLand) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setLong("outland", c.asLong(c.x, c.z));
				lnbt.appendTag(snbt);
			}
			nbt.setTag("outpostland", lnbt);

		return nbt;
	}
	
	public static ChunkPos chunkFromLong(long longIn) {
		int x = (int)longIn;
		int z = (int)(longIn >> 32);
		return new ChunkPos(x, z);
	}
}

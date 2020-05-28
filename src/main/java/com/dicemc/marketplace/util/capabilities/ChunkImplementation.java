package com.dicemc.marketplace.util.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.dicemc.marketplace.util.Reference;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;

public class ChunkImplementation implements ChunkCapability{
	private UUID owner = Reference.NIL;
	private double price = 100;
	private boolean redstone = false;
	private List<String> whitelist = new ArrayList<String>();
	private long tempclaimEnd = System.currentTimeMillis();
	private boolean isPublic = false;
	private boolean isForSale = false;
	private boolean isOutpost = false;
	private List<UUID> permittedPlayers = new ArrayList<UUID>();
	
	@Override
	public void setOwner(UUID guild) {owner = guild; }
	@Override
	public UUID getOwner() {return owner;}
	@Override
	public void setPrice(double price) {this.price = price;}
	@Override
	public double getPrice() { return price; }
	@Override
	public void setWhitelist(NBTTagList list) {
		whitelist.clear();
		for (int i = 0; i < list.tagCount(); i++) {	whitelist.add(list.getStringTagAt(i)); }		
	}
	@Override
	public void changeWhitelist(String item) {
		boolean added = false;
		for (int i = 0; i < whitelist.size(); i++) {
			if (whitelist.get(i) == item) whitelist.remove(i);
			added = true;
		}		
		if (!added) whitelist.add(item);
	}
	@Override
	public NBTTagList getWhitelist() {
		NBTTagList lnbt = new NBTTagList();
		for (int i = 0; i < whitelist.size(); i++) {
			NBTTagString nbt = new NBTTagString(whitelist.get(i));
			lnbt.appendTag(nbt);
		}
		return lnbt;
	}
	@Override
	public void setPublicRedstone(boolean allow) {redstone = allow;}
	@Override
	public boolean getPublicRedstoner() { return redstone;}
	@Override
	public void setTempTime(long millis) { tempclaimEnd = millis;	}
	@Override
	public long getTempTime() {	return tempclaimEnd;}
	@Override
	public void setPublic(boolean type) {isPublic = type; }
	@Override
	public boolean getPublic() {return isPublic;}
	@Override
	public void includePlayer(UUID player) {permittedPlayers.add(player);}
	@Override
	public void removePlayer(UUID player) {
		for (int i = 0; i < permittedPlayers.size(); i++) {
			if (permittedPlayers.get(i).equals(player)) permittedPlayers.remove(i); }		
	}
	@Override
	public List<UUID> getPlayers() {return permittedPlayers;}
	@Override
	public NBTTagCompound playersToNBT() {
		NBTTagCompound snbt = new NBTTagCompound();
		NBTTagList lnbt = new NBTTagList();
		for (int i = 0; i < permittedPlayers.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("player", permittedPlayers.get(i).toString());
			lnbt.appendTag(nbt);
		}
		snbt.setTag("permitted", lnbt);
		return snbt;
	}
	@Override
	public List<UUID> playersFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("permitted", Constants.NBT.TAG_COMPOUND);
		List<UUID> plyrs = new ArrayList<UUID>();
		for (int i = 0; i < list.tagCount(); i++) {
			plyrs.add(UUID.fromString(list.getCompoundTagAt(i).getString("player")));
		}
		return plyrs;
	}
	@Override
	public void setPlayers(List<UUID> list) {permittedPlayers = list;}
	@Override
	public boolean getForSale() {return isForSale;}
	@Override
	public void setForSale(boolean forSale) {isForSale = forSale;}
	@Override
	public boolean getOutpost() {return isOutpost;}
	@Override
	public void setOutpost(boolean isOutpost) {this.isOutpost = isOutpost; }
}

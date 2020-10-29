package com.dicemc.marketplace.util.capabilities;

import java.util.List;
import java.util.UUID;

import com.dicemc.marketplace.core.WhitelistItem;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public interface ChunkCapability {
	public void setTempTime(long millis);
	public long getTempTime();
	public void setPublic(boolean type); //is temp chunk public or private
	public boolean getPublic();
	public void includePlayer(UUID player);
	public void removePlayer(UUID player);
	public List<UUID> getPlayers();
	public NBTTagCompound playersToNBT();
	public List<UUID> playersFromNBT(NBTTagCompound nbt);
	public void setPlayers(List<UUID> list);
	
	public boolean getForSale();
	public void setForSale(boolean forSale);
	
	public boolean getOutpost();
	public void setOutpost(boolean isOutpost);
	
	public void setOwner(UUID guild);
	public UUID getOwner();
	
	public void setPrice(double price);
	public double getPrice();
	public void setLeasePrice(double price);
	public double getLeasePrice();
	public int getLeaseDuration();
	public void setLeaseDuration(int duration);
	
	public List<WhitelistItem> getWhitelist();
	public void changeWhitelist(WhitelistItem item);
	public NBTTagList toNBTWhitelist();
	public void fromNBTWhitelist(NBTTagList list);	
	
	public int getPermMin();
	public void setPermMin(int level);
	
	public boolean getExplosionsOn();
	public void setExplosionsOn(boolean set);
}

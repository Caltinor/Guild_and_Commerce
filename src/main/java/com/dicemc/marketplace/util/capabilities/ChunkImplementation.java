package com.dicemc.marketplace.util.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.WhitelistItem;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class ChunkImplementation implements ChunkCapability{
	private UUID owner = Reference.NIL;
	private double price = Main.ModConfig.LAND_DEFAULT_COST;
	private double leasePrice = -1;
	private int leaseDuration = 0;
	private int permissionMinimum = 3;
	private List<WhitelistItem> whitelist = new ArrayList<WhitelistItem>();
	private long tempclaimEnd = System.currentTimeMillis();
	private boolean isPublic = false;
	private boolean isForSale = false;
	private boolean isOutpost = false;
	private List<UUID> permittedPlayers = new ArrayList<UUID>();
	private boolean explosionsOn = true;
	
	@Override
	public void setOwner(UUID guild) {owner = guild; }
	@Override
	public UUID getOwner() {return owner;}
	@Override
	public void setPrice(double price) {this.price = price;}
	@Override
	public double getPrice() { return price; }
	@Override
	public List<WhitelistItem> getWhitelist() {
		return whitelist;
	}
	@Override
	public void fromNBTWhitelist(NBTTagList list) {
		whitelist.clear();
		for (int i = 0; i < list.tagCount(); i++) {	whitelist.add(new WhitelistItem(list.getCompoundTagAt(i))); }		
	}
	@Override
	public void changeWhitelist(WhitelistItem item) {
		for (int i = 0; i < whitelist.size(); i++) {
			if (item.getBlock() != "" && whitelist.get(i).getBlock().equals(item.getBlock())) {
				whitelist.set(i, item);
				return;
			}
			if (item.getEntity() != "" && item.getEntity().equals(whitelist.get(i).getEntity())) {
				whitelist.set(i, item);
				return;
			}
		}		
		whitelist.add(item);
	}
	@Override
	public NBTTagList toNBTWhitelist() {
		NBTTagList lnbt = new NBTTagList();
		for (int i = 0; i < whitelist.size(); i++) {
			lnbt.appendTag(whitelist.get(i).toNBT());
		}
		return lnbt;
	}
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
	@Override
	public void setLeasePrice(double price) {leasePrice = price;}
	@Override
	public double getLeasePrice() {return leasePrice;}
	@Override
	public int getLeaseDuration() {return leaseDuration;}
	@Override
	public void setLeaseDuration(int duration) {leaseDuration = duration;}	
	@Override
	public int getPermMin() {return permissionMinimum;}
	@Override
	public void setPermMin(int level) {permissionMinimum = level;}
	@Override
	public boolean getExplosionsOn() {return explosionsOn;}
	@Override
	public void setExplosionsOn(boolean set) {explosionsOn = set;}
}

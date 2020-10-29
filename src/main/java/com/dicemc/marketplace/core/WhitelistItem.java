package com.dicemc.marketplace.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class WhitelistItem {
	private boolean canBreak = false;
	private boolean canInteract = false;
	private String refEntity = "";
	private String refBlock = ""; 
	
	public WhitelistItem(Entity entity) {
		refEntity = entity.getClass().getSimpleName();
	}
	
	public WhitelistItem(String blockRegistryName) {
		refBlock = blockRegistryName;
	}
	
	public WhitelistItem(NBTTagCompound nbt) {
		refEntity = nbt.getString("entity");
		refBlock = nbt.getString("block");
		canBreak = nbt.getBoolean("canbreak");
		canInteract = nbt.getBoolean("caninteract");
	}
	
	public boolean getCanBreak() {return canBreak;}
	public void setCanBreak(boolean bool) {canBreak = bool;}
	public boolean getCanInteract() {return canInteract;}
	public void setCanInteract(boolean bool) {canInteract = bool;}
	public String getEntity() {return refEntity;}
	public String getBlock() {return refBlock;}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("entity", refEntity);
		nbt.setString("block", refBlock);
		nbt.setBoolean("canbreak", canBreak);
		nbt.setBoolean("caninteract", canInteract);
		return nbt;
	}
}

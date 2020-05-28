package com.dicemc.marketplace.util.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.Constants;

public class ChunkStorage implements IStorage<ChunkCapability>{

	@Override
	public NBTBase writeNBT(Capability<ChunkCapability> capability, ChunkCapability instance, EnumFacing side) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("publicredstone", instance.getPublicRedstoner());
		nbt.setBoolean("ispublic", instance.getPublic());
		nbt.setBoolean("isoutpost", instance.getOutpost());
		nbt.setBoolean("isforsale", instance.getForSale());
		nbt.setLong("tempclaimend", instance.getTempTime());
		nbt.setUniqueId("UUID", instance.getOwner());
		nbt.setDouble("price", instance.getPrice());
		nbt.setTag("whitelist", instance.getWhitelist());
		nbt.setTag("permitted", instance.playersToNBT());
		return nbt;
	}

	@Override
	public void readNBT(Capability<ChunkCapability> capability, ChunkCapability instance, EnumFacing side, NBTBase nbt) {
		instance.setWhitelist(((NBTTagCompound) nbt).getTagList("whitelist", Constants.NBT.TAG_COMPOUND));	
		instance.setOwner(((NBTTagCompound) nbt).getUniqueId("UUID"));
		instance.setPrice(((NBTTagCompound) nbt).getDouble("price"));
		instance.setPublicRedstone(((NBTTagCompound) nbt).getBoolean("publicredstone"));
		instance.setTempTime(((NBTTagCompound) nbt).getLong("tempclaimend"));
		instance.setPublic(((NBTTagCompound) nbt).getBoolean("ispublic"));
		instance.setPlayers(instance.playersFromNBT(((NBTTagCompound) nbt).getCompoundTag("permitted")));
		instance.setForSale(((NBTTagCompound) nbt).getBoolean("isforsale"));
		instance.setOutpost(((NBTTagCompound) nbt).getBoolean("isoutpost"));
	}

}

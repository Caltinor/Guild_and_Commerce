package com.dicemc.marketplace.util.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ChunkProvider implements ICapabilitySerializable<NBTBase>{

	@CapabilityInject(ChunkCapability.class)
	public static final Capability<ChunkCapability> CHUNK_CAP = null;
	
	private ChunkCapability instance = CHUNK_CAP.getDefaultInstance();
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {return capability == CHUNK_CAP;}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CHUNK_CAP ? CHUNK_CAP.<T> cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT() { return CHUNK_CAP.getStorage().writeNBT(CHUNK_CAP, this.instance, null); }

	@Override
	public void deserializeNBT(NBTBase nbt) { CHUNK_CAP.getStorage().readNBT(CHUNK_CAP, this.instance, null, nbt); }

}

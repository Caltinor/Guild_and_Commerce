package com.dicemc.marketplace.util.datasaver;

import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.core.Marketplace;
import com.dicemc.marketplace.util.ObjectType;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class MarketSaver extends WorldSavedData {
	private static final String DATA_NAME = Reference.MOD_ID + "_MarketsData";
	private final Marketplace LOCAL = new Marketplace(this, ObjectType.LOCAL);
	private final Marketplace GLOBAL = new Marketplace(this, ObjectType.GLOBAL);
	private final Marketplace AUCTION = new Marketplace(this, ObjectType.AUCTION);
	private final Marketplace SERVER = new Marketplace(this, ObjectType.SERVER);

	public MarketSaver(String name) {super(name);}
	
	public MarketSaver() {super(DATA_NAME);	}
	
	public Marketplace getLocal() {return LOCAL;}
	
	public Marketplace getGlobal() {return GLOBAL;}
	
	public Marketplace getAuction() {return AUCTION;}
	
	public Marketplace getServer() {return SERVER;}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		LOCAL.readFromNBT(nbt.getTagList("localmarket", Constants.NBT.TAG_COMPOUND));
		LOCAL.readQueueFromNBT(nbt.getTagList("localqueue", Constants.NBT.TAG_COMPOUND));
		GLOBAL.readFromNBT(nbt.getTagList("globalmarket", Constants.NBT.TAG_COMPOUND));
		GLOBAL.readQueueFromNBT(nbt.getTagList("globalqueue",  Constants.NBT.TAG_COMPOUND));
		AUCTION.readFromNBT(nbt.getTagList("auctionhouse", Constants.NBT.TAG_COMPOUND));
		AUCTION.readQueueFromNBT(nbt.getTagList("auctionqueue", Constants.NBT.TAG_COMPOUND));
		SERVER.readFromNBT(nbt.getTagList("servermarket", Constants.NBT.TAG_COMPOUND));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (Map.Entry<UUID, MarketItem> entry : LOCAL.vendList.entrySet()) {
			list.appendTag(LOCAL.writeToNBT(new NBTTagCompound(), entry.getKey()));
		}
		compound.setTag("localmarket", list);
		compound.setTag("localqueue", LOCAL.writeQueueToNBT(new NBTTagList()));
		list = new NBTTagList();
		for (Map.Entry<UUID, MarketItem> entry : GLOBAL.vendList.entrySet()) {
			list.appendTag(GLOBAL.writeToNBT(new NBTTagCompound(), entry.getKey()));
		}
		compound.setTag("globalmarket", list);
		compound.setTag("globalqueue", GLOBAL.writeQueueToNBT(new NBTTagList()));
		list = new NBTTagList();
		for (Map.Entry<UUID, MarketItem> entry : AUCTION.vendList.entrySet()) {
			list.appendTag(AUCTION.writeToNBT(new NBTTagCompound(), entry.getKey()));
		}
		compound.setTag("auctionhouse", list);
		compound.setTag("auctionqueue", AUCTION.writeQueueToNBT(new NBTTagList()));
		list = new NBTTagList();
		for (Map.Entry<UUID, MarketItem> entry : SERVER.vendList.entrySet()) {
			list.appendTag(SERVER.writeToNBT(new NBTTagCompound(), entry.getKey()));
		}
		compound.setTag("servermarket", list);
		return compound;
	}
	
	public static MarketSaver get(World world) {
		MapStorage storage = world.getMapStorage();
		MarketSaver instance = (MarketSaver) storage.getOrLoadData(MarketSaver.class, DATA_NAME);
		if (instance == null) {
			instance = new MarketSaver();
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}
}

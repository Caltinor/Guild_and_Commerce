package com.dicemc.marketplace.util.proxy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiMarketManager;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;
import com.dicemc.marketplace.network.PacketHandler;
import com.dicemc.marketplace.util.Reference;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {
	
	public void openMarketGui(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response, UUID locality) {}
	
	public void openChunkGui(Guild myGuild, List<ChunkSummary> list, List<Integer> mapColors, String response, double acctP, double acctG) {}
	
	public void openGuildGui(Guild guild, Account acctGuild, double worthT, double worthG, Map<ChunkPos, Double> chunkValues, double balancePlayer) {}
	
	public void openMemberGui(Guild guild, Map<UUID, String> memberNames) {}
	
	public void openPermsGui(Guild guild) {}
	
	public void openCreateGui(Map<UUID, String> invitedGuilds, double balP) {}
	
	public void openAdminGui() {}
	
	public void registerNetworkPackets() {
		Main.NET = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
		PacketHandler.initHandler(); 
	}
	
	public void registerItemRenderer(Item item, int meta, String id) {}
	
	public ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule) {
		throw new IllegalStateException("This should only be called from the client side");
	}
	
	public EntityPlayer getClientPlayer() {
		throw new IllegalStateException("This should only be called from the client side");
	}
}

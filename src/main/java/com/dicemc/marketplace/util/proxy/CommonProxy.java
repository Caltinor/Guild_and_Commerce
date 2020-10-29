package com.dicemc.marketplace.util.proxy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;
import com.dicemc.marketplace.gui.GuiMarketManager.MarketListItem;
import com.dicemc.marketplace.network.PacketHandler;
import com.dicemc.marketplace.util.Reference;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {
	
	public void openMarketGui(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response, UUID locality) {}
	
	public void openChunkGui(Guild myGuild, List<ChunkSummary> list, List<Integer> mapColors, String response, double acctP, double acctG, double tempClaimRate) {}
	
	public void openGuildGui(Guild guild, Account acctGuild, double worthT, double worthG, Map<ChunkPos, Double> chunkValues, double balancePlayer) {}
	
	public void openMemberGui(Guild guild, Map<UUID, String> memberNames) {}
	
	public void openPermsGui(Guild guild) {}
	
	public void openCreateGui(Map<UUID, String> invitedGuilds, double balP, double guildPrice) {}
	
	public void openAdminGui() {}
	
	public void updateMarketGui(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response) {}
	
	public void updateChunkGui(Guild myGuild, List<ChunkSummary> list, List<Integer> mapColors, String response, double acctP, double acctG, double tempClaimRate) {}
	
	public void updateGuildGuiAccounts(Account acctG, double balP) {}
	
	public void updateMemberGui(Guild guild, Map<UUID, String> memberNames) {}
	
	public void updatePermsGui(Guild guild) {}
	
	public void updateAdminSyncAccounts(Map<Account, String> accountList) {}
	
	public void updateAdminSyncGuildList(Map<UUID, String> nameList) {}
	
	public void updateAdminSyncGuildData(String name, boolean open, double tax, String perm0, String perm1, String perm2, String perm3, Map<String, Integer> guildPerms, boolean isAdmin) {}
	
	public void updateAdminSyncGuildLand(List<ChunkPos> posCore, List<ChunkPos> posOutpost, Map<ChunkPos, Double> chunkValues) {}
	
	public void updateAdminSyncGuildLandDetail(double value, boolean isPublic, boolean isForSale, boolean isOutpost) {}
	
	public void updateAdminSyncGuildMembers(Map<UUID, Integer> members, Map<UUID,String> mbrNames) {}
	
	public void updateAdminSyncMarkets(List<MarketListItem> list) {}
	
	public void updateAdminSyncMarketDetail(String vendorName, String locName, String bidderName) {}
	
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

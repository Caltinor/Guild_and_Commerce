package com.dicemc.marketplace.util.proxy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiAccountManager;
import com.dicemc.marketplace.gui.GuiAdmin;
import com.dicemc.marketplace.gui.GuiChunkManager;
import com.dicemc.marketplace.gui.GuiGuildCreate;
import com.dicemc.marketplace.gui.GuiGuildManager;
import com.dicemc.marketplace.gui.GuiGuildMemberManager;
import com.dicemc.marketplace.gui.GuiGuildPerms;
import com.dicemc.marketplace.gui.GuiMarketManager;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;
import com.dicemc.marketplace.gui.GuiMarketManager.MarketListItem;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	
	public void openMarketGui(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response, UUID locality) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiMarketManager(listType, vendList, feeBuy, feeSell, balP, response, locality));
	}
	
	public void openChunkGui(Guild myGuild, List<ChunkSummary> list, List<Integer> mapColors, String response, double acctP, double acctG, double tempClaimRate) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiChunkManager(myGuild, list, mapColors, response, acctP, acctG, tempClaimRate));
	}
	
	public void openGuildGui(Guild guild, Account acctGuild, double worthT, double worthG, Map<ChunkPos, Double> chunkValues, double balancePlayer) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiGuildManager(guild, acctGuild, worthT, worthG, chunkValues, balancePlayer));
	}
	
	public void openMemberGui(Guild guild, Map<UUID, String> memberNames) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiGuildMemberManager(guild, memberNames));
	}
	
	public void openPermsGui(Guild guild) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiGuildPerms(guild));
	}
	
	public void openCreateGui(Map<UUID, String> invitedGuilds, double balP, double guildPrice) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiGuildCreate(invitedGuilds, balP, guildPrice));
	}
	
	public void openAdminGui() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiAdmin());
	}
	
	public void updateMarketGui(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response) {
		GuiMarketManager screen = (GuiMarketManager) Minecraft.getMinecraft().currentScreen;
		screen.syncMarket(listType, vendList, feeBuy, feeSell, balP, response);
	} 
	
	public void updateChunkGui(Guild myGuild, List<ChunkSummary> list, List<Integer> mapColors, String response, double acctP, double acctG, double tempClaimRate) {
		GuiChunkManager screen = (GuiChunkManager) Minecraft.getMinecraft().currentScreen;
		screen.guiUpdate(myGuild, list, mapColors, response, acctP, acctG, tempClaimRate);
	}
	
	public void updateGuildGuiAccounts(Account acctG, double balP) {
		GuiGuildManager screen = (GuiGuildManager) Minecraft.getMinecraft().currentScreen;
		screen.syncAccounts(acctG, balP);
	}
	
	public void updateMemberGui(Guild guild, Map<UUID, String> memberNames) {
		GuiGuildMemberManager screen = (GuiGuildMemberManager) Minecraft.getMinecraft().currentScreen;
		screen.syncMembers(guild, memberNames);
	}
	
	public void updatePermsGui(Guild guild) {
		GuiGuildPerms screen = (GuiGuildPerms) Minecraft.getMinecraft().currentScreen;
		screen.syncGui(guild);
	}
	
	public void updateAdminSyncAccounts(Map<Account, String> accountList) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncAccounts(accountList);
	}
	
	public void updateAdminSyncGuildList(Map<UUID, String> nameList) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncGuildList(nameList);
	}
	
	public void updateAdminSyncGuildData(String name, boolean open, double tax, String perm0, String perm1, String perm2, String perm3, Map<String, Integer> guildPerms, boolean isAdmin) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncGuildData(name, open, tax, perm0, perm1, perm2, perm3, guildPerms, isAdmin);
	}
	
	public void updateAdminSyncGuildLand(List<ChunkPos> posCore, List<ChunkPos> posOutpost, Map<ChunkPos, Double> chunkValues) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncGuildLand(posCore, posOutpost, chunkValues);
	}
	
	public void updateAdminSyncGuildLandDetail(double value, boolean isPublic, boolean isForSale, boolean isOutpost) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncGuildLandDetail(value, isPublic, isForSale, isOutpost);
	}
	
	public void updateAdminSyncGuildMembers(Map<UUID, Integer> members, Map<UUID,String> mbrNames) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncGuildMembers(members, mbrNames);
	}
	
	public void updateAdminSyncMarkets(List<MarketListItem> list) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncMarkets(list);
	}
	
	public void updateAdminSyncMarketDetail(String vendorName, String locName, String bidderName) {
		GuiAdmin screen = (GuiAdmin) Minecraft.getMinecraft().currentScreen;
		screen.syncMarketDetail(vendorName, locName, bidderName);
	}	
	
	public void openAccountGui(double balP) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiAccountManager(balP));
	}
	
	public void registerNetworkPackets() {super.registerNetworkPackets();}
	
	public void registerItemRenderer(Item item, int meta, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
	
	public ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule) {
		return Minecraft.getMinecraft().addScheduledTask(runnableToSchedule);
	}
	
	public EntityPlayer getClientPlayer() {return Minecraft.getMinecraft().player;}
}


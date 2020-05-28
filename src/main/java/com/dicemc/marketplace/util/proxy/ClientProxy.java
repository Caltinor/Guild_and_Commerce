package com.dicemc.marketplace.util.proxy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiChunkManager;
import com.dicemc.marketplace.gui.GuiGuildCreate;
import com.dicemc.marketplace.gui.GuiGuildManager;
import com.dicemc.marketplace.gui.GuiGuildMemberManager;
import com.dicemc.marketplace.gui.GuiGuildPerms;
import com.dicemc.marketplace.gui.GuiMarketManager;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;
import com.dicemc.marketplace.network.PacketHandler;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
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
	
	public void openChunkGui(List<ChunkSummary> list, List<Integer> mapColors, UUID playerGuildID, boolean canGuildClaim, boolean canGuildSell, String response, double acctP, double acctG) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiChunkManager(list, mapColors, playerGuildID, canGuildClaim, canGuildSell, response, acctP, acctG));
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
	
	public void openCreateGui(Map<UUID, String> invitedGuilds, double balP) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiGuildCreate(invitedGuilds, balP));
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


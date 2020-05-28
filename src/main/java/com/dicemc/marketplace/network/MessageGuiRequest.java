package com.dicemc.marketplace.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.AccountGroup;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.Marketplace;
import com.dicemc.marketplace.events.GuiEventHandler;
import com.dicemc.marketplace.gui.GuiChunkManager;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;
import com.dicemc.marketplace.gui.GuiMarketManager;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.commands.TempClaimCommands;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGuiRequest implements IMessage{
	public int guiType;
	/*GuiTypes are as follows
	 *0 = Chunks
	 *1 = Guild
	 *2 = Guild Members
	 *3 = Guild Permissions
	 *4 = Market
	 */

	//this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
	public MessageGuiRequest() {}
	
	//unused until I need the UUID to pass for the check
	public MessageGuiRequest(int guiType) {
		this.guiType = guiType;
	}
	 
	@Override
	public void fromBytes(ByteBuf buf) {
		guiType = buf.readInt();
	}
	 
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(guiType);
	}
	
	public static class PacketGuiRequest implements IMessageHandler<MessageGuiRequest, IMessage> {
		@Override
		public IMessage onMessage(MessageGuiRequest message, MessageContext ctx) {
			 FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageGuiRequest message, MessageContext ctx) {
			List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
			List<Account> acctG = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.accountList;
			List<Account> acctP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.accountList;
			int gindex = -1;
			for (int i = 0; i < glist.size(); i++) {
				for (Map.Entry<UUID, Integer> entries : glist.get(i).members.entrySet()) {
					if (entries.getKey().equals(ctx.getServerHandler().player.getUniqueID()) && entries.getValue() >= 0) {
						gindex = i;
					}
				}
			}
			int acctIndex = -1;
			int pacctIndex = -1;
			double worthT = 0;
			double worthG = 0;
			Map<UUID, String> mbrNames = new HashMap<UUID, String>();
			if (gindex >= 0) {
				worthT = glist.get(gindex).taxableWorth(ctx.getServerHandler().player.getEntityWorld());
				worthG = glist.get(gindex).guildWorth(ctx.getServerHandler().player.getEntityWorld());								
				for (int i = 0; i < acctG.size(); i++) {
					if (acctG.get(i).owner.equals(glist.get(gindex).guildID)) acctIndex = i;
				}								
				for (int i = 0; i < acctP.size(); i++) {
					if (acctP.get(i).owner.equals(ctx.getServerHandler().player.getUniqueID())) pacctIndex = i;
				}				
				for (UUID u : glist.get(gindex).members.keySet()) {
					mbrNames.put(u, ctx.getServerHandler().player.getServer().getPlayerProfileCache().getProfileByUUID(u).getName());
				}
			}
			
			switch(message.guiType) {
			case 0: {
				List<ChunkSummary> list = new ArrayList<ChunkSummary>();
				int cX = ctx.getServerHandler().player.chunkCoordX;
				int cZ = ctx.getServerHandler().player.chunkCoordZ;
				List<Integer> mapColors = new ArrayList<Integer>();				
				int[][] colorRows = new int[80][80];
				for (int z = 0; z < 5; z++) {
					for (int x = 0; x < 5; x++) {
						int modX = x - 2;
						int modZ = z - 2;
						//ChunkSummary(String owner, double price, boolean redstone, boolean isPublic, boolean isForSale, boolean isOutpost, long claimEnd, List<String> whitelist, List<UUID> members)
						Chunk ck = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(cX + modX, cZ + modZ);
						ChunkCapability cap = ck.getCapability(ChunkProvider.CHUNK_CAP, null);
						String ownerName = TempClaimCommands.ownerName(ctx.getServerHandler().player.getUniqueID(), cap, ctx.getServerHandler().player.getServer());
						boolean guildOwned = false;
						for (int i = 0; i < glist.size(); i++) {if (cap.getOwner().equals(glist.get(i).guildID)) {guildOwned = true; break;}}
						List<String> plist = new ArrayList<String>();
						for (int i = 0; i < cap.getPlayers().size(); i++) {	plist.add(Commands.playerNamefromUUID(ctx.getServerHandler().player.getServer(), cap.getPlayers().get(i)));}
						list.add(new ChunkSummary(guildOwned, cap.getOwner(), ownerName, cap.getPrice(), cap.getPublicRedstoner(), cap.getPublic(), cap.getForSale(), cap.getOutpost(), cap.getTempTime(), new ArrayList<String>(), plist));
						for (int r = 0; r < 16; r++) {
							for (int c = 0; c < 16; c++) {
								int xval = c+ck.getPos().getXStart();
								int zval = r+ck.getPos().getZStart();
								BlockPos pos= new BlockPos(xval, 255, zval); 
								for (int top = 255; top >= 0; top--) { 
									if (ctx.getServerHandler().player.getEntityWorld().getBlockState(new BlockPos(xval, top, zval)).getBlock() != Blocks.AIR) {
										pos = new BlockPos(xval, top, zval);
										break;
									}
								}
								colorRows[r+(z*16)][c+(x*16)] = ctx.getServerHandler().player.getEntityWorld().getBlockState(pos).getMapColor(ctx.getServerHandler().player.getEntityWorld(), pos).colorValue;
							}
						}
					}
				}
				for (int a = 0; a < 80; a++) {
					for (int b = 0; b < 80; b++) {
						mapColors.add(colorRows[a][b]);
					}
				}
				UUID gid = (gindex >=0 ) ? glist.get(gindex).guildID : Reference.NIL;
				boolean canGuildClaim =(gindex >=0 ) ? (glist.get(gindex).members.get(ctx.getServerHandler().player.getUniqueID()) <= glist.get(gindex).permissions.get("setclaim") ? true : false) :false;
				boolean canGuildSell = (gindex >=0 ) ? (glist.get(gindex).members.get(ctx.getServerHandler().player.getUniqueID()) <= glist.get(gindex).permissions.get("setsell") ? true : false) :false;
				double balG = gid != Reference.NIL ? AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.getBalance(gid) : 0;
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
				Main.NET.sendTo(new MessageChunkToGui(false, list, mapColors, gid, canGuildClaim, canGuildSell, "", balP, balG), ctx.getServerHandler().player);
				break;
			}			
			case 1: {				
				if (gindex >= 0) {
					Map<ChunkPos, Double> chunkValues = new HashMap<ChunkPos, Double>();
					for (ChunkPos c : glist.get(gindex).coreLand) {
						ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
						double price = cap.getForSale() ? -1* cap.getPrice() : cap.getPrice();
						chunkValues.put(c, price);
					}
					for (ChunkPos c : glist.get(gindex).outpostLand) {
						ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
						double price = cap.getForSale() ? -1* cap.getPrice() : cap.getPrice();
						chunkValues.put(c, price);
					}
					double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
					Main.NET.sendTo(new MessageGuildToGui(1, glist.get(gindex), acctG.get(acctIndex), worthT, worthG, mbrNames, chunkValues, balP, ctx.getServerHandler().player.getUniqueID()), ctx.getServerHandler().player);
				}
				else {openGuiCreate(message, ctx, glist);}
				break;
			}
			case 2: {				
				if (gindex >= 0) {
					Main.NET.sendTo(new MessageGuildToGui(2, glist.get(gindex), acctG.get(acctIndex), worthT, worthG, mbrNames, new HashMap<ChunkPos, Double>(), 0D, ctx.getServerHandler().player.getUniqueID()), ctx.getServerHandler().player);
				}
				else {openGuiCreate(message, ctx, glist);}
				break;
			}
			case 3: {				
				if (gindex >= 0) {
					Main.NET.sendTo(new MessageGuildToGui(3, glist.get(gindex), acctG.get(acctIndex), worthT, worthG, mbrNames, new HashMap<ChunkPos, Double>(), 0D, ctx.getServerHandler().player.getUniqueID()), ctx.getServerHandler().player);
				}
				else {openGuiCreate(message, ctx, glist);}
				break;
			}
			case 4: {
				UUID locality = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(ctx.getServerHandler().player.chunkCoordX, ctx.getServerHandler().player.chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner();
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal();
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
				Main.NET.sendTo(new MessageMarketsToGui(false, 0, market.vendList, locality, market.feeBuy, market.feeSell, balP, ""), ctx.getServerHandler().player);
				break;
			}
			default:
			}
		}
		
		private void openGuiCreate(MessageGuiRequest message, MessageContext ctx, List<Guild> glist) {
			Map<UUID, String> list = new HashMap<UUID, String>();
			for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).members.getOrDefault(ctx.getServerHandler().player.getUniqueID(), 4) == -1) list.put(glist.get(i).guildID, TextFormatting.RED+"INVITE: "+glist.get(i).guildName);
				else if (glist.get(i).openToJoin) list.put(glist.get(i).guildID, TextFormatting.BLUE+"OPEN:   "+glist.get(i).guildName);
			}
			double balance = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
			Main.NET.sendTo(new MessageCreateInfoToGui(list, balance), ctx.getServerHandler().player);
		}
	}
}

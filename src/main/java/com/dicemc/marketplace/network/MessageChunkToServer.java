package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.WhitelistItem;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;
import com.dicemc.marketplace.util.CkPktType;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.commands.TempClaimCommands;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChunkToServer implements IMessage{
	public int cX, cZ;
	public CkPktType type;
	public String name;
	public WhitelistItem wlItem;
	
	public MessageChunkToServer () {}
	
	public MessageChunkToServer(CkPktType type, int chunkX, int chunkZ, String name, WhitelistItem wlItem) {
		this.type = type;
		cX = chunkX;
		cZ = chunkZ;
		this.name = name;
		this.wlItem = wlItem;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		cX = pbuf.readInt();
		cZ = pbuf.readInt();
		this.type = CkPktType.values()[pbuf.readVarInt()];
		this.name = pbuf.readString(32);
		try {this.wlItem = new WhitelistItem(pbuf.readCompoundTag());} catch (IOException e) {}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeInt(cX);
		pbuf.writeInt(cZ);
		pbuf.writeVarInt(type.ordinal());
		pbuf.writeString(name);
		pbuf.writeCompoundTag(wlItem.toNBT());
	}
	
	public static class PacketChunkToServer implements IMessageHandler<MessageChunkToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageChunkToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageChunkToServer message, MessageContext ctx) {
			String response = "";
			switch(message.type) {
			case TEMPCLAIM: {
				response = CoreUtils.tempClaim(ctx.getServerHandler().player.getUniqueID(), message.cX, message.cZ, false);
				break;
			}
			case EXTEND: {
				response = CoreUtils.renewClaim(ctx.getServerHandler().player.getUniqueID(), message.cX, message.cZ);
				break;
			}
			case CLAIM: {
				response = CoreUtils.guildClaim(ctx.getServerHandler().player.getUniqueID(), message.cX, message.cZ);
				break;
			}
			case OUTPOST: {
				response = CoreUtils.guildOutopost(ctx.getServerHandler().player.getUniqueID(), message.cX, message.cZ);
				break;
			}
			case SELL: {
				response = CoreUtils.sellClaim(ctx.getServerHandler().player.getUniqueID(), message.cX, message.cZ, message.name);
				break;
			}
			case ABANDON: {
				response = CoreUtils.abandonClaim(ctx.getServerHandler().player.getUniqueID(), message.cX, message.cZ);
				break;
			}
			case PUBLIC: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.setPublic(cap.getPublic() ? false : true);
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				break;
			}
			case ADDMEMBER: {	
				UUID pid = ctx.getServerHandler().player.getServer().getPlayerProfileCache().getGameProfileForUsername(message.name) != null ? 
						ctx.getServerHandler().player.getServer().getPlayerProfileCache().getGameProfileForUsername(message.name).getId() : Reference.NIL;
				if (!pid.equals(Reference.NIL)) {
					UUID sid = ctx.getServerHandler().player.getUniqueID();
					ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
					if (cap.getOwner().equals(sid))	cap.includePlayer(pid);
					else if (!cap.getOwner().equals(Reference.NIL)){
						UUID gid = Reference.NIL;
						List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
						for (int i = 0; i < glist.size(); i++) {
							if (glist.get(i).members.getOrDefault(sid, -2) >= 0) {
								gid = glist.get(i).guildID;
								break;
							} 
						}
						boolean pidIsMember = glist.get(GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(gid)).members.getOrDefault(pid, -2) >= 0;
						if (cap.getOwner().equals(gid) && pidIsMember) cap.includePlayer(pid);
					}
					ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				}				
				break;
			}
			case REMOVEMEMBER: {
				UUID pid = ctx.getServerHandler().player.getServer().getPlayerProfileCache().getGameProfileForUsername(message.name) != null ? 
						ctx.getServerHandler().player.getServer().getPlayerProfileCache().getGameProfileForUsername(message.name).getId() : Reference.NIL;	
				if (pid.equals(Reference.NIL)) break;
				UUID sid = ctx.getServerHandler().player.getUniqueID();
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				int gindex = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(cap.getOwner());
				Guild guild = gindex >= 0 ? GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.get(gindex) : new Guild(Reference.NIL);
				if (cap.getOwner().equals(sid) || guild.members.getOrDefault(sid, -1) >= 0) {					
					cap.removePlayer(pid);
				}
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				break;
			}
			case WL_CHANGE: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.changeWhitelist(message.wlItem);
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				break;
			}
			case WL_CLEAR: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.fromNBTWhitelist(new NBTTagList());
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				break;
			}
			case SUBLET_INTERVAL: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.setLeaseDuration(Integer.valueOf(message.name));
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				break;
			}
			case SUBLET_PRICE: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.setLeasePrice(Integer.valueOf(message.name));
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				break;
			}
			case WL_GUILD_MIN: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.setPermMin(Integer.valueOf(message.name));
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				break;
			}
			case RENT_START: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().getBalance(ctx.getServerHandler().player.getUniqueID());
				if (balP >= cap.getLeasePrice()) {
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().addBalance(ctx.getServerHandler().player.getUniqueID(), -1*cap.getLeasePrice());
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGuilds().addBalance(cap.getOwner(), cap.getLeasePrice());
					cap.includePlayer(ctx.getServerHandler().player.getUniqueID());
					cap.setTempTime(System.currentTimeMillis()+(3600000*cap.getLeaseDuration()));
				}
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				break;
			}
			case RENT_EXTEND: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().getBalance(ctx.getServerHandler().player.getUniqueID());
				if (balP >= cap.getLeasePrice()) {
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().addBalance(ctx.getServerHandler().player.getUniqueID(), -1*(cap.getLeasePrice()*cap.getPlayers().size()));
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGuilds().addBalance(cap.getOwner(), (cap.getLeasePrice()*cap.getPlayers().size()));
					cap.setTempTime(cap.getTempTime()+(3600000*cap.getLeaseDuration()));
				}
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.cX, message.cZ).markDirty();
				AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				break;
			}
			default:
			}
			List<ChunkSummary> list = new ArrayList<ChunkSummary>();
			int cX = ctx.getServerHandler().player.chunkCoordX;
			int cZ = ctx.getServerHandler().player.chunkCoordZ;
			List<Integer> mapColors = new ArrayList<Integer>();
			List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
			int[][] colorRows = new int[176][176];
			for (int z = 0; z < 11; z++) {
				for (int x = 0; x < 11; x++) {
					int modX = x - 5;
					int modZ = z - 5;
					//ChunkSummary(String owner, double price, boolean redstone, boolean isPublic, boolean isForSale, boolean isOutpost, long claimEnd, List<String> whitelist, List<UUID> members)
					Chunk ck = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(cX + modX, cZ + modZ);
					ChunkCapability cap = ck.getCapability(ChunkProvider.CHUNK_CAP, null);
					String ownerName = TempClaimCommands.ownerName(ctx.getServerHandler().player.getUniqueID(), cap, ctx.getServerHandler().player.getServer());
					boolean guildOwned = false;
					for (int i = 0; i < glist.size(); i++) {if (cap.getOwner().equals(glist.get(i).guildID)) {guildOwned = true; break;}}
					List<String> plist = new ArrayList<String>();
					for (int i = 0; i < cap.getPlayers().size(); i++) {	plist.add(Commands.playerNamefromUUID(ctx.getServerHandler().player.getServer(), cap.getPlayers().get(i)));}
					list.add(new ChunkSummary(guildOwned, cap.getOwner(), ownerName, cap.getPrice(), cap.getPublic(), cap.getForSale(), cap.getOutpost(), cap.getTempTime(), cap.getLeaseDuration(), cap.getWhitelist(), plist, cap.getLeasePrice(), cap.getPermMin()));
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
			for (int a = 0; a < 176; a++) {
				for (int b = 0; b < 176; b++) {
					mapColors.add(colorRows[a][b]);
				}
			}
			Guild myGuild = new Guild(Reference.NIL);
			for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).members.getOrDefault(ctx.getServerHandler().player.getUniqueID(), -2) >= 0) {
					myGuild = glist.get(i);
					break;
				} 
			}
			double balG = myGuild.guildID != Reference.NIL ? AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGuilds().getBalance(myGuild.guildID) : 0;
			double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().getBalance(ctx.getServerHandler().player.getUniqueID());
			Main.NET.sendTo(new MessageChunkToGui(true ,myGuild, list, mapColors, response, balP, balG, Main.ModConfig.TEMPCLAIM_RATE), ctx.getServerHandler().player);
		}
	}
}

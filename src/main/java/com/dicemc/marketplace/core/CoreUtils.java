package com.dicemc.marketplace.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.gui.GuiMarketManager;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class CoreUtils {
	public static World world = null;
	
	public static void setWorld(World world) {CoreUtils.world = world;}

	public static String tempClaim(UUID owner, int chunkX, int chunkZ) {
		ChunkCapability cap = world.getChunkFromChunkCoords(chunkX, chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) {
			if (AccountSaver.get(world).PLAYERS.getBalance(owner) >= (cap.getPrice()*0.1)) {
				AccountSaver.get(world).PLAYERS.addBalance(owner, (-0.1*cap.getPrice()));
				cap.setTempTime(System.currentTimeMillis()+Main.ModConfig.TEMPCLAIM_DURATION);
				cap.includePlayer(owner);
				cap.setOwner(owner);
				AccountSaver.get(world).markDirty();
				return "Chunk Claimed";
			}
			else return "Insufficient Funds to make Temporary Claim.";
		}
		else return "This land is already claimed.";
	}
	
	public static String renewClaim(UUID owner,int chunkX, int chunkZ) {
		ChunkCapability cap = world.getChunkFromChunkCoords(chunkX, chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null);
		double cost = (0.1*cap.getPrice())+(.01*(cap.getPlayers().size()-1));
		if (AccountSaver.get(world).PLAYERS.getBalance(owner) >= cost) {
			AccountSaver.get(world).PLAYERS.addBalance(owner, (-1*cost));
			cap.setTempTime(cap.getTempTime()+Main.ModConfig.TEMPCLAIM_DURATION);
			return "Claim extended until "+String.valueOf(new Timestamp(cap.getTempTime()))+" for $"+String.valueOf(cost);
		}
		else return"Insuficient funds to renew claim.";
	}
	
	public static String guildClaim(UUID owner,int chunkX, int chunkZ) {
		List<Guild> glist = GuildSaver.get(world).GUILDS;
		int gindex = -1;
		boolean restricted = false;
		for (int i = 0; i < glist.size(); i++) {
			if (glist.get(i).members.containsKey(owner)) {
				if (glist.get(i).members.get(owner) >= 0) gindex = i;
				break;
			}
		}
		if (gindex >= 0 ) {
			if (AccountSaver.get(world).GUILDS.getBalance(glist.get(gindex).guildID) < 0) restricted = true;
		}
		if (gindex >= 0) {
			if (!restricted) {
				boolean bordersGuildLand = false;
				boolean bordersOutpost = false;
				UUID owningGuild = GuildSaver.get(world).GUILDS.get(gindex).guildID;
				Chunk ck = world.getChunkFromChunkCoords(chunkX, chunkZ);
				ChunkCapability cap = ck.getCapability(ChunkProvider.CHUNK_CAP, null);
				//checks if the desired claim borders existing land, or passes true if the guild owns no land.
				if (GuildSaver.get(world).GUILDS.get(gindex).coreLand.size() == 0) bordersGuildLand = true;
				else if (world.getChunkFromChunkCoords(chunkX -1 , chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(owningGuild)) {
					bordersGuildLand = true;
					if (world.getChunkFromChunkCoords(chunkX -1 , chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost()) bordersOutpost = true;
				}
				else if (world.getChunkFromChunkCoords(chunkX +1 , chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(owningGuild)) {
					bordersGuildLand = true;
					if (world.getChunkFromChunkCoords(chunkX +1 , chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost()) bordersOutpost = true;
				}
				else if (world.getChunkFromChunkCoords(chunkX, chunkZ -1).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(owningGuild)) {
					bordersGuildLand = true;
					if (world.getChunkFromChunkCoords(chunkX, chunkZ -1).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost()) bordersOutpost = true;
				}
				else if (world.getChunkFromChunkCoords(chunkX, chunkZ +1).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(owningGuild)) {
					bordersGuildLand = true;
					if (world.getChunkFromChunkCoords(chunkX, chunkZ +1).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost()) bordersOutpost = true;
				}
				if (bordersGuildLand) {
					if (GuildSaver.get(world).guildNamefromUUID(cap.getOwner()).equalsIgnoreCase("Guild N/A") || cap.getForSale()) {
						if (AccountSaver.get(world).GUILDS.getBalance(owningGuild) >= cap.getPrice()) {
							if (!cap.getOwner().equals(Reference.NIL)) {
								AccountSaver.get(world).PLAYERS.addBalance(cap.getOwner(), cap.getPrice()*.1);
								cap.setPlayers(new ArrayList<UUID>());
								cap.setWhitelist(new NBTTagList());
								cap.setPublic(false);
								cap.setPublicRedstone(false);
							}
							if (cap.getForSale()) {
								AccountSaver.get(world).GUILDS.addBalance(cap.getOwner(), cap.getPrice());
								int sellerIndex = GuildSaver.get(world).guildIndexFromName(GuildSaver.get(world).guildNamefromUUID(cap.getOwner()));
								GuildSaver.get(world).GUILDS.get(sellerIndex).removeLand(ck.getPos());
								landShiftChecker(sellerIndex);
								cap.setOutpost(false);
								cap.setWhitelist(new NBTTagList());
								cap.setPlayers(new ArrayList<UUID>());
								cap.setPublic(false);
								cap.setPublicRedstone(false);
							}
							AccountSaver.get(world).GUILDS.addBalance(owningGuild, (-1* cap.getPrice()));
							cap.setOwner(owningGuild);
							cap.setForSale(false);
							if (bordersOutpost) {
								cap.setOutpost(true);
								GuildSaver.get(world).GUILDS.get(gindex).outpostLand.add(ck.getPos());
							}
							else if (!bordersOutpost) GuildSaver.get(world).GUILDS.get(gindex).coreLand.add(ck.getPos());
							world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
							GuildSaver.get(world).markDirty();
							AccountSaver.get(world).markDirty();
							return "Claimed Chunk ("+String.valueOf(chunkX)+","+String.valueOf(chunkZ)+") for "+GuildSaver.get(world).GUILDS.get(gindex).guildName;
						}
						else return "Insufficient funds in guild account.";
					}
					else return "This chunk is already owned by: "+ Commands.playerNamefromUUID(world.getMinecraftServer(), cap.getOwner())+", and is not for sale.";
				}
				else return "Additional claims must border existing guild territory.";
			}
			else return "Your guild is currently restricted due to taxes.  Action denied";
		}
		else return "You must be in a guild to use this command.  try /tempclaim";
	}

	public static String guildOutopost(UUID owner,int chunkX, int chunkZ) {
		List<Guild> glist = GuildSaver.get(world).GUILDS;
		int gindex = -1;
		boolean restricted = false;
		for (int i = 0; i < glist.size(); i++) {
			if (glist.get(i).members.containsKey(owner)) {
				if (glist.get(i).members.get(owner) >= 0) gindex = i;
				break;
			}
		}
		if (gindex >= 0 ) {
			if (AccountSaver.get(world).GUILDS.getBalance(glist.get(gindex).guildID) < 0) restricted = true;
		}
		if (gindex >= 0) {
			if (!restricted) {
				UUID owningGuild = GuildSaver.get(world).GUILDS.get(gindex).guildID;
				Chunk ck = world.getChunkFromChunkCoords(chunkX, chunkZ);
				ChunkCapability cap = ck.getCapability(ChunkProvider.CHUNK_CAP, null);
				if (GuildSaver.get(world).guildNamefromUUID(cap.getOwner()).equalsIgnoreCase("Guild N/A") || cap.getForSale()) {
					if (AccountSaver.get(world).GUILDS.getBalance(owningGuild) >= cap.getPrice()+2000) {
						if (!cap.getOwner().equals(Reference.NIL)) {
							AccountSaver.get(world).PLAYERS.addBalance(cap.getOwner(), cap.getPrice()*.1);
						}
						if (cap.getForSale()) {
							AccountSaver.get(world).GUILDS.addBalance(cap.getOwner(), cap.getPrice());
							int sellerIndex = GuildSaver.get(world).guildIndexFromName(GuildSaver.get(world).guildNamefromUUID(cap.getOwner()));
							GuildSaver.get(world).GUILDS.get(sellerIndex).removeLand(ck.getPos());
							landShiftChecker(sellerIndex);
						}
						AccountSaver.get(world).GUILDS.addBalance(owningGuild, (-1* (cap.getPrice()+2000)));
						cap.setOwner(owningGuild);
						cap.setOutpost(true);
						GuildSaver.get(world).GUILDS.get(gindex).outpostLand.add(ck.getPos());
						world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
						GuildSaver.get(world).markDirty();
						AccountSaver.get(world).markDirty();
						return "Established Outpost at ("+String.valueOf(chunkX)+","+String.valueOf(chunkZ)+") for "+GuildSaver.get(world).GUILDS.get(gindex).guildName;
					}
					else return "Insufficient funds in guild account.";
				}
				else return "This chunk is already owned by: "+ Commands.playerNamefromUUID(world.getMinecraftServer(), cap.getOwner())+", and is not for sale.";
			}
			else return "Your guild is currently restricted due to taxes.  Action denied.";
		}
		else return "You must be in a guild to use this command.  try /tempclaim";
	}

	public static String sellClaim(UUID owner,int chunkX, int chunkZ, String price) {
		List<Guild> glist = GuildSaver.get(world).GUILDS;
		int gindex = -1;
		boolean restricted = false;
		for (int i = 0; i < glist.size(); i++) {
			if (glist.get(i).members.containsKey(owner)) {
				if (glist.get(i).members.get(owner) >= 0) gindex = i;
				break;
			}
		}
		if (gindex >= 0 ) {
			if (AccountSaver.get(world).GUILDS.getBalance(glist.get(gindex).guildID) < 0) restricted = true;
		}
		ChunkCapability cap = world.getChunkFromChunkCoords(chunkX, chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null);
		cap.setForSale(true);
		cap.setPrice(Math.abs(Double.valueOf(price)));
		world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
		return "Chunk has been listed for $"+String.valueOf(Math.abs(Double.valueOf(price)));
	}
	
	public static String abandonClaim(UUID owner,int chunkX, int chunkZ) {
		List<Guild> glist = GuildSaver.get(world).GUILDS;
		int gindex = -1;
		boolean restricted = false;
		for (int i = 0; i < glist.size(); i++) {
			if (glist.get(i).members.containsKey(owner)) {
				if (glist.get(i).members.get(owner) >= 0) gindex = i;
				break;
			}
		}
		Chunk ck = world.getChunkFromChunkCoords(chunkX, chunkZ);
		ChunkCapability cap = ck.getCapability(ChunkProvider.CHUNK_CAP, null);
		String resp = "";
		if (cap.getOwner().equals(glist.get(gindex).guildID)) {
			if (!cap.getForSale()) {
				AccountSaver.get(world).GUILDS.addBalance(glist.get(gindex).guildID, cap.getPrice()*.75);
				resp = "Guild has been refunded $"+String.valueOf(cap.getPrice()*.75);
			}
			GuildSaver.get(world).GUILDS.get(GuildSaver.get(world).guildIndexFromUUID(cap.getOwner())).removeLand(ck.getPos());
			landShiftChecker(GuildSaver.get(world).guildIndexFromUUID(cap.getOwner()));
			cap.setOwner(Reference.NIL);
			cap.setPublicRedstone(false);
			cap.setPublic(false);
			cap.setWhitelist(new NBTTagList());
			cap.setForSale(false);
			cap.setOutpost(false);
			cap.setPlayers(new ArrayList<UUID>());			
			GuildSaver.get(world).markDirty();
			ck.markDirty();
			resp = "Chunk has been abandoned. "+resp;
		}
		return resp;
	}

	public static String createGuild(UUID creator, String name) {
		AccountSaver.get(world).PLAYERS.addBalance(creator, -1*Main.ModConfig.GUILD_CREATE_COST);
		AccountSaver.get(world).markDirty();
		GuildSaver.get(world).GUILDS.add(new Guild(name));
		GuildSaver.get(world).GUILDS.get(GuildSaver.get(world).guildIndexFromName(name)).addMember(creator, 0);
		GuildSaver.get(world).markDirty();
		AccountSaver.get(world).GUILDS.addAccount(GuildSaver.get(world).guildUUIDfromName(name), Main.ModConfig.GUILD_STARTING_FUNDS);
		AccountSaver.get(world).markDirty();
		return "Created new guild: " + name;
	}
	
	public static String joinGuild(UUID guild, UUID joiner) {
		int gid = GuildSaver.get(world).guildIndexFromUUID(guild);
		GuildSaver.get(world).GUILDS.get(gid).addMember(joiner, 3);
		GuildSaver.get(world).markDirty();
		for (EntityPlayer player : world.getMinecraftServer().getPlayerList().getPlayers()) {
			if (GuildSaver.get(world).GUILDS.get(gid).members.getOrDefault(player.getUniqueID(), -1) >= 0) {
				player.sendMessage(new TextComponentString(world.getPlayerEntityByUUID(joiner).getDisplayNameString()+ " has joined the guild"));}
		}
		return "You have joined "+GuildSaver.get(world).GUILDS.get(gid).guildName+"!";
	}
	
	public static String guildNamefromUUID (MinecraftServer server, UUID guild) {	
		for (Guild g : GuildSaver.get(server.getEntityWorld()).GUILDS) {
			if (g.guildID.equals(guild)) return g.guildName;
		}
		return "GuildNotFound";
	}
	
	public static void landShiftChecker(int sellerIndex) {
		Guild guild = GuildSaver.get(world).GUILDS.get(sellerIndex);
		if (guild.coreLand.isEmpty()) {
			if (!guild.outpostLand.isEmpty()) {
				guild.coreLand.add(guild.outpostLand.get(0));
				guild.outpostLand.remove(0);
				boolean noMore = false;
				while (!noMore) {	
					noMore = true;
					for (int o = guild.outpostLand.size()-1; o >= 0; o--) {
						ChunkPos ck1 = guild.outpostLand.get(o);
						for (int c = 0; c < guild.coreLand.size(); c++) {
							ChunkPos ck2 = guild.coreLand.get(c);
							if ((ck1.x == ck2.x && (ck1.z-1 == ck2.z || ck1.z+1 == ck2.z)) || (ck1.z == ck2.z && (ck1.x-1 == ck2.x || ck1.x+1 == ck2.x))) {
								guild.coreLand.add(guild.outpostLand.get(o));
								guild.outpostLand.remove(o);
								noMore = false;
								break;
							}
						}						
					}
				}
			}
			else if (guild.outpostLand.isEmpty()) {
				for (Map.Entry<UUID, MarketItem> sales : MarketSaver.get(world).getLocal().vendList.entrySet()) {
					if (sales.getValue().locality.equals(guild.guildID)) sales.getValue().locality = Reference.NIL;
				}
			}
		}
	}

	//returns true if the keycode is a number key, 
	public static boolean validNumberKey(int keyCode) {
		return ((keyCode >= 2 && keyCode <= 11) || keyCode == 14 || keyCode == 203 || keyCode == 205 || keyCode == 211 
				|| keyCode == 71 || keyCode == 72 || keyCode == 73 || keyCode == 75 || keyCode == 76 || keyCode == 77
				|| keyCode == 79 || keyCode == 80 || keyCode == 81 || keyCode == 82 || keyCode == 52 || keyCode == 83);		
	}
}

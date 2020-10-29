package com.dicemc.marketplace.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class CoreUtils {
	public static World world = null;
	
	public static void setWorld(World world) {CoreUtils.world = world;}

	public static String tempClaim(UUID owner, int chunkX, int chunkZ) {
		ChunkCapability cap = world.getChunkFromChunkCoords(chunkX, chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) {
			if (AccountSaver.get(world).getPlayers().getBalance(owner) >= (cap.getPrice()*Main.ModConfig.TEMPCLAIM_RATE)) {
				AccountSaver.get(world).getPlayers().addBalance(owner, (-1*Main.ModConfig.TEMPCLAIM_RATE*cap.getPrice()));
				cap.setTempTime(System.currentTimeMillis()+Main.ModConfig.TEMPCLAIM_DURATION);
				cap.includePlayer(owner);
				cap.setOwner(owner);
				cap.setExplosionsOn(false);
				AccountSaver.get(world).markDirty();
				return new TextComponentTranslation("core.utils.tempclaim.success").getFormattedText();
			}
			else return new TextComponentTranslation("core.utils.tempclaim.failfunds").getFormattedText();
		}
		else return new TextComponentTranslation("core.utils.tempclaim.failowned").getFormattedText();
	}
	
	public static String renewClaim(UUID owner,int chunkX, int chunkZ) {
		ChunkCapability cap = world.getChunkFromChunkCoords(chunkX, chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null);
		double cost = (0.1*cap.getPrice())+(.01*(cap.getPlayers().size()-1));
		if (AccountSaver.get(world).getPlayers().getBalance(owner) >= cost) {
			AccountSaver.get(world).getPlayers().addBalance(owner, (-1*cost));
			cap.setTempTime(cap.getTempTime()+Main.ModConfig.TEMPCLAIM_DURATION);
			return new TextComponentTranslation("core.utils.renewclaim.success", String.valueOf(new Timestamp(cap.getTempTime())), String.valueOf(cost)).getFormattedText();
		}
		else return new TextComponentTranslation("core.utils.renewclaim.failfunds").getFormattedText();
	}
	
	public static String guildClaim(UUID owner,int chunkX, int chunkZ) {
		List<Guild> glist = GuildSaver.get(world).GUILDS;
		int gindex = -1;
		boolean restricted = false;
		for (int i = 0; i < glist.size(); i++) {
			if (glist.get(i).members.containsKey(owner)) {
				if (glist.get(i).members.get(owner) >= 0) {gindex = i; break;}				
			}
		}
		if (gindex >= 0 ) {
			if (AccountSaver.get(world).getGuilds().getBalance(glist.get(gindex).guildID) < 0) restricted = true;
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
						if (AccountSaver.get(world).getGuilds().getBalance(owningGuild) >= cap.getPrice()) {
							if (!cap.getOwner().equals(Reference.NIL) && GuildSaver.get(world).guildNamefromUUID(cap.getOwner()).equalsIgnoreCase("Guild N/A")) {
								AccountSaver.get(world).getPlayers().addBalance(cap.getOwner(), cap.getPrice()*.1);
								cap.setPlayers(new ArrayList<UUID>());
								cap.fromNBTWhitelist(new NBTTagList());
								cap.setPublic(false);
							}
							if (cap.getForSale()) {
								AccountSaver.get(world).getGuilds().addBalance(cap.getOwner(), cap.getPrice());
								int sellerIndex = GuildSaver.get(world).guildIndexFromName(GuildSaver.get(world).guildNamefromUUID(cap.getOwner()));
								GuildSaver.get(world).GUILDS.get(sellerIndex).removeLand(ck.getPos());
								landShiftChecker(sellerIndex);
								cap.setOutpost(false);
								cap.fromNBTWhitelist(new NBTTagList());
								cap.setPlayers(new ArrayList<UUID>());
								cap.setPublic(false);
							}
							AccountSaver.get(world).getGuilds().addBalance(owningGuild, (-1* cap.getPrice()));
							cap.setOwner(owningGuild);
							cap.setForSale(false);
							cap.setExplosionsOn(false);
							cap.setTempTime(System.currentTimeMillis());
							if (bordersOutpost) {
								cap.setOutpost(true);
								GuildSaver.get(world).GUILDS.get(gindex).outpostLand.add(ck.getPos());
							}
							else if (!bordersOutpost) GuildSaver.get(world).GUILDS.get(gindex).coreLand.add(ck.getPos());
							world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
							GuildSaver.get(world).markDirty();
							AccountSaver.get(world).markDirty();
							return new TextComponentTranslation("core.utils.claim.success", String.valueOf(chunkX)+","+String.valueOf(chunkZ), GuildSaver.get(world).GUILDS.get(gindex).guildName).getFormattedText();
						}
						else return new TextComponentTranslation("core.utils.claim.failfunds").getFormattedText();
					}
					else return new TextComponentTranslation("core.utils.claim.failowned", Commands.playerNamefromUUID(world.getMinecraftServer(), cap.getOwner())).getFormattedText();
				}
				else return new TextComponentTranslation("core.utils.claim.failborder").getFormattedText();
			}
			else return new TextComponentTranslation("core.utils.claim.faildebt").getFormattedText();
		}
		else return new TextComponentTranslation("core.utils.claim.failnoguild").getFormattedText();
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
			if (AccountSaver.get(world).getGuilds().getBalance(glist.get(gindex).guildID) < 0) restricted = true;
		}
		if (gindex >= 0) {
			if (!restricted) {
				UUID owningGuild = GuildSaver.get(world).GUILDS.get(gindex).guildID;
				Chunk ck = world.getChunkFromChunkCoords(chunkX, chunkZ);
				ChunkCapability cap = ck.getCapability(ChunkProvider.CHUNK_CAP, null);
				if (GuildSaver.get(world).guildNamefromUUID(cap.getOwner()).equalsIgnoreCase("Guild N/A") || cap.getForSale()) {
					if (AccountSaver.get(world).getGuilds().getBalance(owningGuild) >= cap.getPrice()+2000) {
						if (!cap.getOwner().equals(Reference.NIL)) {
							AccountSaver.get(world).getPlayers().addBalance(cap.getOwner(), cap.getPrice()*.1);
						}
						if (cap.getForSale()) {
							AccountSaver.get(world).getGuilds().addBalance(cap.getOwner(), cap.getPrice());
							int sellerIndex = GuildSaver.get(world).guildIndexFromName(GuildSaver.get(world).guildNamefromUUID(cap.getOwner()));
							GuildSaver.get(world).GUILDS.get(sellerIndex).removeLand(ck.getPos());
							landShiftChecker(sellerIndex);
						}
						AccountSaver.get(world).getGuilds().addBalance(owningGuild, (-1* (cap.getPrice()+Main.ModConfig.OUTPOST_CREATE_COST)));
						cap.setOwner(owningGuild);
						cap.setOutpost(true);
						cap.setTempTime(System.currentTimeMillis());
						GuildSaver.get(world).GUILDS.get(gindex).outpostLand.add(ck.getPos());
						world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
						GuildSaver.get(world).markDirty();
						AccountSaver.get(world).markDirty();
						return new TextComponentTranslation("core.utils.outpost.success", String.valueOf(chunkX)+","+String.valueOf(chunkZ), GuildSaver.get(world).GUILDS.get(gindex).guildName).getFormattedText();
					}
					else return new TextComponentTranslation("core.utils.claim.failfunds").getFormattedText();
				}
				else return new TextComponentTranslation("core.utils.claim.failowned", Commands.playerNamefromUUID(world.getMinecraftServer(), cap.getOwner())).getFormattedText();
			}
			else return new TextComponentTranslation("core.utils.claim.faildebt").getFormattedText();
		}
		else return new TextComponentTranslation("core.utils.claim.failnoguild").getFormattedText();
	}

	public static String sellClaim(UUID owner,int chunkX, int chunkZ, String price) {
		List<Guild> glist = GuildSaver.get(world).GUILDS;
		int gindex = -1;
		for (int i = 0; i < glist.size(); i++) {
			if (glist.get(i).members.containsKey(owner)) {
				if (glist.get(i).members.get(owner) >= 0) gindex = i;
				break;
			}
		}
		if (gindex >= 0 ) {
			if (AccountSaver.get(world).getGuilds().getBalance(glist.get(gindex).guildID) < 0) {
			}
		}
		ChunkCapability cap = world.getChunkFromChunkCoords(chunkX, chunkZ).getCapability(ChunkProvider.CHUNK_CAP, null);
		cap.setForSale(true);
		cap.setPrice(Math.abs(Double.valueOf(price)));
		world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
		return new TextComponentTranslation("core.utils.sell", String.valueOf(Math.abs(Double.valueOf(price)))).getFormattedText();
	}
	
	public static String abandonClaim(UUID owner,int chunkX, int chunkZ) {
		List<Guild> glist = GuildSaver.get(world).GUILDS;
		int gindex = -1;
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
				AccountSaver.get(world).getGuilds().addBalance(glist.get(gindex).guildID, cap.getPrice()*Main.ModConfig.LAND_ABANDON_REFUND_RATE);
				resp = new TextComponentTranslation("core.utils.abandonrefund", String.valueOf(cap.getPrice()*.75)).getFormattedText();
			}
			GuildSaver.get(world).GUILDS.get(GuildSaver.get(world).guildIndexFromUUID(cap.getOwner())).removeLand(ck.getPos());
			landShiftChecker(GuildSaver.get(world).guildIndexFromUUID(cap.getOwner()));
			cap.setOwner(Reference.NIL);
			cap.setPublic(false);
			cap.fromNBTWhitelist(new NBTTagList());
			cap.setForSale(false);
			cap.setOutpost(false);
			cap.setPlayers(new ArrayList<UUID>());			
			GuildSaver.get(world).markDirty();
			ck.markDirty();
			resp = new TextComponentTranslation("core.utils.abandon").getFormattedText()+" "+resp;
		}
		return resp;
	}

	public static String createGuild(UUID creator, String name) {
		if (GuildSaver.get(world).guildIndexFromName(name) >= 0) {return new TextComponentTranslation("core.utils.create.fail").getFormattedText();}
		AccountSaver.get(world).getPlayers().addBalance(creator, -1 * Main.ModConfig.GUILD_CREATE_COST);
		GuildSaver.get(world).GUILDS.add(new Guild(name));
		GuildSaver.get(world).GUILDS.get(GuildSaver.get(world).guildIndexFromName(name)).addMember(creator, 0);
		GuildSaver.get(world).markDirty();
		AccountSaver.get(world).getGuilds().addAccount(GuildSaver.get(world).guildUUIDfromName(name), Main.ModConfig.GUILD_STARTING_FUNDS);
		AccountSaver.get(world).markDirty();
		return new TextComponentTranslation("core.utils.create.success", name).getFormattedText();
	}
	
	public static String joinGuild(UUID guild, UUID joiner) {
		int gid = GuildSaver.get(world).guildIndexFromUUID(guild);
		GuildSaver.get(world).GUILDS.get(gid).addMember(joiner, 3);
		GuildSaver.get(world).markDirty();
		for (EntityPlayer player : world.getMinecraftServer().getPlayerList().getPlayers()) {
			if (GuildSaver.get(world).GUILDS.get(gid).members.getOrDefault(player.getUniqueID(), -1) >= 0) {
				player.sendMessage(new TextComponentTranslation("core.utils.join.notify", world.getPlayerEntityByUUID(joiner).getDisplayNameString()));}
		}
		return new TextComponentTranslation("core.utils.join.success", GuildSaver.get(world).GUILDS.get(gid).guildName).getFormattedText();
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
		return ((keyCode >= 2 && keyCode <= 12) || keyCode == 14 || keyCode == 203 || keyCode == 205 || keyCode == 211 || keyCode >= 71 && keyCode <= 77 
				|| keyCode == 79 || keyCode == 80 || keyCode == 81 || keyCode == 82 || keyCode == 52 || keyCode == 83);		
	}
}

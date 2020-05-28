package com.dicemc.marketplace.util.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.AccountGroup;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiChunkManager;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;
import com.dicemc.marketplace.gui.GuiMarketSell;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class Commands extends CommandBase{

	@Override
	public String getName() { return "admin"; }

	@Override
	public String getUsage(ICommandSender sender) { return "/admin <account/market/guild>"; }
	
	private void message(String str, ICommandSender sender) {
		sender.sendMessage(new TextComponentString(str));
	}
	
	public static UUID playerUUIDfromString (MinecraftServer server, String username) {
		return (EntityPlayerMP.getUUID(server.getPlayerProfileCache().getGameProfileForUsername(username)) != null) ? EntityPlayerMP.getUUID(server.getPlayerProfileCache().getGameProfileForUsername(username)) : null;
	}
	public static String playerNamefromUUID (MinecraftServer server, UUID player) {	
		return (server.getPlayerProfileCache().getProfileByUUID(player).getName() != null) ? server.getPlayerProfileCache().getProfileByUUID(player).getName() : null;
	}
	public static String guildNamefromUUID (MinecraftServer server, UUID guild) {	
		for (Guild g : GuildSaver.get(server.getEntityWorld()).GUILDS) {
			if (g.guildID.equals(guild)) return g.guildName;
		}
		return "GuildNotFound";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (sender.canUseCommand(2, this.getName())) return true;
		return false;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		ItemStack item;
		EntityPlayerMP plyr;
		if (args.length == 0) {
			message("/admin market <local/global/auction>", sender);
			message("/admin account <player/guild>", sender);
			message("/admin guild <create/set/list/claim>", sender);
			return;
		}
		switch(args[0]) {
		case "test": {
			server.getPlayerList().getPlayerByUUID(sender.getCommandSenderEntity().getUniqueID()).openGui(Reference.MOD_ID, 1, server.getEntityWorld(), 0, 0, 0);
			System.out.println("post openGui method");
			break;
		}
		//base arguments <market/account/guild>
		case "market": {
			//TODO create paging on lists to accomodate large lists for server managers.
			Map<UUID, MarketItem> vendlist;
			switch(args[1]) {
			//base arguments <local/global/auction>
			case "local": {
				switch(args[2]) {
				//base arguments <list/add/remove>
				case "list": {
					vendlist = MarketSaver.get(server.getEntityWorld()).getLocal().vendList;
					message("Local Market Itemized List:", sender);
					for (Map.Entry<UUID, MarketItem> entry : vendlist.entrySet()) {
						String merchant = entry.getValue().vendor.equals(Reference.NIL) ? "Server Offered" : playerNamefromUUID(server, entry.getValue().vendor);
						message("["+entry.getKey().toString()+"] "+ entry.getValue().item.getDisplayName() + " $"+String.valueOf(entry.getValue().price)+ " sold by:"+merchant, sender);
					}
					break;
				}
				case "add": {
					//admin market local add <price> <infinite> <vendorGiveItem>
					plyr = (EntityPlayerMP) sender;
					item = plyr.inventory.getCurrentItem();
					MarketSaver.get(server.getEntityWorld()).getLocal().addToList(Boolean.valueOf(args[5]), item, Reference.NIL, Double.valueOf(args[3]), Boolean.valueOf(args[4]));
					message("Server Item Added", sender);
					break;
				}
				case "remove": {
					MarketSaver.get(sender.getEntityWorld()).getLocal().removeFromList(UUID.fromString(args[3]));
					message("Item removed from Local market.", sender);
					break;
				}
				default: message("Invalid Local Market Action.", sender);
				}
				break;					
			}
			case "global": {
				switch(args[2]) {
				//base arguments <list/add/remove>
				case "list": {
					vendlist = MarketSaver.get(server.getEntityWorld()).getGlobal().vendList;
					message("Global Market Itemized List:", sender);
					for (Map.Entry<UUID, MarketItem> entry : vendlist.entrySet()) {
						String merchant = entry.getValue().vendor.equals(Reference.NIL) ? "Server Offered" : playerNamefromUUID(server, entry.getValue().vendor);
						message("["+entry.getKey().toString()+"] "+ entry.getValue().item.getDisplayName() + " $"+String.valueOf(entry.getValue().price)+ " sold by:"+merchant, sender);
					}
					break;
				}
				case "add": {
					//admin market local add <price> <infinite> <vendorGiveItem>
					plyr = (EntityPlayerMP) sender;
					item = plyr.inventory.getCurrentItem();
					MarketSaver.get(server.getEntityWorld()).getGlobal().addToList(Boolean.valueOf(args[5]), item, Reference.NIL, Double.valueOf(args[3]), Boolean.valueOf(args[4]));
					message("Server Item Added", sender);
					break;
				}
				case "remove": {
					MarketSaver.get(sender.getEntityWorld()).getGlobal().removeFromList(UUID.fromString(args[3]));
					message("Item removed from Global market.", sender);
					break;
				}
				default: message("Invalid Global Market Action.", sender);
				}
				break;
			}
			case "server": {
				switch(args[2]) {
				//base arguments <list/add/remove>
				case "list": {
					vendlist = MarketSaver.get(server.getEntityWorld()).getServer().vendList;
					message("Global Market Itemized List:", sender);
					for (Map.Entry<UUID, MarketItem> entry : vendlist.entrySet()) {
						message("["+entry.getKey().toString()+"] "+ entry.getValue().item.toString() + " $"+String.valueOf(entry.getValue().price)+String.valueOf(entry.getValue().vendorGiveItem), sender);
					}
					break;
				}
				case "add": {
					//admin market local add <price> <infinite> <vendorGiveItem>
					plyr = (EntityPlayerMP) sender;
					item = plyr.inventory.getCurrentItem();
					MarketSaver.get(server.getEntityWorld()).getServer().addToList(Boolean.valueOf(args[5]), item, Reference.NIL, Double.valueOf(args[3]), Boolean.valueOf(args[4]));
					message("Server Item Added", sender);
					break;
				}
				case "remove": {
					MarketSaver.get(sender.getEntityWorld()).getServer().removeFromList(UUID.fromString(args[3]));
					message("Item removed from Server market.", sender);
					break;
				}
				default: message("Invalid Server Market Action.", sender);
				}
				break;
			}
			case "auction": case "ah":{
				switch(args[2]) {
				//base arguments <list/add/remove>
				case "list": {
					vendlist = MarketSaver.get(server.getEntityWorld()).getAuction().vendList;
					message("Auction House Itemized List:", sender);
					for (Map.Entry<UUID, MarketItem> entry : vendlist.entrySet()) {
						message("["+entry.getKey().toString()+"] "+ entry.getValue().item.getDisplayName() + " $"+String.valueOf(entry.getValue().price)+ " sold by:"+playerNamefromUUID(server, entry.getValue().vendor), sender);
					}
					break;
				}
				case "add": {
					message("Unimplemented.", sender);
					plyr = (EntityPlayerMP) sender;
					item = plyr.inventory.getCurrentItem();
					item.setCount(Integer.valueOf(args[3]));
					plyr.inventory.decrStackSize(plyr.inventory.currentItem, item.getCount());
					message(item.getDisplayName()+" vendSize="+String.valueOf(item.getCount()), sender);
					break;
				}
				case "remove": {
					MarketSaver.get(sender.getEntityWorld()).getAuction().removeFromList(UUID.fromString(args[3]));
					message("Item removed from Auction market.", sender);
					break;
				}
				case "expire": {
					MarketSaver.get(sender.getEntityWorld()).getAuction().vendList.get(UUID.fromString(args[3])).bidEnd = System.currentTimeMillis();
					message("Expired auction sale at index ["+args[3]+"]", sender);
					break;
				}
				default: message("Invalid Auction Market Action.", sender);
				}	
				break;
			}
			default: message("Invalid market type. ", sender);
			}
			break;				
		}
		case "account": {
			switch(args[1]) {
			//base arguments <player/guild>
			case "player": {
				AccountGroup acctPlayers = AccountSaver.get(server.getEntityWorld()).PLAYERS;
				switch(args[2]) {
				//base arguments <balance/set/add>
				case "balance": {
					if (args[3].equalsIgnoreCase("all")) {
						message("[0]ServerPlayer $"+String.valueOf(acctPlayers.accountList.get(0).balance), sender);
						for (int i = 1; i < acctPlayers.accountList.size(); i++) {
							message("["+String.valueOf(i)+"]"+server.getPlayerProfileCache().getProfileByUUID(acctPlayers.accountList.get(i).owner).getName()+" $"+String.valueOf(acctPlayers.accountList.get(i).balance), sender);
						}
					}
					else {
						double x = acctPlayers.getBalance(playerUUIDfromString(server, args[3]));
						message("Balance for " + args[3] + " = $" + Double.toString(x), sender);
					}
					break;
				}
				case "set": {
					acctPlayers.setBalance(playerUUIDfromString(server, args[3]), Double.valueOf(args[4]));
					AccountSaver.get(sender.getEntityWorld()).markDirty();
					message(args[3]+" account set to $"+args[4], sender);
					break;
				}
				case "add": {
					acctPlayers.addBalance(playerUUIDfromString(server, args[3]), Double.valueOf(args[4]));
					AccountSaver.get(sender.getEntityWorld()).markDirty();
					message("$"+args[4]+" added to "+args[3]+"'s account.", sender);
					break;
				}
				default: message("Invalid player account action.", sender);
				}			
				break;
			}
			case "guild": {
				AccountGroup acctGuilds = AccountSaver.get(server.getEntityWorld()).GUILDS;
				switch(args[2]) {
				//base arguments <balance/set/add>
				case "balance": {
					if (args[3].equalsIgnoreCase("all")) {
						message("[0]ServerGuild $"+String.valueOf(acctGuilds.accountList.get(0).balance), sender);
						for (int i = 1; i < acctGuilds.accountList.size(); i++) {
							message("["+String.valueOf(i)+"]"+GuildSaver.get(sender.getEntityWorld()).guildNamefromUUID(acctGuilds.accountList.get(i).owner)+" $"+String.valueOf(acctGuilds.accountList.get(i).balance), sender);
						}
					}
					else {
						double x = acctGuilds.getBalance(GuildSaver.get(sender.getEntityWorld()).guildUUIDfromName(args[3]));
						message("Balance for " + args[3] + " = $" + Double.toString(x), sender);
					}
					break;
				}
				case "set": {
					message("Unimplemented.", sender);
					break;
				}
				case "add": {
					message("Unimplemented.", sender);
					break;
				}
				default: message("Invalid guild account action.", sender);
				}
				break;
			}
			default: message("Invalid account type.", sender);
			}
			break;
		}
		case "guild": {
			List<Guild> glist = GuildSaver.get(sender.getEntityWorld()).GUILDS;
			switch(args[1]) {
			//TODO: probably want a player management option.
			//base arguments <create/set/list/claim>
			case "create": {
				GuildSaver.get(sender.getEntityWorld()).GUILDS.add(new Guild(args[2]));
				message("Guild "+args[2]+" created.", sender);
				break;
			}
			case "listchunks": {
				message("Core Land:", sender);
				for (ChunkPos c : GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[2])).coreLand) {
					message("("+String.valueOf(c.x)+","+String.valueOf(c.z)+")", sender);
				}
				message("Outpost Land:", sender);
				for (ChunkPos c : GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[2])).outpostLand) {
					message("("+String.valueOf(c.x)+","+String.valueOf(c.z)+")", sender);
				}
				break;
			}
			case "set": {
				switch(args[2]) {
				//base arguments <name/open/tax/permissions>
				case "name": {
					GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[3])).guildName = args[4];
					GuildSaver.get(sender.getEntityWorld()).markDirty();
					message("Guild name changed to "+GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[3])).guildName, sender);
					break;
				}
				case "open": {
					GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[3])).openToJoin = Boolean.valueOf(args[4]);
					GuildSaver.get(sender.getEntityWorld()).markDirty();
					message("Guild open status now "+args[4], sender);
					break;
				}
				case "tax": {
					GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[3])).guildTax = Double.valueOf(args[4]);
					GuildSaver.get(sender.getEntityWorld()).markDirty();
					message("Guild tax is now: "+ String.valueOf(Double.valueOf(args[4])*100)+"%", sender);
					break;
				}
				case "permname": {
					List<String> list = new ArrayList<String>();
					if (args[4].equalsIgnoreCase("0") || args[4].equalsIgnoreCase("1") || args[4].equalsIgnoreCase("2") || args[4].equalsIgnoreCase("3")) {
						glist.get(Integer.valueOf(args[3])).permLevels.put(Integer.valueOf(args[4]), args[5]);
						GuildSaver.get(sender.getEntityWorld()).markDirty();
					}
					else message("Permission level number not recognized.", sender);
					break;
				}
				default: message("Invalid guild setting option.", sender);
				}
				break;
			}
			case "promote": {
				glist.get(Integer.valueOf(args[1])).promoteMember(playerUUIDfromString(server, args[2]));
				GuildSaver.get(sender.getEntityWorld()).markDirty();
				message("Promoted "+args[2]+"! (not if player is already in top level there will be no change.)", sender);
				break;
			}
			case "demote": {
				glist.get(Integer.valueOf(args[1])).demoteMember(Commands.playerUUIDfromString(server, args[2]));
				GuildSaver.get(sender.getEntityWorld()).markDirty();
				message("Demoted "+args[2]+"! (not if player is already in bottom level there will be no change.)", sender);
				break;
			}
			case "list": {
				List<Guild> list = GuildSaver.get(sender.getEntityWorld()).GUILDS;
				message("Guild List:", sender);
				for (int i = 0; i < list.size(); i++) {
					int membercount = 0;
					for (Map.Entry<UUID, Integer> entry : list.get(i).members.entrySet()) membercount += entry.getValue() != -1 ? 1: 0;
					membercount *= Main.ModConfig.CHUNKS_PER_MEMBER;
					message("["+String.valueOf(i)+"]"+list.get(i).guildName +" :Open="+String.valueOf(list.get(i).openToJoin) +", Chunks: "+ String.valueOf(list.get(i).coreLand.size()+list.get(i).outpostLand.size())+"/"+String.valueOf(membercount) + "(size:"+String.valueOf(list.get(i).members.size())+")", sender);
				}
				break;
			}
			case "claim": {
				UUID owningGuild = GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[2])).guildID;
				int cX = sender.getCommandSenderEntity().chunkCoordX;
				int cZ = sender.getCommandSenderEntity().chunkCoordZ;
				ChunkCapability cap = sender.getCommandSenderEntity().getEntityWorld().getChunkFromChunkCoords(cX, cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.setOwner(owningGuild);
				sender.getCommandSenderEntity().getEntityWorld().getChunkFromChunkCoords(cX, cZ).markDirty();
				message("Claimed Chunk ("+String.valueOf(cX)+","+String.valueOf(cZ)+") for "+GuildSaver.get(sender.getEntityWorld()).GUILDS.get(Integer.valueOf(args[2])).guildName , sender);
				break;
			}
			case "info": {
				int gid = -1;
				String gname = args[3];
				for (int n = 4; n < args.length; n++) {gname += " "+args[n];}
				for (int i = 0; i < glist.size(); i++) {
					if (glist.get(i).guildName.equalsIgnoreCase(gname)) gid = i; }
				message("loading guild at index: "+String.valueOf(gid), sender);
				if (gid >= 0) {
					message(TextFormatting.GOLD+"==="+glist.get(gid).guildName+"===", sender);
					int membercount = 0;
					for (Map.Entry<UUID, Integer> entry : glist.get(gid).members.entrySet()) membercount += entry.getValue() != -1 ? 1: 0;
					membercount *= 25;
					message("Tax: "+String.valueOf(glist.get(gid).guildTax*100)+"%,"+TextFormatting.AQUA+" Open-to-Join: "+String.valueOf(glist.get(gid).openToJoin)+TextFormatting.WHITE+", Chunks Claimed: "+String.valueOf(glist.get(gid).coreLand.size()+glist.get(gid).outpostLand.size())+"/"+String.valueOf(membercount), sender);
					message(TextFormatting.DARK_GREEN+"Balance: $"+String.valueOf(AccountSaver.get(sender.getEntityWorld()).GUILDS.getBalance(glist.get(gid).guildID)), sender);
					message("Member List:", sender);
					message(TextFormatting.AQUA+glist.get(gid).permLevels.get(0)+":", sender);
						List<String> list = glist.get(gid).listMembers(0, server);
						try {for (String name : list) message(name, sender);} catch(NullPointerException e){}
					message(TextFormatting.AQUA+glist.get(gid).permLevels.get(1)+":", sender);
						list = glist.get(gid).listMembers(1, server);
						try {for (String name : list) message(name, sender);} catch(NullPointerException e){} 
					message(TextFormatting.AQUA+glist.get(gid).permLevels.get(2)+":", sender);
						list = glist.get(gid).listMembers(2, server);
						try {for (String name : list) message(name, sender);} catch(NullPointerException e){} 
					message(TextFormatting.AQUA+glist.get(gid).permLevels.get(3)+":", sender);
						list = glist.get(gid).listMembers(3, server);
						try {for (String name : list) message(name, sender);} catch(NullPointerException e){} 
				}
				else message("Unable to find guild by that name.", sender);
				break;
			}
			default: message("Guild option not recognized.", sender);
			}
			break;
		}
		default: message("Invalid Argument.", sender);
		}
	}

}

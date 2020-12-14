package com.dicemc.marketplace.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.util.capabilities.ChunkCapability;

import net.minecraft.entity.Entity;

public class ProtectionChecker {

	public static enum matchType {FULL, DENIED, WHITELIST}
	enum playerType {UNSET, ULNM, LNM, LM, HRM, LRM}
	//elongated: UnListed Non-Member, Listed Non-Member, Listed Member, High Rank Member, Low Rank Member
	
	/**Checks if the player exists in the chunk data as a permitted interactor.
	 *  
	 * @param player the player who is being checked 
	 * @param cap the capability of the chunk being checked
	 * @param glist the server's list of guilds
	 * @return
	 */
	public static matchType ownerMatch(UUID player, ChunkCapability cap, List<Guild> glist) {
		if (cap.getPublic()) return matchType.FULL;
		boolean isGuildOwned = false;
		int gindex = -1;
		for (int i = 0; i < glist.size(); i++) { if (glist.get(i).guildID.equals(cap.getOwner())) {isGuildOwned = true; gindex = i; break;}}
		if (!isGuildOwned) for (UUID plyr : cap.getPlayers()) {if (plyr.equals(player)) {return matchType.FULL;}}
		if (isGuildOwned && !cap.getForSale()) {
			playerType pt = playerType.UNSET;
			if (glist.get(gindex).members.getOrDefault(player, -1) == -1) {
				for (UUID plyrs : cap.getPlayers()) {if (plyrs.equals(player)) {pt = playerType.LNM; break;}}
				if (pt == playerType.UNSET) pt = playerType.ULNM;
			}
			else {
				for (UUID plyrs : cap.getPlayers()) {if (plyrs.equals(player)) {pt = playerType.LM; break;}}
				if (pt == playerType.UNSET && glist.get(gindex).members.getOrDefault(player, 4) <= cap.getPermMin()) pt = playerType.HRM;
				else if (pt == playerType.UNSET) pt = playerType.LRM;
			}
			if (cap.getWhitelist().size() == 0 && cap.getPlayers().size() == 0) {
				switch (pt) {
				case ULNM: {return matchType.DENIED;}
				case HRM: case LRM: {return matchType.FULL;}
				default:}
			}
			else if (cap.getWhitelist().size() == 0 && cap.getPlayers().size() > 0){
				switch (pt) {
				case ULNM: case LRM: {return matchType.DENIED;}
				case LM: case LNM: {return matchType.FULL;}
				case HRM: {
					for (Map.Entry<UUID, Integer> members : glist.get(gindex).members.entrySet()) {
						if (members.getKey().equals(cap.getPlayers().get(0))) { return matchType.FULL;}
					}
					return matchType.DENIED;
				}
				default:}
			}
			else if (cap.getWhitelist().size() > 0 && cap.getPlayers().size() == 0 && cap.getLeasePrice() >= 0){
				switch (pt) {
				case ULNM:{return matchType.DENIED;}
				case LRM: {return matchType.WHITELIST;}
				case HRM: {return matchType.FULL;}
				default:}
			}
			else if (cap.getWhitelist().size() > 0 && cap.getPlayers().size() == 0 && cap.getLeasePrice() == -1){
				switch (pt) {
				case ULNM: case LRM: {return matchType.WHITELIST;}
				case HRM: {return matchType.FULL;}
				default:}
			}
			else if (cap.getWhitelist().size() > 0 && cap.getPlayers().size() > 0){
				switch (pt) {
				case ULNM: case LRM: {return matchType.DENIED;}
				case LNM: case LM: {return matchType.WHITELIST;}
				case HRM: {
					for (Map.Entry<UUID, Integer> members : glist.get(gindex).members.entrySet()) {
						if (members.getKey().equals(cap.getPlayers().get(0))) { return matchType.FULL;}
					}
					return matchType.DENIED;
				}
				default:}
			}
		}		
		return matchType.DENIED;
	}
	
	public static boolean whitelistBreakCheck(String block, ChunkCapability cap) {
		if (cap.getWhitelist().size() == 0) return true;
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getBlock().equalsIgnoreCase(block) && wlItem.getCanBreak()) return true;
		}
		return false;
	}
	
	public static boolean whitelistInteractCheck(String block, ChunkCapability cap) {
		if (cap.getWhitelist().size() == 0) return true;
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getBlock().equalsIgnoreCase(block) && wlItem.getCanInteract()) return true;
		}
		return false;
	}
	
	public static boolean whitelistAttackCheck(Entity entity, ChunkCapability cap) {
		if (cap.getWhitelist().size() == 0) return true;
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getEntity().equalsIgnoreCase(entity.getClass().getSimpleName()) && wlItem.getCanBreak()) return true;
		}
		return false;
	}
	
	public static boolean whitelistInteractCheck(Entity entity, ChunkCapability cap) {
		if (cap.getWhitelist().size() == 0) return true;
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getEntity().equalsIgnoreCase(entity.getClass().getSimpleName()) && wlItem.getCanInteract()) return true;
		}
		return false;
	}

}

package com.dicemc.marketplace.core;

import java.util.List;
import java.util.UUID;

import com.dicemc.marketplace.util.capabilities.ChunkCapability;

import net.minecraft.entity.Entity;

public class ProtectionChecker {

	public static enum matchType {FULL, DENIED, WHITELIST}
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
		for (int i = 0; i < glist.size(); i++) { if (glist.get(i).guildID.equals(cap.getOwner())) {isGuildOwned = true; gindex = i;}}
		if (!isGuildOwned) for (UUID plyr : cap.getPlayers()) {if (plyr.equals(player)) return matchType.FULL;}
		if (isGuildOwned && !cap.getForSale()) {
			if (glist.get(gindex).members.getOrDefault(player, -1) == -1) {
				for (UUID plyrs : cap.getPlayers()) {if (plyrs.equals(player)) return matchType.WHITELIST;}
			}
			else if (glist.get(gindex).members.getOrDefault(player, 4) > cap.getPermMin()) return matchType.WHITELIST; 
			else if (glist.get(gindex).members.getOrDefault(player, 4) <= cap.getPermMin() && glist.get(gindex).members.getOrDefault(player, 4) != -1) return matchType.FULL; 
		}
		return matchType.DENIED;
	}
	
	public static boolean whitelistBreakCheck(String block, ChunkCapability cap) {
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getBlock().equalsIgnoreCase(block) && wlItem.getCanBreak()) return true;
		}
		return false;
	}
	
	public static boolean whitelistInteractCheck(String block, ChunkCapability cap) {
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getBlock().equalsIgnoreCase(block) && wlItem.getCanInteract()) return true;
		}
		return false;
	}
	
	public static boolean whitelistAttackCheck(Entity entity, ChunkCapability cap) {
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getEntity().equalsIgnoreCase(entity.getClass().getSimpleName()) && wlItem.getCanBreak()) return true;
		}
		return false;
	}
	
	public static boolean whitelistInteractCheck(Entity entity, ChunkCapability cap) {
		for (WhitelistItem wlItem : cap.getWhitelist()) {
			if (wlItem.getEntity().equalsIgnoreCase(entity.getClass().getSimpleName()) && wlItem.getCanInteract()) return true;
		}
		return false;
	}
}

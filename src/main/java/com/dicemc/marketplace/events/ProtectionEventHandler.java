package com.dicemc.marketplace.events;

import java.util.List;
import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.Main.ModConfig;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.ProtectionChecker;
import com.dicemc.marketplace.core.WhitelistItem;
import com.dicemc.marketplace.item.ItemWhitelistStick;
import com.dicemc.marketplace.item.ItemWhitelister;
import com.dicemc.marketplace.item.ModItems;
import com.dicemc.marketplace.network.MessageClientConfigRequest;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class ProtectionEventHandler {
	
	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event) {
		//TODO this isn't triggering when it should
		System.out.println("block break triggered");
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (event.getPlayer().isCreative() || event.getPlayer().dimension != 0) return;
		if (cap.getOwner().equals(Reference.NIL) && !Main.ModConfig.UNOWNED_PROTECTED) return;
		if (cap.getOwner().equals(Reference.NIL) && Main.ModConfig.UNOWNED_PROTECTED 
			&& ProtectionChecker.unownedWLBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) return;
		if (cap.getOwner().equals(Reference.NIL) && Main.ModConfig.AUTO_TEMP_CLAIM) {
			ChunkPos pos = event.getWorld().getChunkFromBlockCoords(event.getPos()).getPos();
			Main.NET.sendTo(new MessageClientConfigRequest(0, pos.x, pos.z), (EntityPlayerMP) event.getPlayer());
		}
		switch (ProtectionChecker.ownerMatch(event.getPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
		case DENIED: {
			event.setCanceled(true);
			event.getPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.breakdeny"), true);
			return;
		}
		case WHITELIST: {
			if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
				event.setCanceled(true);
				event.getPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.breakdeny"), true);
			}
			return;
		}
		default:
		}
	}
	
	@SubscribeEvent
	public static void onBlockLeftClick(LeftClickBlock event) {
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
		Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
		List<Guild> glist = GuildSaver.get(event.getWorld()).GUILDS;
		boolean status = isWhiteListAction(cap, heldItem, event.getEntityPlayer(), null, block.getRegistryName().toString(), true, glist, true);
		if (status) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onBlockRightClick(RightClickBlock event) {
		if (!event.getWorld().isRemote) {
			IBlockState state = event.getWorld().getBlockState(event.getPos());
			ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
			ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
			Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
			List<Guild> glist = GuildSaver.get(event.getWorld()).GUILDS;
			//Whitelister interact toggle
			boolean status = isWhiteListAction(cap, heldItem, event.getEntityPlayer(), null,  block.getRegistryName().toString(), true, glist, false);
			if (status) {
				event.setCanceled(true);
				Chunk chunk = event.getWorld().getChunkFromBlockCoords(event.getPos());
				event.getWorld().markAndNotifyBlock(event.getPos(), chunk, state, state, Constants.BlockFlags.NOTIFY_NEIGHBORS | Constants.BlockFlags.SEND_TO_CLIENTS);
				return;
			}
			//normal protection checks
			EntityPlayer player = event.getEntityPlayer();
			//Ignore if in creative, not in overworld, or land is unowned
			if (event.getEntityPlayer().isCreative() || event.getEntityPlayer().dimension != 0 || cap.getOwner().equals(Reference.NIL)) {return;}
			switch (ProtectionChecker.ownerMatch(player.getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
			case DENIED: {
				event.setCanceled(true);
				Chunk chunk = event.getWorld().getChunkFromBlockCoords(event.getPos());
				event.getWorld().markAndNotifyBlock(event.getPos(), chunk, state, state, Constants.BlockFlags.NOTIFY_NEIGHBORS | Constants.BlockFlags.SEND_TO_CLIENTS);
				event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.breakdeny"), true);
				return;
			}
			case WHITELIST: {
				if (!ProtectionChecker.whitelistInteractCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
					event.setCanceled(true);
					Chunk chunk = event.getWorld().getChunkFromBlockCoords(event.getPos());
					event.getWorld().markAndNotifyBlock(event.getPos(), chunk, state, state, Constants.BlockFlags.NOTIFY_NEIGHBORS | Constants.BlockFlags.SEND_TO_CLIENTS);
					event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.breakdeny"), true);
				}
				return;
			}
			default: {
				Chunk chunk = event.getWorld().getChunkFromBlockCoords(event.getPos());
				event.getWorld().markAndNotifyBlock(event.getPos(), chunk, state, state, Constants.BlockFlags.NOTIFY_NEIGHBORS | Constants.BlockFlags.SEND_TO_CLIENTS);
			}
			}
		}		
	}
	
	private static boolean isWhiteListAction(ChunkCapability cap, ItemStack heldItem, EntityPlayer player,@Nullable Entity wlEntity, String wlItem, boolean isBlock, List<Guild> glist, boolean isLeftClick) {
		//Check if the item is a whitelister first
		if (heldItem.getItem().equals(ModItems.WHITELISTER) || heldItem.getItem() instanceof ItemWhitelistStick) {
			//setup base checker variables
			int gid = -1;
			for (int i = 0; i < glist.size(); i++) {if (glist.get(i).members.getOrDefault(player.getUniqueID(), -1) >= 0) gid = i;}
			//next check what kind of item it is
			if (heldItem.getItem().equals(ModItems.WHITELISTER)) {
				if (isLeftClick) {
					if (player.isSneaking()) {
						if (isBlock) { //copies chunk WL to item WL
							ItemWhitelister.setWhitelister(heldItem, cap.getWhitelist());
						}
						//else {/*isEntity*/ }//left click sneaking an entity does nothing
					}
					else {//not sneaking
						if (isBlock) {
							ItemWhitelister.addToWhitelister(heldItem, wlItem, true, false);
						}
						else {//isEntity
							ItemWhitelister.addToWhitelister(heldItem, wlEntity, true, false);
						}
					}
				}
				else {//is right click
					if (player.isSneaking() && gid >= 0) {
						if (isBlock) {
							if (isSubletPermitted(cap, glist, gid, player)) {
								cap.fromNBTWhitelist(heldItem.getTagCompound().getTagList("whitelister", Constants.NBT.TAG_COMPOUND));
								player.sendStatusMessage(new TextComponentTranslation("event.chunk.wlapply"), true);
							}
							else {player.sendStatusMessage(new TextComponentTranslation("event.chunk.wlapply.deny"), true);}
						}
						//else {/*isEntity*/ }//right click sneaking an entity does nothing
					}
					else {//not sneaking
						if (isBlock) {
							ItemWhitelister.addToWhitelister(heldItem, wlItem, false, true);
						}
						else {//isEntity
							ItemWhitelister.addToWhitelister(heldItem, wlEntity, false, true);
						}
					}
				}
			}
			else if (heldItem.getItem().equals(ModItems.WL_STICK_GREEN) && gid >= 0) {
				if (!isSubletPermitted(cap, glist, gid, player)) {
					player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.deny"), true);
					return false;
				}
				WhitelistItem wli;
				if (isLeftClick) {
					if (isBlock) {
						wli = new WhitelistItem(wlItem);
						wli.setCanBreak(true);
						wli.setCanInteract(ProtectionChecker.whitelistInteractCheck(wlItem, cap));
						String canInteract = String.valueOf(wli.getCanInteract());						
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlItem, "True", canInteract), true);
					}
					else {//isEntity
						wli = new WhitelistItem(wlEntity);
						wli.setCanBreak(true);
						wli.setCanInteract(ProtectionChecker.whitelistInteractCheck(wlEntity, cap));
						String canInteract = String.valueOf(wli.getCanInteract());
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlEntity.getClass().getSimpleName(), "True", canInteract), true);
					}
				}
				else {//is right click
					if (isBlock) {
						wli = new WhitelistItem(wlItem);
						wli.setCanInteract(true);
						wli.setCanBreak(ProtectionChecker.whitelistBreakCheck(wlItem, cap));
						String canBreak = String.valueOf(wli.getCanBreak());
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlItem, canBreak, "True"), true);
					}
					else {//isEntity
						wli = new WhitelistItem(wlEntity);
						wli.setCanInteract(true);
						wli.setCanBreak(ProtectionChecker.whitelistAttackCheck(wlEntity, cap));
						String canBreak = String.valueOf(wli.getCanBreak());
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlEntity.getClass().getSimpleName(), canBreak, "True"), true);
					}
				}
				cap.changeWhitelist(wli);
			}
			else if (heldItem.getItem().equals(ModItems.WL_STICK_RED) && gid >= 0) {
				if (!isSubletPermitted(cap, glist, gid, player)) {
					player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.deny"), true);
					return false;
				}
				WhitelistItem wli;
				if (isLeftClick) {
					if (isBlock) {
						wli = new WhitelistItem(wlItem);
						wli.setCanBreak(false);
						wli.setCanInteract(ProtectionChecker.whitelistInteractCheck(wlItem, cap));
						String canInteract = String.valueOf(wli.getCanInteract());
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlItem, "False", canInteract), true);
					}
					else {//isEntity
						wli = new WhitelistItem(wlEntity);
						wli.setCanBreak(false);
						wli.setCanInteract(ProtectionChecker.whitelistInteractCheck(wlEntity, cap));
						String canInteract = String.valueOf(wli.getCanInteract());
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlEntity.getClass().getSimpleName(), "False", canInteract), true);
					}
				}
				else {//is right click
					if (isBlock) {
						wli = new WhitelistItem(wlItem);
						wli.setCanInteract(false);
						wli.setCanBreak(ProtectionChecker.whitelistBreakCheck(wlItem, cap));
						String canBreak = String.valueOf(wli.getCanBreak());
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlItem, canBreak, "False"), true);
					}
					else {//isEntity
						wli = new WhitelistItem(wlEntity);
						wli.setCanInteract(false);
						wli.setCanBreak(ProtectionChecker.whitelistAttackCheck(wlEntity, cap));
						String canBreak = String.valueOf(wli.getCanBreak());
						player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.stick.newsetting", wlEntity.getClass().getSimpleName(), canBreak, "False"), true);
					}
				}
				cap.changeWhitelist(wli);
			}
			return true;
		}			
		return false;
	}
	
	private static boolean isSubletPermitted(ChunkCapability cap, List<Guild> glist, int gid, EntityPlayer player) {
		if (player.canUseCommand(2, "") && player.isCreative()) return true;
		return (cap.getOwner().equals(glist.get(gid).guildID) && glist.get(gid).members.getOrDefault(player.getUniqueID(), 4) <= glist.get(gid).permissions.get("managesublet")
				&& glist.get(gid).members.getOrDefault(player.getUniqueID(), 4) != -1);
	}
	
	@SubscribeEvent
	public static void onBlockPlace(EntityPlaceEvent event) {
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (!ModConfig.UNOWNED_PROTECTED && cap.getOwner().equals(Reference.NIL)) return;
		if (event.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.getEntity();
			if (player.isCreative() || player.dimension != 0) return;
			if (cap.getOwner().equals(Reference.NIL) && Main.ModConfig.AUTO_TEMP_CLAIM) {
				ChunkPos pos = event.getWorld().getChunkFromBlockCoords(event.getPos()).getPos();
				Main.NET.sendTo(new MessageClientConfigRequest(0, pos.x, pos.z), (EntityPlayerMP) player);
			}
			switch (ProtectionChecker.ownerMatch(player.getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
			case DENIED: {
				//TODO send packet to sync the inventory
				event.setCanceled(true);
				player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.placedeny"), true);
				return;
			}
			case WHITELIST: {
				if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
					event.setCanceled(true);
					player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.placedeny"), true);
				}
				return;
			}
			default:
			}
		}
		//TODO logic for non-player placing like special blocks.
		else event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onEntityInteract(EntityInteract event) {
		if (!event.getEntity().world.isRemote) {
			ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
			ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
			List<Guild> glist = GuildSaver.get(event.getWorld()).GUILDS;
			//Whitelister interact toggle
			boolean status = isWhiteListAction(cap, heldItem, event.getEntityPlayer(), event.getTarget(),  "", false, glist, false);
			event.setCanceled(status);
			if (status) return;
			if (event.getEntityPlayer().isCreative() || cap.getOwner().equals(Reference.NIL)) return;
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.DENIED && !event.getEntity().world.isRemote) {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.interactdeny"), true);
			}
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST && !event.getEntity().world.isRemote) {
				if (!ProtectionChecker.whitelistInteractCheck(event.getTarget(), cap)) {
					event.setCanceled(true);
					event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.interactdeny"), true);
				}
			}
		}		
	}
	
	@SubscribeEvent
	public static void onEntityInteract(EntityInteractSpecific event) {
		if (!event.getEntity().world.isRemote) {
			ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
			ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
			List<Guild> glist = GuildSaver.get(event.getWorld()).GUILDS;
			//Whitelister interact toggle
			boolean status = isWhiteListAction(cap, heldItem, event.getEntityPlayer(), event.getTarget(),  "", false, glist, false);
			event.setCanceled(status);
			if (status) return;
			if (event.getEntityPlayer().isCreative() || cap.getOwner().equals(Reference.NIL)) return;
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.DENIED && !event.getEntity().world.isRemote) {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.interactdeny"), true);
			}
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST && !event.getEntity().world.isRemote) {
				if (!ProtectionChecker.whitelistInteractCheck(event.getTarget(), cap)) {
					event.setCanceled(true);
					event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.interactdeny"), true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityAttack(AttackEntityEvent event) {
		ChunkCapability cap = event.getEntity().world.getChunkFromBlockCoords(event.getEntity().getPosition()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (!event.getEntity().world.isRemote) {
			ItemStack heldItem = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
			List<Guild> glist = GuildSaver.get(event.getEntity().world).GUILDS;
			boolean status = isWhiteListAction(cap, heldItem, event.getEntityPlayer(), event.getTarget(),  "", false, glist, true);
			event.setCanceled(status);
			if (status) return;
		}
		if (event.getEntityPlayer().isCreative() || cap.getOwner().equals(Reference.NIL)) return;
		if (!(event.getTarget() instanceof EntityMob) && !(event.getTarget() instanceof EntityPlayer))	{
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getEntityPlayer().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.DENIED) {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.breakdeny"), true);
			}
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getEntityPlayer().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST) {
				if (!ProtectionChecker.whitelistAttackCheck(event.getTarget(), cap)) {
					event.setCanceled(true);
					event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.breakdeny"), true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityDamage(LivingDamageEvent event) {
		if (event.getSource().isCreativePlayer()) return;
		if (!(event.getEntityLiving() instanceof EntityMob) && !(event.getEntityLiving() instanceof EntityPlayer) && (event.getSource().getTrueSource() instanceof EntityPlayer))	{
			ChunkCapability cap = event.getEntityLiving().world.getChunkFromChunkCoords(event.getEntityLiving().chunkCoordX, event.getEntityLiving().chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null);
			if (((EntityPlayer)event.getSource().getTrueSource()).getHeldItemMainhand().getItem().equals(ModItems.WHITELISTER) && !((EntityPlayer)event.getSource().getTrueSource()).isSneaking() && !event.getEntity().world.isRemote) {
				ItemStack heldItem = ((EntityPlayer)event.getSource().getTrueSource()).getHeldItem(EnumHand.MAIN_HAND);
				List<Guild> glist = GuildSaver.get(event.getEntity().world).GUILDS;
				boolean status = isWhiteListAction(cap, heldItem, (EntityPlayer)event.getSource().getTrueSource(), event.getEntity(),  "", false, glist, true);
				event.setCanceled(status);
				if (status) return;
			}			
			if (cap.getOwner().equals(Reference.NIL)) return;
			if (ProtectionChecker.ownerMatch(event.getSource().getTrueSource().getUniqueID(), cap, GuildSaver.get(event.getSource().getTrueSource().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.DENIED) {
				event.setCanceled(true);
				if (event.getSource().getTrueSource() instanceof EntityPlayer) ((EntityPlayer)event.getSource().getTrueSource()).sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.breakdeny"), true);
			}
			if (ProtectionChecker.ownerMatch(event.getSource().getTrueSource().getUniqueID(), cap, GuildSaver.get(event.getSource().getTrueSource().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST) {
				if (!ProtectionChecker.whitelistAttackCheck(event.getSource().getTrueSource(), cap)) {
					event.setCanceled(true);
					if (event.getSource().getTrueSource() instanceof EntityPlayer) ((EntityPlayer)event.getSource().getTrueSource()).sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.entity.breakdeny"), true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onExplosion (ExplosionEvent.Detonate event) {
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(new BlockPos(event.getExplosion().getPosition())).getCapability(ChunkProvider.CHUNK_CAP, null);
		for (int i = event.getAffectedBlocks().size()-1; i > 0; i--) {
			cap = event.getWorld().getChunkFromBlockCoords(event.getAffectedBlocks().get(i)).getCapability(ChunkProvider.CHUNK_CAP, null);
			if (!cap.getOwner().equals(Reference.NIL) && !cap.getExplosionsOn()) event.getAffectedBlocks().remove(i);
		}
		for (int i = event.getAffectedEntities().size()-1; i > 0; i--) {
			cap = event.getWorld().getChunkFromBlockCoords(event.getAffectedEntities().get(i).getPosition()).getCapability(ChunkProvider.CHUNK_CAP, null);
			if (!cap.getOwner().equals(Reference.NIL) && !cap.getExplosionsOn()) event.getAffectedEntities().remove(i);
		}		
	}
	
	@SubscribeEvent
	public static void onTrample(FarmlandTrampleEvent event) {
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (!ModConfig.UNOWNED_PROTECTED && cap.getOwner().equals(Reference.NIL)) return;
		if (event.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.getEntity();
			if (player.isCreative() || player.dimension != 0) return;
			if (cap.getOwner().equals(Reference.NIL) && Main.ModConfig.AUTO_TEMP_CLAIM) {
				ChunkPos pos = event.getWorld().getChunkFromBlockCoords(event.getPos()).getPos();
				Main.NET.sendTo(new MessageClientConfigRequest(0, pos.x, pos.z), (EntityPlayerMP) event.getEntity());
			}
			switch (ProtectionChecker.ownerMatch(player.getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
			case DENIED: {
				event.setCanceled(true);
				player.sendStatusMessage(new TextComponentTranslation("event.chunk.trampledeny"), true);
				break;
			}
			case WHITELIST: {
				if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
					event.setCanceled(true);
					player.sendStatusMessage(new TextComponentTranslation("event.chunk.trampledeny"), true);
				}
				break;
			}
			default:
			}
		}
		else event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onBucketUse(FillBucketEvent event) {
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getTarget().getBlockPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (!ModConfig.UNOWNED_PROTECTED && cap.getOwner().equals(Reference.NIL)) return;
		if (event.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.getEntity();
			if (player.isCreative() || player.dimension != 0) return;
			if (cap.getOwner().equals(Reference.NIL) && Main.ModConfig.AUTO_TEMP_CLAIM) {
				ChunkPos pos = event.getWorld().getChunkFromBlockCoords(event.getTarget().getBlockPos()).getPos();
				Main.NET.sendTo(new MessageClientConfigRequest(0, pos.x, pos.z), (EntityPlayerMP) event.getEntity());
			}
			switch (ProtectionChecker.ownerMatch(player.getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
			case DENIED: {
				event.setCanceled(true);
				player.sendStatusMessage(new TextComponentTranslation("event.chunk.trampledeny"), true);
				break;
			}
			case WHITELIST: {
				if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getTarget().getBlockPos()).getBlock().getRegistryName().toString(), cap)) {
					event.setCanceled(true);
					player.sendStatusMessage(new TextComponentTranslation("event.chunk.trampledeny"), true);
				}
				break;
			}
			default:
			}
		}
		else event.setCanceled(true);
	}
}

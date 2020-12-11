package com.dicemc.marketplace.events;

import java.util.List;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.Main.ModConfig;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.ProtectionChecker;
import com.dicemc.marketplace.item.ItemWhitelister;
import com.dicemc.marketplace.item.ModItems;
import com.dicemc.marketplace.network.MessageBlockActivate;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
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
		if (event.getPlayer().isCreative()) return;
		if (event.getPlayer().dimension != 0) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL) && !Main.ModConfig.UNOWNED_PROTECTED) return;
		switch (ProtectionChecker.ownerMatch(event.getPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
		case DENIED: {
			event.setCanceled(true);
			event.getPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.breakdeny"), true);
			break;
		}
		case WHITELIST: {
			if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
				event.setCanceled(true);
				event.getPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.breakdeny"), true);
			}
			break;
		}
		default:
		}
	}
	
	@SubscribeEvent
	public static void onBlockLeftClick(LeftClickBlock event) {
		if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && !event.getEntityPlayer().isSneaking() && !event.getEntity().world.isRemote) {
			ItemWhitelister.addToWhitelister(event.getEntityPlayer().getHeldItemMainhand(), event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), true, false);
			event.setCanceled(true);
			return;
		}
		if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && event.getEntityPlayer().isSneaking() && !event.getEntity().world.isRemote) {
			ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
			ItemWhitelister.setWhitelister(event.getEntityPlayer().getHeldItemMainhand(), cap.getWhitelist());
			event.setCanceled(true);
			return;
		}
	}
	
	@SubscribeEvent
	public static void onBlockRightClick(RightClickBlock event) {
		if (!event.getWorld().isRemote) {
			//Whitelister interact toggle
			if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && !event.getEntityPlayer().isSneaking()) {
				ItemWhitelister.addToWhitelister(event.getEntityPlayer().getHeldItemMainhand(), event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), false, true);
				event.setCanceled(true);
				return;
			}		
			ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
			//Whitelister apply to chunk action
			if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && event.getEntityPlayer().isSneaking() && cap.getPlayers().size() == 0) {
				event.setCanceled(true);
				if (event.getEntityPlayer().canUseCommand(2, "") && event.getEntityPlayer().isCreative()) {
					cap.fromNBTWhitelist(event.getEntityPlayer().getHeldItemMainhand().getTagCompound().getTagList("whitelister", Constants.NBT.TAG_COMPOUND));
					event.getEntityPlayer().sendStatusMessage(new TextComponentString("Whitelist applied to chunk"), true);
					return;
				}
				List<Guild> glist = GuildSaver.get(event.getWorld()).GUILDS;
				int gid = -1;
				for (int i = 0; i < glist.size(); i++) {if (glist.get(i).members.getOrDefault(event.getEntityPlayer().getUniqueID(), -1) >= 0) gid = i;}
				if (gid >= 0) {
					if (cap.getOwner().equals(glist.get(gid).guildID) && glist.get(gid).members.getOrDefault(event.getEntityPlayer().getUniqueID(), 4) <= glist.get(gid).permissions.get("managesublet")
							&& glist.get(gid).members.getOrDefault(event.getEntityPlayer().getUniqueID(), 4) != -1) {
						cap.fromNBTWhitelist(event.getEntityPlayer().getHeldItemMainhand().getTagCompound().getTagList("whitelister", Constants.NBT.TAG_COMPOUND));
						event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.wlapply"), true);
						return;
					}
					else {
						event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.wlapply.deny"), true);
						return;
					}
				}
			}		
			EntityPlayer player = event.getEntityPlayer();
			//Ignore if in creative, not in overworld, or land is unowned
			if (event.getEntityPlayer().isCreative() || event.getEntityPlayer().dimension != 0 || cap.getOwner().equals(Reference.NIL)) {return;}			
			switch (ProtectionChecker.ownerMatch(player.getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
			case DENIED: {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.breakdeny"), true);
				return;
			}
			case WHITELIST: {
				if (!ProtectionChecker.whitelistInteractCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
					event.setCanceled(true);
					event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.breakdeny"), true);
				}
				return;
			}
			default:
			}
		}
		
	}
	
	@SubscribeEvent
	public static void onBlockPlace(EntityPlaceEvent event) {
		System.out.println("PlaceEvent");
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (!ModConfig.UNOWNED_PROTECTED && cap.getOwner().equals(Reference.NIL)) return;
		if (event.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.getEntity();
			if (player.isCreative()) return;
			if (player.dimension != 0) return;			
			switch (ProtectionChecker.ownerMatch(player.getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
			case DENIED: {
				event.setCanceled(true);
				player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.placedeny"), true);
				break;
			}
			case WHITELIST: {
				if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
					event.setCanceled(true);
					player.sendStatusMessage(new TextComponentTranslation("event.chunk.whitelist.block.placedeny"), true);
				}
				break;
			}
			default:
			}
		}
		//TODO logic for non-player placing like special blocks.
		else event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onEntityInteract(EntityInteract event) {
		if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && !event.getEntityPlayer().isSneaking() && !event.getEntity().world.isRemote) {
			ItemWhitelister.addToWhitelister(event.getEntityPlayer().getHeldItemMainhand(), event.getTarget(), false, true);
			event.setCanceled(true);
			return;
		}
		if (event.getEntityPlayer().isCreative()) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) return;
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
	
	@SubscribeEvent
	public static void onEntityInteract(EntityInteractSpecific event) {
		if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && !event.getEntityPlayer().isSneaking() && !event.getEntity().world.isRemote && event.getResult().equals(EnumActionResult.SUCCESS)) {
			ItemWhitelister.addToWhitelister(event.getEntityPlayer().getHeldItemMainhand(), event.getTarget(), false, true);
			event.setCanceled(true);
			return;
		}
		if (event.getEntityPlayer().isCreative()) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) return;
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
	@SubscribeEvent
	public static void onEntityAttack(AttackEntityEvent event) {
		if (event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem().equals(ModItems.WHITELISTER) && !event.getEntityPlayer().isSneaking() && !event.getEntity().world.isRemote) {
			ItemWhitelister.addToWhitelister(event.getEntityPlayer().getHeldItemMainhand(), event.getTarget(), true, false);
			event.setCanceled(true);
			return;
		}
		if (event.getEntityPlayer().isCreative()) return;
		if (!(event.getTarget() instanceof EntityMob) && !(event.getTarget() instanceof EntityPlayer))	{
			ChunkCapability cap = event.getEntity().world.getChunkFromBlockCoords(event.getEntity().getPosition()).getCapability(ChunkProvider.CHUNK_CAP, null);
			if (cap.getOwner().equals(Reference.NIL)) return;
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
			if (((EntityPlayer)event.getSource().getTrueSource()).getHeldItemMainhand().getItem().equals(ModItems.WHITELISTER) && !((EntityPlayer)event.getSource().getTrueSource()).isSneaking() && !event.getEntity().world.isRemote) {
				ItemWhitelister.addToWhitelister(((EntityPlayer)event.getSource().getTrueSource()).getHeldItemMainhand(), event.getEntityLiving(), true, false);
				event.setCanceled(true);
				return;
			}
			ChunkCapability cap = event.getEntityLiving().world.getChunkFromChunkCoords(event.getEntityLiving().chunkCoordX, event.getEntityLiving().chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null);
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
			if (player.isCreative()) return;
			if (player.dimension != 0) return;			
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
}

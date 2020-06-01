package com.dicemc.marketplace.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.gui.ContainerSell;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class ChunkEventHandler {
	public static final ResourceLocation CHUNK_LOC = new ResourceLocation(Reference.MOD_ID, "_ChunkData");
	//TODO Implement checks for the whitelist.
	@SubscribeEvent
	public static void onChunkEnterEvent(EnteringChunk event) {
		if (event.getEntity() instanceof EntityPlayer) {
			ChunkCapability ncap = event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ()).getCapability(ChunkProvider.CHUNK_CAP, null);
			List<Guild> list = GuildSaver.get(event.getEntity().world).GUILDS;
			String str = "Unowned";
			if (!ncap.getOwner().equals(Reference.NIL)) {
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).guildID.equals(ncap.getOwner())) {
						str = list.get(i).guildName; 
						if (ncap.getOutpost()) {
							//checks if the entered chunk is an outpost that borders core land and updates it to be core land.
							if (event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX() -1 , event.getNewChunkZ()).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(ncap.getOwner())) {
								if (!(event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX() -1 , event.getNewChunkZ()).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost())) {
									ncap.setOutpost(false);
									event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ()).markDirty();
								}								
							}
							if ((event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX() +1 , event.getNewChunkZ()).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(ncap.getOwner()))) {
								if (!(event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX() +1 , event.getNewChunkZ()).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost())) {
									ncap.setOutpost(false);
									event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ()).markDirty();
								}								
							}
							if ((event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ() -1).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(ncap.getOwner()))) {
								if (!(event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ() -1).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost())) {
									ncap.setOutpost(false);
									event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ()).markDirty();
								}
							}
							if ((event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ() +1).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner().equals(ncap.getOwner()))) {
								if (!(event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ() +1).getCapability(ChunkProvider.CHUNK_CAP, null).getOutpost())) {
									ncap.setOutpost(false);
									event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ()).markDirty();
								}
							}
							if (!ncap.getOutpost()) {
								Chunk ck = event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX(), event.getNewChunkZ());
								List<ChunkPos> cp = list.get(i).outpostLand;
								int opIdx = -1;
								for (int c = 0; c < cp.size(); c++) {
									if (cp.get(c).x == ck.x && cp.get(c).z == ck.z) opIdx = c;
								}
								if (opIdx != -1) {
									list.get(i).coreLand.add(cp.get(opIdx));
									cp.remove(opIdx);
								}
							}
						}
						break;
					}
				}				
				if (str == "Unowned" && ncap.getTempTime() >= System.currentTimeMillis()) str = Commands.playerNamefromUUID(event.getEntity().getServer(), ncap.getOwner())+"'s temporary ";
				if (str == "Unowned" && ncap.getTempTime() < System.currentTimeMillis()) {
					ncap.setOwner(Reference.NIL); 
					ncap.setPublic(false);
					ncap.setOutpost(false);
					ncap.setPlayers(new ArrayList<UUID>());
					ncap.setPublicRedstone(false);
					ncap.setWhitelist(new NBTTagList());
				}
			}
			ChunkCapability ocap = event.getEntity().world.getChunkFromChunkCoords(event.getOldChunkX(), event.getOldChunkZ()).getCapability(ChunkProvider.CHUNK_CAP, null);	
			if (!ocap.getOwner().equals(ncap.getOwner())) {
				if (!event.getEntity().world.isRemote) event.getEntity().sendMessage(new TextComponentString("Now Entering "+ str + " territory."));
				
			}
			if (ncap.getForSale() && (event.getNewChunkX() != event.getOldChunkX() || event.getNewChunkZ() != event.getOldChunkZ())) if (!event.getEntity().world.isRemote) event.getEntity().sendMessage(new TextComponentString(TextFormatting.GREEN+"Land for sale at $"+String.valueOf(ncap.getPrice())));
		}
	}
	@SubscribeEvent
	public static void attachChunkCap(AttachCapabilitiesEvent<Chunk> event) {
		event.addCapability(CHUNK_LOC, new ChunkProvider());
	}
	/*
	 * TODO: Lease permissions. 
	 * players can be considered "guild exclusive" by having a guild member add them to the chunk, or
	 * players can lease the property to have their name added to the chunk.
	 * 
	 * This will probably be best controlled by a custom gui.  See discord for details.
	 * 
	 * ==UPDATE==
	 * The only check needed in this section is to deny access to otherwise permitted members when a
	 * player is in the permittedplayers list.  
	 */
	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event) {
		if (event.getPlayer().isCreative()) return;
		if (event.getPlayer().dimension != 0) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL) && !Main.ModConfig.UNOWNED_PROTECTED) return;
		if (!ownerMatch(event.getPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
			event.setCanceled(true);
			event.getPlayer().sendStatusMessage(new TextComponentString("You are not permitted to break blocks here."), true);
		}
	}
	@SubscribeEvent
	public static void onBlockRightClick(RightClickBlock event) {
		if (event.getEntityPlayer().isCreative()) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) return;
		if (!ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) && !event.getEntity().world.isRemote) {
			event.setCanceled(true);
			event.getEntityPlayer().sendStatusMessage(new TextComponentString("Block Interaction Denied."), true);
		}
	}
	@SubscribeEvent
	public static void onEntityInteract(EntityInteract event) {
		if (event.getEntityPlayer().isCreative()) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) return;
		if (!ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) && !event.getEntity().world.isRemote) {
			event.setCanceled(true);
			event.getEntityPlayer().sendStatusMessage(new TextComponentString("Entity Interaction Denied."), true);
		}
	}
	@SubscribeEvent
	public static void onEntityInteract(EntityInteractSpecific event) {
		if (event.getEntityPlayer().isCreative()) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) return;
		if (!ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) && !event.getEntity().world.isRemote) {
			event.setCanceled(true);
			event.getEntityPlayer().sendStatusMessage(new TextComponentString("Entity Interaction Denied."), true);
		}
	}
	@SubscribeEvent
	public static void onEntityAttack(AttackEntityEvent event) {
		if (event.getEntityPlayer().isCreative()) return;
		if (!(event.getTarget() instanceof EntityMob))	{
			ChunkCapability cap = event.getEntity().world.getChunkFromBlockCoords(event.getEntity().getPosition()).getCapability(ChunkProvider.CHUNK_CAP, null);
			List<Guild> glist = GuildSaver.get(event.getEntity().world).GUILDS;
			boolean canAttack = false;
			if (cap.getOwner().equals(Reference.NIL)) canAttack = true;
			if (!canAttack) {for (UUID plyr : cap.getPlayers()) {if (plyr.equals(event.getEntityPlayer().getUniqueID())) canAttack = true;}}
			if (!canAttack) { for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).guildID.equals(cap.getOwner())) {
					for (UUID member : glist.get(i).members.keySet()) {
						if (member.equals(event.getEntityPlayer().getUniqueID())) {
							canAttack = true;
						}
					}
				}
			}}
			if (!canAttack) {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentString("Entity Invulnerable."), true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityDamage(LivingDamageEvent event) {
		if (event.getSource().isCreativePlayer()) return;
		if (!(event.getEntityLiving() instanceof EntityMob) && !(event.getEntityLiving() instanceof EntityPlayer) && (event.getSource().getTrueSource() instanceof EntityPlayer))	{
			ChunkCapability cap = event.getEntityLiving().world.getChunkFromChunkCoords(event.getEntityLiving().chunkCoordX, event.getEntityLiving().chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null);
			List<Guild> glist = GuildSaver.get(event.getEntity().world).GUILDS;
			boolean canAttack = false;
			if (cap.getOwner().equals(Reference.NIL)) canAttack = true;
			if (!canAttack && !cap.getForSale()) {for (UUID plyr : cap.getPlayers()) {if (plyr.equals(event.getSource().getTrueSource().getUniqueID())) canAttack = true;}}
			if (!canAttack && !cap.getForSale()) { for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).guildID.equals(cap.getOwner())) {
					for (UUID member : glist.get(i).members.keySet()) {
						if (member.equals(event.getSource().getTrueSource().getUniqueID())) {
							canAttack = true;
						}
					}
				}
			}}
			if (!canAttack) {
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onContainerInteract(PlayerContainerEvent.Open event) {
		if (event.getEntityPlayer().isCreative()) return;
		if (event.getContainer() instanceof ContainerPlayer) return;
		if (event.getContainer() instanceof ContainerSell) return;
		ChunkCapability cap = event.getEntity().world.getChunkFromBlockCoords(event.getEntity().getPosition()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) return;
		if (!ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getEntity().world).GUILDS) && !event.getEntity().world.isRemote) {
			event.setCanceled(true);
			event.getEntityPlayer().sendStatusMessage(new TextComponentString("Container Interaction Denied."), true);
		}
	}
	
	@SubscribeEvent
	public static void onExplosion (ExplosionEvent.Start event) {
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(new BlockPos(event.getExplosion().getPosition())).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (!cap.getOwner().equals(Reference.NIL)) event.setCanceled(true);
	}
	
	private static boolean ownerMatch(UUID player, ChunkCapability cap, List<Guild> glist) {
		if (cap.getPublic()) return true;
		for (UUID plyr : cap.getPlayers()) {if (plyr.equals(player)) return true;}
		if (!cap.getForSale()) {
			for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).guildID.equals(cap.getOwner())) {
					for (UUID member : glist.get(i).members.keySet()) {
						if (member.equals(player)) return true;
					}
				}
			}
		}
		return false;
	}
}

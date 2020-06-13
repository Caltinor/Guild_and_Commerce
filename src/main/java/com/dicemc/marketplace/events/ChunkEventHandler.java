package com.dicemc.marketplace.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.ProtectionChecker;
import com.dicemc.marketplace.core.ProtectionChecker.matchType;
import com.dicemc.marketplace.core.WhitelistItem;
import com.dicemc.marketplace.gui.ContainerSell;
import com.dicemc.marketplace.item.ItemWhitelister;
import com.dicemc.marketplace.item.ModItems;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber
public class ChunkEventHandler {
	public static final ResourceLocation CHUNK_LOC = new ResourceLocation(Reference.MOD_ID, "_ChunkData");
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
					ncap.fromNBTWhitelist(new NBTTagList());
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
	
	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event) {
		if (event.getPlayer().isCreative()) return;
		if (event.getPlayer().dimension != 0) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL) && !Main.ModConfig.UNOWNED_PROTECTED) return;
		switch (ProtectionChecker.ownerMatch(event.getPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS)) {
		case DENIED: {
			event.setCanceled(true);
			event.getPlayer().sendStatusMessage(new TextComponentString("You are not permitted to break blocks here."), true);
			break;
		}
		case WHITELIST: {
			if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
				event.setCanceled(true);
				event.getPlayer().sendStatusMessage(new TextComponentString("You are not permitted to break blocks here."), true);
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
		if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && !event.getEntityPlayer().isSneaking() && !event.getEntity().world.isRemote) {
			ItemWhitelister.addToWhitelister(event.getEntityPlayer().getHeldItemMainhand(), event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), false, true);
			event.setCanceled(true);
			return;
		}		
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem().equals(ModItems.WHITELISTER) && event.getEntityPlayer().isSneaking() && !event.getEntity().world.isRemote && cap.getPlayers().size() == 0) {
			List<Guild> glist = GuildSaver.get(event.getWorld()).GUILDS;
			int gid = -1;
			for (int i = 0; i < glist.size(); i++) {if (glist.get(i).members.getOrDefault(event.getEntityPlayer().getUniqueID(), -1) >= 0) gid = i;}
			if (gid >= 0) {
				if (cap.getOwner().equals(glist.get(gid).guildID) && glist.get(gid).members.getOrDefault(event.getEntityPlayer().getUniqueID(), 4) <= glist.get(gid).permissions.get("managesublet")
						&& glist.get(gid).members.getOrDefault(event.getEntityPlayer().getUniqueID(), 4) != -1) {
					cap.fromNBTWhitelist(event.getEntityPlayer().getHeldItemMainhand().getTagCompound().getTagList("whitelister", Constants.NBT.TAG_COMPOUND));
					event.setCanceled(true);
					event.getEntityPlayer().sendStatusMessage(new TextComponentString("Whitelist applied to chunk"), true);
					return;
				}
				else event.getEntityPlayer().sendStatusMessage(new TextComponentString("you cannot apply the whitelist here."), true);
			}
		}			
		if (event.getEntityPlayer().isCreative()) return;
		if (cap.getOwner().equals(Reference.NIL)) return;
		if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.DENIED && !event.getEntity().world.isRemote) {
			event.setCanceled(true);
			event.getEntityPlayer().sendStatusMessage(new TextComponentString("Block Interaction Denied."), true);
		}
		if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST && !event.getEntity().world.isRemote) {
			if (!ProtectionChecker.whitelistInteractCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
				event.setUseBlock(Result.DENY);
				event.getEntityPlayer().sendStatusMessage(new TextComponentString("Block Interaction not whitelisted."), true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onBlockPlace(PlaceEvent event) {
		if (event.getPlayer().isCreative()) return;
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(event.getPos()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (ProtectionChecker.ownerMatch(event.getPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.DENIED && !event.getEntity().world.isRemote) {
			event.setCanceled(true);
			event.getPlayer().sendStatusMessage(new TextComponentString("Block Place Denied."), true);
		}
		if (ProtectionChecker.ownerMatch(event.getPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST && !event.getEntity().world.isRemote) {
			if (!ProtectionChecker.whitelistBreakCheck(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName().toString(), cap)) {
				event.setCanceled(true);
				event.getPlayer().sendStatusMessage(new TextComponentString("Block Place Denied."), true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onEntityInteract(EntityInteractSpecific event) {
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
			event.getEntityPlayer().sendStatusMessage(new TextComponentString("Entity Interaction Denied."), true);
		}
		if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST && !event.getEntity().world.isRemote) {
			if (!ProtectionChecker.whitelistInteractCheck(event.getTarget(), cap)) {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentString("Block Interaction Denied."), true);
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
		if (!(event.getTarget() instanceof EntityMob))	{
			ChunkCapability cap = event.getEntity().world.getChunkFromBlockCoords(event.getEntity().getPosition()).getCapability(ChunkProvider.CHUNK_CAP, null);
			List<Guild> glist = GuildSaver.get(event.getEntity().world).GUILDS;
			if (cap.getOwner().equals(Reference.NIL)) return;
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getEntityPlayer().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.DENIED) {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentString("Entity Invulnerable."), true);
			}
			if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getEntityPlayer().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST) {
				if (!ProtectionChecker.whitelistAttackCheck(event.getTarget(), cap)) {
					event.setCanceled(true);
					event.getEntityPlayer().sendStatusMessage(new TextComponentString("Block Interaction Denied."), true);
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
			List<Guild> glist = GuildSaver.get(event.getEntity().world).GUILDS;
			if (cap.getOwner().equals(Reference.NIL)) return;
			if (ProtectionChecker.ownerMatch(event.getSource().getTrueSource().getUniqueID(), cap, GuildSaver.get(event.getSource().getTrueSource().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.DENIED) {
				event.setCanceled(true);
				if (event.getSource().getTrueSource() instanceof EntityPlayer) ((EntityPlayer)event.getSource().getTrueSource()).sendStatusMessage(new TextComponentString("Entity Invulnerable."), true);
			}
			if (ProtectionChecker.ownerMatch(event.getSource().getTrueSource().getUniqueID(), cap, GuildSaver.get(event.getSource().getTrueSource().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST) {
				if (!ProtectionChecker.whitelistAttackCheck(event.getSource().getTrueSource(), cap)) {
					event.setCanceled(true);
					if (event.getSource().getTrueSource() instanceof EntityPlayer) ((EntityPlayer)event.getSource().getTrueSource()).sendStatusMessage(new TextComponentString("Block Interaction Denied."), true);
				}
			}
		}
	}
	
	/*@SubscribeEvent
	public static void onContainerInteract(PlayerContainerEvent.Open event) {
		if (event.getEntityPlayer().isCreative()) return;
		if (event.getContainer() instanceof ContainerPlayer) return;
		if (event.getContainer() instanceof ContainerSell) return;
		ChunkCapability cap = event.getEntity().world.getChunkFromBlockCoords(event.getEntity().getPosition()).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (cap.getOwner().equals(Reference.NIL)) return;
		if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getEntity().world).GUILDS) == ProtectionChecker.matchType.DENIED && !event.getEntity().world.isRemote) {
			event.setCanceled(true);
			event.getEntityPlayer().sendStatusMessage(new TextComponentString("Container Interaction Denied."), true);
		}
		if (ProtectionChecker.ownerMatch(event.getEntityPlayer().getUniqueID(), cap, GuildSaver.get(event.getEntityPlayer().getEntityWorld()).GUILDS) == ProtectionChecker.matchType.WHITELIST && !event.getEntity().world.isRemote) {
			if (!ProtectionChecker.whitelistInteractCheck(event.getEntityPlayer().getEntityWorld().getBlockState(event.getEntity().getPosition()).getBlock().getRegistryName().toString(), cap)) {
				event.setCanceled(true);
				event.getEntityPlayer().sendStatusMessage(new TextComponentString("Block Interaction Denied."), true);
			}
		}
	}*/
	
	@SubscribeEvent
	public static void onExplosion (ExplosionEvent.Start event) {
		ChunkCapability cap = event.getWorld().getChunkFromBlockCoords(new BlockPos(event.getExplosion().getPosition())).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (!cap.getOwner().equals(Reference.NIL)) event.setCanceled(true);
	}

}

package com.dicemc.marketplace.events;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
				if (!event.getEntity().world.isRemote) event.getEntity().sendMessage(new TextComponentTranslation("event.chunk.enter", str));
				
			}
			if (ncap.getForSale() && (event.getNewChunkX() != event.getOldChunkX() || event.getNewChunkZ() != event.getOldChunkZ())) if (!event.getEntity().world.isRemote) {
				Style style = new Style();
				event.getEntity().sendMessage(new TextComponentTranslation("event.chunk.forsale", String.valueOf(ncap.getPrice())).setStyle(style.setColor(TextFormatting.GREEN)));
			}
		}
	}
	@SubscribeEvent
	public static void attachChunkCap(AttachCapabilitiesEvent<Chunk> event) {
		event.addCapability(CHUNK_LOC, new ChunkProvider());
	}


}

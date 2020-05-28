package com.dicemc.marketplace.events;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.text.JTextComponent.KeyBinding;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@EventBusSubscriber
public class PlayerEventHandler {
	public static int tickCounter = 0;
	public static int taxCounter = 0;
	
	@SubscribeEvent
	public static void onPlayerLogin (PlayerLoggedInEvent event) {
		System.out.println(AccountSaver.get(event.player.world).PLAYERS.addAccount(event.player.getUniqueID(), Main.ModConfig.STARTING_FUNDS));
		System.out.println("Player ID: "+ event.player.getUniqueID().toString());
	}
	
	@SubscribeEvent
    public static void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Reference.MOD_ID))
        {
            ConfigManager.sync(Reference.MOD_ID, Type.INSTANCE);
        }
    }
	
	@SubscribeEvent
	public static void onServerTick (ServerTickEvent event) {
		if (event.phase == Phase.END) {
			taxCounter++;
			tickCounter++;
			World world = CoreUtils.world;
			if (tickCounter >= 1200) {
				Map<UUID, MarketItem> vendlist = MarketSaver.get(world).getAuction().vendList;
				for (Map.Entry<UUID, MarketItem> entry : vendlist.entrySet()) {
					if (entry.getValue().bidEnd < System.currentTimeMillis()) {
						MarketSaver.get(world).getAuction().addToQueue(entry.getValue().highestBidder, entry.getValue().item);
						AccountSaver.get(world).PLAYERS.addBalance(entry.getValue().vendor, entry.getValue().price);
						vendlist.remove(entry.getKey());
						AccountSaver.get(world).markDirty();
						MarketSaver.get(world).markDirty();
						System.out.println("Auction Expired and was processed.");
					}
				}
				tickCounter = 0;
			}
			if (taxCounter >= Main.ModConfig.GLOBAL_TAX_INTERVAL) {
				List<Guild> glist = GuildSaver.get(world).GUILDS;
				for (int i = 0; i < glist.size(); i++) {
					Guild g = glist.get(i);
					//Player Tax Portion
					if (g.guildTax > 0) {
						for (UUID m : g.members.keySet()) {
							double bal = AccountSaver.get(world).PLAYERS.getBalance(m);
							if ((bal*g.guildTax) <= bal) {
								AccountSaver.get(world).PLAYERS.addBalance(m, (-1*(bal*g.guildTax)));
								AccountSaver.get(world).GUILDS.addBalance(g.guildID, (bal*g.guildTax));
							}
						}
						AccountSaver.get(world).markDirty();
					}					
					//Guild Tax portion
					if (g.members.size() > 0) {
						double bal = AccountSaver.get(world).GUILDS.getBalance(g.guildID);
						if (bal < (g.guildWorth(world)/2)*-1) {
							for (EntityPlayer player : world.playerEntities) {player.sendMessage(new TextComponentString("Guild <"+g.guildName+"> has been deleted due to bankruptcy"));}
							glist.remove(GuildSaver.get(world).guildIndexFromName(g.guildName));
						}
						AccountSaver.get(world).GUILDS.addBalance(g.guildID, (g.taxableWorth(world)*-1*Main.ModConfig.GLOBAL_TAX_RATE));
						AccountSaver.get(world).markDirty();
					}
					System.out.println(g.guildName + " taxes have been processed.");
				}
				System.out.println("Taxes have been applied");
				for (EntityPlayer player : world.playerEntities) {player.sendMessage(new TextComponentString("Taxes have been applied."));}
				taxCounter = 0;
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLogout (PlayerLoggedOutEvent event) {
		
	}
}

package com.dicemc.marketplace.events;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@EventBusSubscriber
public class PlayerEventHandler {
	public static int tickCounter = 0;
	public static int taxCounter = 0;
	
	@SubscribeEvent
	public static void onPlayerLogin (PlayerLoggedInEvent event) {
		System.out.println(AccountSaver.get(event.player.world).getPlayers().addAccount(event.player.getUniqueID(), Main.ModConfig.STARTING_FUNDS));
	}
	
	@SubscribeEvent
    public static void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Reference.MOD_ID))
        {
            ConfigManager.sync(Reference.MOD_ID, Type.INSTANCE);
        }
    }
	
	@SuppressWarnings("static-access")
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
						AccountSaver.get(world).getPlayers().addBalance(entry.getValue().vendor, entry.getValue().price);
						vendlist.remove(entry.getKey());
						AccountSaver.get(world).markDirty();
						MarketSaver.get(world).markDirty();
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
							double bal = AccountSaver.get(world).getPlayers().getBalance(m);
							if ((bal*g.guildTax) <= bal) {
								AccountSaver.get(world).getPlayers().addBalance(m, (-1*(bal*g.guildTax)));
								AccountSaver.get(world).getGuilds().addBalance(g.guildID, (bal*g.guildTax));
							}
						}
						AccountSaver.get(world).markDirty();
					}					
					//Guild Tax portion
					if (!g.isAdmin) {
						double bal = AccountSaver.get(world).getGuilds().getBalance(g.guildID);
						double owe = AccountSaver.get(world).debt.getOrDefault(g.guildID, 0D);
						if (bal > owe && owe > 0D) {
							AccountSaver.get(world).getGuilds().addBalance(g.guildID, -1 * owe);
							AccountSaver.get(world).debt.remove(g.guildID);
							bal -= owe;
							for (EntityPlayer player : world.playerEntities) {player.sendMessage(new TextComponentTranslation("event.tax.debt.paid", g.guildName));}
						}
						else if (bal < owe && owe > 0D && bal > 0) {
							AccountSaver.get(world).getGuilds().addBalance(g.guildID, -1* owe);
							AccountSaver.get(world).debt.put(g.guildID, owe - bal);
							owe -= bal;
							bal = 0D;
							for (EntityPlayer player : world.playerEntities) {player.sendMessage(new TextComponentTranslation("event.tax.debt.less", g.guildName, String.valueOf(owe)));}
						}
						if (bal < (g.taxableWorth(world)*Main.ModConfig.GLOBAL_TAX_RATE)) {
							AccountSaver.get(world).debt.put(g.guildID, owe+(g.taxableWorth(world)*Main.ModConfig.GLOBAL_TAX_RATE));
							for (EntityPlayer player : world.playerEntities) {player.sendMessage(new TextComponentTranslation("event.tax.debt.more", g.guildName, String.valueOf(owe)));}
						}
						else AccountSaver.get(world).getGuilds().addBalance(g.guildID, (g.taxableWorth(world)*-1*Main.ModConfig.GLOBAL_TAX_RATE));
						if (owe > (g.guildWorth(world)/2)) {
							for (EntityPlayer player : world.playerEntities) {player.sendMessage(new TextComponentTranslation("event.tax.debt.bankrupt", g.guildName));}
							glist.remove(GuildSaver.get(world).guildIndexFromName(g.guildName));
						}
						AccountSaver.get(world).markDirty();
					}
				}
				for (EntityPlayer player : world.playerEntities) {player.sendMessage(new TextComponentTranslation("event.taxes.notice"));}
				taxCounter = 0;
			}
		}
	}
}

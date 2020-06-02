package com.dicemc.marketplace;

import java.util.List;

import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.network.PacketHandler;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkImplementation;
import com.dicemc.marketplace.util.capabilities.ChunkStorage;
import com.dicemc.marketplace.util.commands.AccountCommands;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.commands.GuildCommands;
import com.dicemc.marketplace.util.commands.TempClaimCommands;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;
import com.dicemc.marketplace.util.proxy.CommonProxy;
import com.dicemc.marketplace.util.proxy.GuiProxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid=Reference.MOD_ID, name=Reference.NAME, version=Reference.VERSION)
public class Main {
	public static SimpleNetworkWrapper NET;
	
	@Instance
	public static Main instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	
	@EventHandler
	public static void PreInit(FMLPreInitializationEvent event) {
		proxy.registerNetworkPackets();
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event) {	
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiProxy());
		CapabilityManager.INSTANCE.register(ChunkCapability.class, new ChunkStorage(), ChunkImplementation::new);
		ConfigManager.sync(Reference.MOD_ID, Type.INSTANCE);
	}
	
	@EventHandler
	public static void PostInit(FMLPostInitializationEvent event) {
		
	}
	
	@EventHandler
	public static void init(FMLServerStartingEvent event) {
		event.registerServerCommand(new Commands());
		event.registerServerCommand(new GuildCommands());
		event.registerServerCommand(new AccountCommands());
		event.registerServerCommand(new TempClaimCommands());
		MarketSaver.get(event.getServer().getEntityWorld());
		AccountSaver.get(event.getServer().getEntityWorld());
		List<Guild> glist = GuildSaver.get(event.getServer().getEntityWorld()).GUILDS;
		for (int i = 0; i < glist.size(); i++) AccountSaver.get(event.getServer().getEntityWorld()).GUILDS.addAccount(glist.get(i).guildID, Main.ModConfig.GUILD_STARTING_FUNDS);
		AccountSaver.get(event.getServer().getEntityWorld()).markDirty();
		CoreUtils.setWorld(event.getServer().getEntityWorld());
	}
	
	@Config(modid = Reference.MOD_ID, name = Reference.MOD_ID + "_Config", type = Type.INSTANCE, category = "general")
	public static class ModConfig {
		@Name("CHUNKS_PER_MEMBER")
		@RangeInt(min = 0)
		@Comment({"The number of untaxed chunks per member of a guild.", "setting this to zero would make all land taxed."})
		@RequiresMcRestart
		public static int CHUNKS_PER_MEMBER = 9;
		@Name("GLOBAL_TAX_RATE")
		@RangeDouble(min = 0.0, max = 1.0)
		@RequiresMcRestart
		public static double GLOBAL_TAX_RATE = 0.1;
		@Name("GLOBAL_TAX_INTERVAL")
		@RangeInt(min = 20)
		@RequiresMcRestart
		public static int GLOBAL_TAX_INTERVAL = 864000;
		@Name("GUILD_CREATE_COST")
		@RangeDouble(min = 0.0)
		@RequiresMcRestart
		public static double GUILD_CREATE_COST = 2500;
		@Name("TEMPCLAIM_DURATION")
		@RangeInt(min = 20)
		@RequiresMcRestart
		public static int TEMPCLAIM_DURATION = 43200000;
		@Name("STARTING_FUNDS")
		@RangeDouble(min = 0.0)
		@Comment({"The funds a player starts with.", "This should not be zero as the player would have no default way of tempclaiming"})
		@RequiresMcRestart
		public static double STARTING_FUNDS = 1000;
		@Name("GUILD_STARTING_FUNDS")
		@RequiresMcRestart
		@RangeDouble(min = 0.0)
		@Comment({"Funds given to a guild after creation.", "This is free money to the guild and is not taken from the creator.", "This should be thought of as a rebate on guild creation."})
		public static double GUILD_STARTING_FUNDS = 0;
		@Name("MARKET_GLOBAL_TAX_BUY")
		@RangeDouble(min = 0.0)
		public static double MARKET_GLOBAL_TAX_BUY = 0.1;
		@Name("MARKET_GLOBAL_TAX_SELL")
		@RangeDouble(min = 0.0)
		public static double MARKET_GLOBAL_TAX_SELL = 0.1;
		@Name("MARKET_AUCTION_TAX_SELL")
		@RangeDouble(min = 0.0)
		public static double MARKET_AUCTION_TAX_SELL = 0.3;
		@Name("UNOWNED_PROTECTED")
		@RequiresWorldRestart
		@Comment({"Is Unowned land protected.", "Default: TRUE", "setting false will allow modification of land without temp claims"})
		public static boolean UNOWNED_PROTECTED = true;
		@Name("AUCTION_OPEN_DURATION")
		@RequiresWorldRestart
		@Comment({"Determines how long an auction should stay up", "default: 259200000", "one day = 86400000"})
		public static int AUCTION_OPEN_DURATION = 259200000;
	}
}

package com.dicemc.marketplace.network;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.network.MessageGuildToGui.PacketGuildToGui;
import com.dicemc.marketplace.network.MessageMarketsToGui.PacketMarketsToGui;
import com.dicemc.marketplace.network.MessageMarketsToServer.PacketMarketsToServer;
import com.dicemc.marketplace.network.MessageMemberInfoToGui.PacketMemberInfoToGui;
import com.dicemc.marketplace.network.MessageMemberInfoToServer.PacketMemberInfoToServer;
import com.dicemc.marketplace.network.MessagePermsToGui.PacketPermsToGui;
import com.dicemc.marketplace.network.MessagePermsToServer.PacketPermsToServer;
import com.dicemc.marketplace.network.MessageAccountInfoToServer.PacketAccountInfoToServer;
import com.dicemc.marketplace.network.MessageAccountToGui.PacketAccountToGui;
import com.dicemc.marketplace.network.MessageAdminGuiOpen.PacketAdminGuiOpen;
import com.dicemc.marketplace.network.MessageAdminToGui.PacketAdminToGui;
import com.dicemc.marketplace.network.MessageAdminToServer.PacketAdminToServer;
import com.dicemc.marketplace.network.MessageAutoTempClaim.PacketAutoTempClaim;
import com.dicemc.marketplace.network.MessageChunkToGui.PacketChunkToGui;
import com.dicemc.marketplace.network.MessageChunkToServer.PacketChunkToServer;
import com.dicemc.marketplace.network.MessageClientConfigRequest.PacketClientConfigRequest;
import com.dicemc.marketplace.network.MessageCreateInfoToGui.PacketCreateInfoToGui;
import com.dicemc.marketplace.network.MessageCreateInfoToServer.PacketCreateInfoToServer;
import com.dicemc.marketplace.network.MessageGuiRequest.PacketGuiRequest;
import com.dicemc.marketplace.network.MessageGuildInfoToServer.PacketGuildInfoToServer;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	private static int nextPacketId = 0;
	private static int nextID() {return nextPacketId++;}
	
	public static void initHandler() {
		Main.NET.registerMessage(PacketGuiRequest.class, MessageGuiRequest.class, nextID(), Side.SERVER);		
		Main.NET.registerMessage(PacketGuildInfoToServer.class, MessageGuildInfoToServer.class, nextID(), Side.SERVER);
		Main.NET.registerMessage(PacketAccountInfoToServer.class, MessageAccountInfoToServer.class, nextID(), Side.SERVER);		
		Main.NET.registerMessage(PacketMemberInfoToServer.class, MessageMemberInfoToServer.class, nextID(), Side.SERVER);		
		Main.NET.registerMessage(PacketChunkToServer.class, MessageChunkToServer.class, nextID(), Side.SERVER);		
		Main.NET.registerMessage(PacketCreateInfoToServer.class, MessageCreateInfoToServer.class, nextID(), Side.SERVER);
		Main.NET.registerMessage(PacketPermsToServer.class, MessagePermsToServer.class, nextID(), Side.SERVER);		
		Main.NET.registerMessage(PacketMarketsToServer.class, MessageMarketsToServer.class, nextID(), Side.SERVER);
		Main.NET.registerMessage(PacketAdminToServer.class, MessageAdminToServer.class, nextID(), Side.SERVER);
		Main.NET.registerMessage(PacketAutoTempClaim.class, MessageAutoTempClaim.class, nextID(), Side.SERVER);
		//Client Packets		
		Main.NET.registerMessage(PacketAccountToGui.class, MessageAccountToGui.class, nextID(), Side.CLIENT);
		Main.NET.registerMessage(PacketMemberInfoToGui.class, MessageMemberInfoToGui.class, nextID(), Side.CLIENT);		
		Main.NET.registerMessage(PacketChunkToGui.class, MessageChunkToGui.class, nextID(), Side.CLIENT);		
		Main.NET.registerMessage(PacketCreateInfoToGui.class, MessageCreateInfoToGui.class, nextID(), Side.CLIENT);
		Main.NET.registerMessage(PacketPermsToGui.class, MessagePermsToGui.class, nextID(), Side.CLIENT);		
		Main.NET.registerMessage(PacketMarketsToGui.class, MessageMarketsToGui.class, nextID(), Side.CLIENT);		
		Main.NET.registerMessage(PacketGuildToGui.class, MessageGuildToGui.class, nextID(), Side.CLIENT);
		Main.NET.registerMessage(PacketAdminToGui.class, MessageAdminToGui.class, nextID(), Side.CLIENT);
		Main.NET.registerMessage(PacketAdminGuiOpen.class, MessageAdminGuiOpen.class, nextID(), Side.CLIENT);
		Main.NET.registerMessage(PacketClientConfigRequest.class, MessageClientConfigRequest.class, nextID(), Side.CLIENT);
	}
}

package com.dicemc.marketplace.gui;

import java.awt.Color;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.WhitelistItem;
import com.dicemc.marketplace.events.GuiEventHandler;
import com.dicemc.marketplace.events.PlayerEventHandler;
import com.dicemc.marketplace.network.MessageAccountInfoToServer;
import com.dicemc.marketplace.network.MessageChunkToServer;
import com.dicemc.marketplace.network.MessageGuildInfoToServer;
import com.dicemc.marketplace.util.CkPktType;
import com.dicemc.marketplace.util.Reference;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiUtils;


public class GuiChunkManager extends GuiScreen{
	public static Guild myGuild;
	public static List<ChunkSummary> chunkList;
	public static List<Integer> mapColors;
	private static GuiButton button1, button2, button3, overlayToggle, subletMenu, addMember, removeMember, backButton, wl0, wl1, wl2, wl3;
	private static GuiButton button4;
	private static GuiTextField sellprice, memberAdd;
	public static double acctPlayer, acctGuild;
	private static DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private static GuiListChunkMembers chunkMbrList;
	private static GuiListWhitelist wlList;
	private static int selectedIdx = 60;
	private static boolean overlayOwners = false;
	public static boolean canGuildClaim;
	public static boolean canGuildSell;
	public static boolean canGuildSublet;
	public static boolean isSubletMenu = false;
	public static Map<UUID, Integer> overlayColors = new HashMap<UUID, Integer>();
	private Map<Integer, Color> colorReference = colorSetup();
	private static String response = "";
	private static String sellpricelabel = "";
	int hoveredIdx = 0;
	int mapX, mapY, mapD;
	static String topString;
	private static EntityPlayer player = Minecraft.getMinecraft().player;

	private Map<Integer, Color> colorSetup() {
		Map<Integer, Color> clr = new HashMap<Integer, Color>();
		clr.put(0, Color.GREEN);
		clr.put(1, Color.RED);
		clr.put(2, Color.YELLOW);
		clr.put(3, Color.ORANGE);
		clr.put(4, Color.PINK);
		clr.put(5, Color.MAGENTA);
		clr.put(6, Color.WHITE);
		clr.put(7, Color.CYAN);
		clr.put(8, Color.BLUE);
		clr.put(9, Color.LIGHT_GRAY);
		clr.put(10, Color.DARK_GRAY);
		clr.put(11, Color.BLACK);
		return clr;
	}
	
	private static void setOverlayColors() {
		int index = 0;	
		overlayColors.put(Reference.NIL, -1);
		for (ChunkSummary entry : chunkList) {
			if (overlayColors.putIfAbsent(entry.ownerID, index) == null) index++;
		}
	}
	
	public static void guiUpdate(Guild myGuild, List<ChunkSummary> list, List<Integer> mapColors, String response, double acctP, double acctG) {
		GuiChunkManager.myGuild = myGuild;
		chunkList = list;
		GuiChunkManager.mapColors = mapColors;
		GuiChunkManager.canGuildClaim = myGuild.members.getOrDefault(Minecraft.getMinecraft().player.getUniqueID(), 4) <= myGuild.permissions.getOrDefault("setclaim", 3);
		GuiChunkManager.canGuildSell = myGuild.members.getOrDefault(Minecraft.getMinecraft().player.getUniqueID(), 4) <= myGuild.permissions.getOrDefault("setsell", 3);
		GuiChunkManager.canGuildSublet = myGuild.members.getOrDefault(Minecraft.getMinecraft().player.getUniqueID(), 4) <= myGuild.permissions.getOrDefault("managesublet", 3);
		GuiChunkManager.response = response;
		acctPlayer = acctP;
		acctGuild = acctG;
		setOverlayColors();
		GuiChunkManager.chunkMbrList.refreshList(selectedIdx);
		GuiChunkManager.wlList.refreshList(selectedIdx);
		GuiChunkManager.updateVisibility();
	}
	
	public GuiChunkManager(Guild myGuild, List<ChunkSummary> list, List<Integer> mapColors, String response, double acctP, double acctG) {
		this.myGuild = myGuild;
		chunkList = list;
		this.mapColors = mapColors;
		this.canGuildClaim = myGuild.members.getOrDefault(Minecraft.getMinecraft().player.getUniqueID(), 4) <= myGuild.permissions.getOrDefault("setclaim", 3);
		this.canGuildSell = myGuild.members.getOrDefault(Minecraft.getMinecraft().player.getUniqueID(), 4) <= myGuild.permissions.getOrDefault("setsell", 3);
		this.canGuildSublet = myGuild.members.getOrDefault(Minecraft.getMinecraft().player.getUniqueID(), 4) <= myGuild.permissions.getOrDefault("managesublet", 3);
		acctPlayer = acctP;
		acctGuild = acctG;
		response = "";
		isSubletMenu = false;
		setOverlayColors();
	}
	
	public void initGui() {
		button1 = new GuiButton(11, 3, 20, 75, 20, "Temp Claim");
		button2 = new GuiButton(12, button1.x, button1.y+ 23, 75, 20, "Guild Claim");
		button3 = new GuiButton(13, button1.x, button1.y+ 46, 75, 20, "New Outpost");
		button4 = new GuiButton(14, button1.x, button1.y+ 69, 75, 20, chunkList.get(12).isPublic ? "Public: Yes" : "Public: No");
		overlayToggle = new GuiButton(15, 3, this.height - 30, 100, 20, overlayOwners? "Owner Overlay: On" : "Owner Overlay: Off");
		chunkMbrList = new GuiListChunkMembers(this, mc.getMinecraft(), button1.x+button1.width+175, 30, this.width/3, (this.height-60)/2, 10);
		sellprice = new GuiTextField(1, this.fontRenderer, button1.x, button4.y+button4.height+12, 75, 20);
		memberAdd = new GuiTextField(2, this.fontRenderer, chunkMbrList.x, chunkMbrList.y+chunkMbrList.height+3, 100, 20);
		subletMenu = new GuiButton(16, button1.x, sellprice.y+sellprice.height+3, 75, 20, "Sublet Menu");
		addMember = new GuiButton(17, memberAdd.x+memberAdd.width+1, memberAdd.y, this.width - (memberAdd.x+memberAdd.width+1), 20, "Add Member");
		removeMember = new GuiButton(18, addMember.x, addMember.y+addMember.height+3, addMember.width, 20, "Remove");
		mapX = button1.x+button1.width+3;
		mapY = 16;
		mapD = 167;
		wlList = new GuiListWhitelist(this, mc.getMinecraft(), mapX, chunkMbrList.y, chunkMbrList.x-mapX-5, this.height-60, 11);
		backButton = new GuiButton(10, this.width/2 - 38, this.height- 30, 75, 20, "Back");
		wl0 = new GuiButton(20, chunkMbrList.x, removeMember.y+removeMember.height+5, 20, 20, "0");
		wl1 = new GuiButton(21, wl0.x+wl0.width, wl0.y, 20, 20, "1");
		wl2 = new GuiButton(22, wl1.x+wl1.width, wl0.y, 20, 20, "2");
		wl3 = new GuiButton(23, wl2.x+wl2.width, wl0.y, 20, 20, "3");
		this.buttonList.add(backButton);
		this.buttonList.add(button1);
		this.buttonList.add(button2);
		this.buttonList.add(button3);
		this.buttonList.add(button4);
		this.buttonList.add(overlayToggle);
		this.buttonList.add(subletMenu);
		this.buttonList.add(addMember);
		this.buttonList.add(removeMember);
		this.buttonList.add(wl0);
		this.buttonList.add(wl1);
		this.buttonList.add(wl2);
		this.buttonList.add(wl3);
		//permission updates on init
		updateVisibility();
	}
	
	private List<String> hTextPopulate(ChunkSummary sum) {
		List<String> hoveredText = new ArrayList<String>();
		hoveredText.add("Owner: "+ sum.owner);
		if (!sum.ownerIsGuild && !sum.ownerID.equals(Reference.NIL)) hoveredText.add(TextFormatting.BLUE+" Until "+ String.valueOf(new Timestamp(sum.tempclaimEnd)));
		hoveredText.add((sum.ownerIsGuild ? (sum.isForSale ? "Guild Buy Price: $" : "Value to Guild: $") : "Guild Buy Price: $")+df.format(sum.price)+(sum.isForSale ? TextFormatting.RED+" <FOR SALE>" :""));
		double ePrice = sum.permittedPlayers.size() > 0 ? (sum.price*0.05*(sum.permittedPlayers.size()-1)) : 0D;
		if (sum.ownerID.equals(Reference.NIL) && sum.leasePrice < 0) hoveredText.add("Temp Price: $"+df.format(ePrice+(sum.price*Main.ModConfig.TEMPCLAIM_RATE)));
		if (sum.ownerIsGuild && sum.leasePrice >= 0) hoveredText.add("Lease Price: $"+df.format(sum.leasePrice));
		hoveredText.add("Public: "+(sum.isPublic ? "Yes" : "No"));
		hoveredText.add("Outpost: "+(sum.isOutpost ? "Yes" : "No"));		
		return hoveredText;
	}
	
	public static void updateVisibility () {
		backButton.enabled = true;
		backButton.displayString = "Back";
		button2.enabled = false;
		button3.enabled = false;
		button1.enabled = false;
		addMember.visible = false;
		removeMember.visible = false;
		memberAdd.setVisible(false);
		sellprice.setVisible(false);
		wl0.visible = false;
		wl1.visible = false;
		wl2.visible = false;
		wl3.visible = false;
		sellpricelabel = "";
		button1.displayString = "Temp Claim";
		button2.displayString = "Guild Claim";
		button3.displayString = "New Outpost";
		overlayToggle.enabled = false;
		overlayToggle.displayString = overlayOwners ? "Owner Overlay: On" : "Owner Overlay: Off";
		button4.enabled = false;
		button4.displayString = chunkList.get(selectedIdx).isPublic ? "Public: Yes" : "Public: No";
		if (!isSubletMenu) {
			overlayToggle.enabled = true;
			topString = TextFormatting.GREEN+"Account: $"+df.format(acctPlayer) + (myGuild.guildID.equals(Reference.NIL) ? "" : TextFormatting.GOLD+" [Guild: $"+df.format(acctGuild)+"]");
			if (chunkList.get(selectedIdx).ownerID.equals(Reference.NIL)) {
				if ((chunkList.get(selectedIdx).price*.1) <= acctPlayer) button1.enabled = true;
				if (canGuildClaim && chunkList.get(selectedIdx).price <= acctGuild) {
					button2.enabled = true;
					button3.enabled = true;
				}
				else if (!canGuildClaim) {
					button2.enabled = false;
					button3.enabled = false;				
				}
			}
			else if (!chunkList.get(selectedIdx).ownerID.equals(Reference.NIL)) {
				button1.enabled = false;
				if (!chunkList.get(selectedIdx).ownerIsGuild) {
					if (canGuildClaim && chunkList.get(selectedIdx).price <= acctGuild) {
						button2.enabled = true;
						button3.enabled = true;
					}
					else if (!canGuildClaim) {
						button2.enabled = false;
						button3.enabled = false;				
					}
					boolean ispermitted = false;
					for (int i = 0; i < chunkList.get(selectedIdx).permittedPlayers.size(); i++) if (chunkList.get(selectedIdx).permittedPlayers.get(i).equals(player.getName())) {ispermitted = true; break;}
					if (chunkList.get(selectedIdx).ownerID.equals(player.getUniqueID()) || ispermitted) {
						button1.enabled = true;
						button1.displayString = "Extend Time";
						button4.enabled = true;
					}
				}
				else if(chunkList.get(selectedIdx).ownerIsGuild) {
					button2.enabled = false;
					button3.enabled = false;
					button4.enabled = false;
					if (chunkList.get(selectedIdx).isForSale && canGuildClaim && chunkList.get(selectedIdx).price <= acctGuild && !chunkList.get(selectedIdx).ownerID.equals(myGuild.guildID)) {
						button2.enabled = true;
						button2.displayString = "Guild Claim";
						button3.enabled = true;
						button3.displayString = "New Outpost";
					}
					else if (canGuildSell && chunkList.get(selectedIdx).ownerID.equals(myGuild.guildID)) {
						button2.enabled = true;
						button2.displayString = chunkList.get(selectedIdx).isForSale ? "Update Price" :"Sell Claim";
						sellprice.setVisible(true);
						sellpricelabel = "Sale Price";
						button3.enabled = true;
						button3.displayString = "Abandon Claim";
						button4.enabled = true;
					}
				}
			}
		}
		if (isSubletMenu) {
			button1.displayString = "Set Break";
			button2.displayString = "Set Interact";
			button3.displayString = "Set Interval";
			button4.displayString = "Set Price";
			overlayToggle.displayString = "Disable Sublet";
			sellpricelabel = "price/interval";
			sellprice.setVisible(true);
			backButton.displayString = "Clear List";
			backButton.enabled = false;
			if (chunkList.get(selectedIdx).ownerID.equals(myGuild.guildID)) {
				backButton.enabled = canGuildSublet;
				overlayToggle.enabled = canGuildSublet;
				button1.enabled = canGuildSublet && chunkList.get(selectedIdx).permittedPlayers.size() == 0;
				button2.enabled = canGuildSublet && chunkList.get(selectedIdx).permittedPlayers.size() == 0;
				button3.enabled = canGuildSublet;
				button4.enabled = canGuildSublet;
				wl0.visible = true;
				wl1.visible = true;
				wl2.visible = true;
				wl3.visible = true;
				wl0.enabled = chunkList.get(selectedIdx).guildLvlMin == 0 ? false : true;
				wl1.enabled = chunkList.get(selectedIdx).guildLvlMin == 1 ? false : true;
				wl2.enabled = chunkList.get(selectedIdx).guildLvlMin == 2 ? false : true;
				wl3.enabled = chunkList.get(selectedIdx).guildLvlMin == 3 ? false : true;
				sellprice.setEnabled(canGuildSublet);
				memberAdd.setVisible(true);
				addMember.visible = true;
				removeMember.visible = true;
				addMember.enabled = canGuildSublet && chunkList.get(selectedIdx).tempclaimEnd < System.currentTimeMillis();
				removeMember.enabled = canGuildSublet  && chunkList.get(selectedIdx).tempclaimEnd < System.currentTimeMillis();
				backButton.enabled = canGuildSublet;
			}
			else if (!chunkList.get(selectedIdx).ownerID.equals(Reference.NIL) && chunkList.get(selectedIdx).leasePrice != -1) {
				button1.enabled = (acctPlayer >= chunkList.get(selectedIdx).leasePrice && chunkList.get(selectedIdx).permittedPlayers.size() == 0);
				button1.displayString = "Rent";
				boolean isMember = false;
				for (String member : chunkList.get(selectedIdx).permittedPlayers) {if (member.equalsIgnoreCase(player.getName())) {isMember = true; break;}}
				button2.enabled = (isMember && acctPlayer >= (chunkList.get(selectedIdx).leasePrice*chunkList.get(selectedIdx).permittedPlayers.size()));
				button2.displayString = "Extend";
				addMember.visible = isMember;
				removeMember.visible = isMember;
				memberAdd.setVisible(isMember);
				
			}
			else if (chunkList.get(selectedIdx).ownerID.equals(player.getUniqueID())) {
				overlayToggle.enabled = false;
				memberAdd.setVisible(true);
				sellprice.setEnabled(false);
				addMember.visible = true;
				removeMember.visible = true;
			}
			if (chunkList.get(selectedIdx).permittedPlayers.size() == 0) {
				topString = "Subletting: "+ (chunkList.get(selectedIdx).leasePrice <= -1 ? "No" : "Yes");
				topString += chunkList.get(selectedIdx).leasePrice >= 0 ? TextFormatting.GREEN+" [Rent Price: $"+df.format(chunkList.get(selectedIdx).leasePrice)+"]" : "";
				topString += chunkList.get(selectedIdx).leasePrice >= 0 ? TextFormatting.AQUA+" [Rent Interval: "+String.valueOf(chunkList.get(selectedIdx).leaseDuration)+" hours]" : "";
			}
			else {
				topString = "Rented Until: "+ TextFormatting.BLUE+String.valueOf(new Timestamp(chunkList.get(selectedIdx).tempclaimEnd));
				topString += chunkList.get(selectedIdx).leasePrice >= 0 ? (TextFormatting.GREEN + " [Rate: $" +df.format(chunkList.get(selectedIdx).leasePrice*chunkList.get(selectedIdx).permittedPlayers.size())) : "";
				topString += chunkList.get(selectedIdx).leasePrice >= 0 ? (" for "+String.valueOf(chunkList.get(selectedIdx).leaseDuration)+" hours]") : "";
			}
		}
	}
	
	public ChunkPos fromIndex() {
		int cX = mc.player.chunkCoordX + ((selectedIdx%11) - 5);
		int cZ = mc.player.chunkCoordZ + ((selectedIdx/11) - 5);
		return new ChunkPos(cX, cZ);
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        chunkMbrList.handleMouseInput();
        wlList.handleMouseInput();
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (this.sellprice.isFocused() && CoreUtils.validNumberKey(keyCode)) this.sellprice.textboxKeyTyped(typedChar, keyCode);
		if (this.memberAdd.isFocused()) this.memberAdd.textboxKeyTyped(typedChar, keyCode);
    }
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 10) { //Exit Button
			if (!isSubletMenu) {
				mc.player.closeScreen();
				mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
			}
			if (isSubletMenu) Main.NET.sendToServer(new MessageChunkToServer(CkPktType.WL_CLEAR, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
		}
		if (button == button1) { //Temp Claim Button
			if (button.displayString == "Temp Claim") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.TEMPCLAIM, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			else if (button.displayString == "Extend Time")	Main.NET.sendToServer(new MessageChunkToServer(CkPktType.EXTEND, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			else if (button.displayString == "Set Break" && wlList.selectedIdx >= 0) {
				WhitelistItem wlItem = wlList.getSelectedMember().wlItem;
				wlItem.setCanBreak(wlItem.getCanBreak() ? false : true);
				Main.NET.sendToServer(new MessageChunkToServer(CkPktType.WL_CHANGE, fromIndex().x, fromIndex().z, "", wlItem));
			}
			else if (button.displayString == "Rent") {
				Main.NET.sendToServer(new MessageChunkToServer(CkPktType.RENT_START, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			}
		}
		if (button == button2) { //Guild Claim Button
			if (button.displayString == "Guild Claim") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.CLAIM, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			else if (button.displayString == "Update Price" || button.displayString == "Sell Claim"){
				double sellP = -1D;
				try {sellP = Math.abs(Double.valueOf(sellprice.getText()));} catch (NumberFormatException e) {}
				if (sellP != -1) Main.NET.sendToServer( new MessageChunkToServer(CkPktType.SELL, fromIndex().x, fromIndex().z, sellprice.getText(), new WhitelistItem("")));				
			}
			else if (button.displayString == "Set Interact" && wlList.selectedIdx >= 0) {
				WhitelistItem wlItem = wlList.getSelectedMember().wlItem;
				wlItem.setCanInteract(wlItem.getCanInteract() ? false: true);
				Main.NET.sendToServer(new MessageChunkToServer(CkPktType.WL_CHANGE, fromIndex().x, fromIndex().z, "", wlItem));
			}
			else if (button.displayString == "Extend") {
				Main.NET.sendToServer(new MessageChunkToServer(CkPktType.RENT_EXTEND, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			}			
		}
		if (button == button3) { //Outpost Claim Button
			if (button.displayString == "New Outpost") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.OUTPOST, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			else if (button.displayString == "Abandon Claim") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.ABANDON, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			else if (button.displayString == "Set Interval") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.SUBLET_INTERVAL, fromIndex().x, fromIndex().z, sellprice.getText(), new WhitelistItem("")));
		}
		if (button == button4) { //Public Toggle Button
			if (!isSubletMenu) Main.NET.sendToServer(new MessageChunkToServer(CkPktType.PUBLIC, fromIndex().x, fromIndex().z, "", new WhitelistItem("")));
			if (isSubletMenu) Main.NET.sendToServer(new MessageChunkToServer(CkPktType.SUBLET_PRICE, fromIndex().x, fromIndex().z, sellprice.getText(), new WhitelistItem("")));
		}
		if (button.id == 15) { //Overlay toggle
			if (!overlayToggle.displayString.equalsIgnoreCase("Disable Sublet")) {
				overlayOwners = overlayOwners ? false : true;
				overlayToggle.displayString = overlayOwners? "Owner Overlay: On" : "Owner Overlay: Off";
			}
			else {Main.NET.sendToServer(new MessageChunkToServer(CkPktType.SUBLET_PRICE, fromIndex().x, fromIndex().z, "-1", new WhitelistItem("")));}
			
		}
		if (button == subletMenu) {
			isSubletMenu = isSubletMenu ? false : true;
			subletMenu.displayString = isSubletMenu ? "Chunk Menu" : "Sublet Menu";
			wlList.selectedIdx = -1;
			wlList.selectedElement = -1;
			wlList.refreshList(selectedIdx);
		}
		if (button == addMember && memberAdd.getText().length() > 0) {
			Main.NET.sendToServer(new MessageChunkToServer(CkPktType.ADDMEMBER, fromIndex().x, fromIndex().z, memberAdd.getText(), new WhitelistItem("")));
		}
		if (button == removeMember && chunkMbrList.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageChunkToServer(CkPktType.REMOVEMEMBER, fromIndex().x, fromIndex().z, chunkMbrList.getSelectedMember().name, new WhitelistItem("")));
		}
		if (button == wl0 && canGuildSublet) {
			Main.NET.sendToServer(new MessageChunkToServer(CkPktType.WL_GUILD_MIN, fromIndex().x, fromIndex().z, "0", new WhitelistItem("")));
		}
		if (button == wl1 && canGuildSublet) {
			Main.NET.sendToServer(new MessageChunkToServer(CkPktType.WL_GUILD_MIN, fromIndex().x, fromIndex().z, "1", new WhitelistItem("")));		
		}
		if (button == wl2 && canGuildSublet) {
			Main.NET.sendToServer(new MessageChunkToServer(CkPktType.WL_GUILD_MIN, fromIndex().x, fromIndex().z, "2", new WhitelistItem("")));
		}
		if (button == wl3 && canGuildSublet) {
			Main.NET.sendToServer(new MessageChunkToServer(CkPktType.WL_GUILD_MIN, fromIndex().x, fromIndex().z, "3", new WhitelistItem("")));
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!isSubletMenu && mouseX > mapX && mouseX < mapX + mapD && mouseY > mapY && mouseY < mapY+mapD) {
        	double ivl = (mapD/11);
        	double yModifier = Math.floor((mouseY-mapY)/ivl)*11;
        	double xModifier = (mouseX-mapX)/ivl;        	
        	selectedIdx = (int) (Math.floor(xModifier) + Math.floor(yModifier));
        	button4.displayString = chunkList.get(selectedIdx).isPublic ? "Public: Yes" : "Public: No";
        	chunkMbrList.refreshList(selectedIdx);        	
        }
        sellprice.mouseClicked(mouseX, mouseY, mouseButton);
        memberAdd.mouseClicked(mouseX, mouseY, mouseButton);
        chunkMbrList.mouseClicked(mouseX, mouseY, mouseButton);
        wlList.mouseClicked(mouseX, mouseY, mouseButton);
        updateVisibility();
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        chunkMbrList.mouseReleased(mouseX, mouseY, state);
        wlList.mouseReleased(mouseX, mouseY, state);
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks)  {
    	this.drawDefaultBackground();
    	this.drawString(this.fontRenderer, topString, 3, 3, 16777215);
    	this.drawString(this.fontRenderer, "Permitted Players", chunkMbrList.x, 20, 16777215);
    	this.drawString(this.fontRenderer, sellpricelabel, sellprice.x, sellprice.y-10, 16777215);
    	this.chunkMbrList.drawScreen(mouseX, mouseY, partialTicks);
    	sellprice.drawTextBox();
    	if (isSubletMenu) {
    		this.drawString(this.fontRenderer, "Min Rank to Access", wl0.x, wl0.y-10, 16777215);
    		this.drawString(this.fontRenderer, "Whitelist: "+TextFormatting.GOLD+"Object "+TextFormatting.RED+"Break "+TextFormatting.BLUE+"Interact", wlList.x, wlList.y-10, 16777215);
    		memberAdd.drawTextBox();
    		wlList.drawScreen(mouseX, mouseY, partialTicks);
    	}
    	//Map area 
    	if (!isSubletMenu) {
	    	double x = mapX;
	    	double y = mapY;
	    	double d = mapD;    
	    	GlStateManager.disableLighting();
	        GlStateManager.disableFog();
	        Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder bufferbuilder = tessellator.getBuffer();
	        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	        GlStateManager.disableTexture2D();
	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
	        bufferbuilder.pos(x+d, y+d, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
	        bufferbuilder.pos(x+d, y, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
	        bufferbuilder.pos(x, y, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        
	        bufferbuilder.pos(x, y+d, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
	        tessellator.draw();
	        //Draw the map
	        double ivl = ((d-2)/176); //the draw dimensions for a single block on screen.
	        drawChunkMap(tessellator, bufferbuilder, ivl, x, y, d);
	        drawFacingArrow(tessellator, bufferbuilder, ivl);
	        //draw selection box
	        double boxX = x+ ((double)((int)selectedIdx%11)*(ivl*16));
	        double boxY = y+ ((double)((int)selectedIdx/11)*(ivl*16));
	        GlStateManager.enableBlend();
	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
	        bufferbuilder.pos(boxX+(ivl*16)	,boxY+(ivl*16)		, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();
	        bufferbuilder.pos(boxX+(ivl*16)	,boxY				, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();
	        bufferbuilder.pos(boxX			,boxY				, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();        
	        bufferbuilder.pos(boxX			,boxY+(ivl*16)		, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();
	        tessellator.draw();
	        //draw the overlay if enabled
	        if (overlayOwners) {
	        	for (int i = 0; i < 121; i++) {
	        		boxX = x+ ((double)((int)i%11)*(ivl*16));
	                boxY = y+ ((double)((int)i/11)*(ivl*16));
	                int alpha = 0;
	                Color clr = Color.WHITE;
	                if (!chunkList.get(i).ownerID.equals(Reference.NIL)) {
	                	clr = colorReference.get(overlayColors.get(chunkList.get(i).ownerID));
	                	alpha = 128;
	                }
	                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
	                bufferbuilder.pos(boxX+(ivl*16)	,boxY+(ivl*16)		, 0.0D).tex(1.0D, 1.0D).color(clr.getRed(), clr.getGreen(), clr.getBlue(), alpha).endVertex();
	                bufferbuilder.pos(boxX+(ivl*16)	,boxY				, 0.0D).tex(1.0D, 1.0D).color(clr.getRed(), clr.getGreen(), clr.getBlue(), alpha).endVertex();
	                bufferbuilder.pos(boxX			,boxY				, 0.0D).tex(1.0D, 1.0D).color(clr.getRed(), clr.getGreen(), clr.getBlue(), alpha).endVertex();        
	                bufferbuilder.pos(boxX			,boxY+(ivl*16)		, 0.0D).tex(1.0D, 1.0D).color(clr.getRed(), clr.getGreen(), clr.getBlue(), alpha).endVertex();
	                tessellator.draw();
	        	}
	        }
	        GlStateManager.disableBlend();
	        GlStateManager.enableTexture2D();
	        //tooltip code
	        if (mouseX > x && mouseX < x+d && mouseY > y && mouseY < y+d) {
	        	double interval = (d/11);
	        	double yModifier = Math.floor((mouseY-y)/interval)*11;
	        	double xModifier = (mouseX-x)/interval;        	
	        	hoveredIdx = (int) (Math.floor(xModifier) + Math.floor(yModifier));        	
	        	if (hoveredIdx < chunkList.size()) {
	        		RenderHelper.enableGUIStandardItemLighting();
	        		GuiUtils.drawHoveringText(hTextPopulate(chunkList.get(hoveredIdx)), mouseX, mouseY, this.width, this.height, 200, this.fontRenderer);
	        		RenderHelper.disableStandardItemLighting();
	        	}
	        }
	    }
    	//End of Map Drawing Block
        this.drawCenteredString(this.fontRenderer, response, this.width/2, this.height-45, Integer.parseInt("FFAA00", 16));
    	super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawChunkMap(Tessellator tess, BufferBuilder buf, double ivl, double x, double y, double d) {
    	for (double yy = 0; yy < 176; yy++) {
        	for (double xx = 0; xx < 176; xx++) {
                Color color = new Color(mapColors.get((int) ((yy*176)+xx))); 
        		buf.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buf.pos((x+1)+(xx*ivl)+ivl, (y+1)+(yy*ivl)+ivl	, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
                buf.pos((x+1)+(xx*ivl)+ivl, (y+1)+(yy*ivl)		, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
                buf.pos((x+1)+(xx*ivl)	, (y+1)+(yy*ivl)		, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();        
                buf.pos((x+1)+(xx*ivl)	, (y+1)+(yy*ivl)+ivl	, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
                tess.draw();
        	}
        }
        //draw the grid
        for (int v = 1; v < 11; v++) {
        	buf.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buf.pos(x+(ivl*v*16)+1	, y+d	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buf.pos(x+(ivl*v*16)+1	, y		, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buf.pos(x+(ivl*v*16)	, y		, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        
            buf.pos(x+(ivl*v*16)	, y+d	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            tess.draw();
        }
        for (int h = 1; h < 11; h++) {
        	buf.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buf.pos(x+d	,y+(ivl*h*16)+1	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buf.pos(x+d	,y+(ivl*h*16)	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buf.pos(x		,y+(ivl*h*16)	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        
            buf.pos(x		,y+(ivl*h*16)+1	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            tess.draw();
        }
    }
    
    private void drawFacingArrow(Tessellator tess, BufferBuilder buf, double ivl) {
    	double centX = mapX+ (88*ivl);
        double centY = mapY+ (88*ivl);
        switch (mc.player.getHorizontalFacing()) {
        case NORTH: {
        	buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX + 1, centY+5, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX + 1, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX - 1, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	buf.pos(centX - 1, centY+5, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	tess.draw();
        	buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX -3, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX +3, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX	, centY-5, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	        	        	
        	tess.draw();
        	break;
        }
        case SOUTH: {
        	buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX + 1, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX + 1, centY-5, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX - 1, centY-5, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	buf.pos(centX - 1, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	tess.draw();
        	buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX	, centY+5, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX +3, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX -3, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	        	        	        	
        	tess.draw();
        	break;
        }
        case EAST: {
        	buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX + 1, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX + 1, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX - 5, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	buf.pos(centX - 5, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	tess.draw();
        	buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX + 1, centY+3, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX + 5, centY, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX + 1, centY-3, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	        	
        	tess.draw();
        	break;
        }
        case WEST: {
        	buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX + 5, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX + 5, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX - 1, centY-1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	buf.pos(centX - 1, centY+1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	tess.draw();
        	buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        	buf.pos(centX - 1, centY+3, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX - 1, centY-3, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        	buf.pos(centX - 5, centY, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        	
        	tess.draw();
        	break;
        }
        default:
        }
    }

    public static class ChunkSummary {
		public boolean ownerIsGuild;
		public UUID ownerID;
		public String owner;
		public double price;
		public double leasePrice;
		public int leaseDuration;
		public int guildLvlMin;
		public List<WhitelistItem> whitelist;
		public long tempclaimEnd;
		public boolean isPublic;
		public boolean isForSale;
		public boolean isOutpost;
		public List<String> permittedPlayers;
		
		public ChunkSummary() {}
		
		public ChunkSummary(boolean guildOwned, UUID ownerID, String owner, double price, boolean isPublic, boolean isForSale, boolean isOutpost, long claimEnd, int leaseDuration, List<WhitelistItem> whitelist, List<String> members, double leasePrice, int guildLvlMin) {
			this.ownerIsGuild = guildOwned;
			this.ownerID = ownerID;
			this.owner = owner;
			this.price = price;
			this.leasePrice = leasePrice;
			this.leaseDuration = leaseDuration;
			this.isPublic = isPublic;
			this.isForSale = isForSale;
			this.isOutpost = isOutpost;
			tempclaimEnd = claimEnd;
			this.whitelist = whitelist;
			permittedPlayers = members;
			this.guildLvlMin = guildLvlMin;
		}
		
		public static ChunkSummary fromNBT(NBTTagCompound nbt) {
			ChunkSummary sum = new ChunkSummary();
			sum.ownerIsGuild = nbt.getBoolean("guildowned");
			sum.ownerID = nbt.getUniqueId("UUID");
			sum.owner = nbt.getString("owner");
			sum.price = nbt.getDouble("price");
			sum.leasePrice = nbt.getDouble("leaseprice");
			sum.leaseDuration = nbt.getInteger("duration");
			sum.guildLvlMin = nbt.getInteger("levelmin");
			sum.isPublic = nbt.getBoolean("public");
			sum.isForSale = nbt.getBoolean("forsale");
			sum.isOutpost = nbt.getBoolean("outpost");
			sum.tempclaimEnd = nbt.getLong("claimend");
			List<WhitelistItem> wlist = new ArrayList<WhitelistItem>();
			NBTTagList list = nbt.getTagList("whitelist", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				wlist.add(new WhitelistItem(list.getCompoundTagAt(i).getCompoundTag("whiteitem")));
			}
			sum.whitelist = wlist;
			List<String> plist = new ArrayList<String>();
			list = nbt.getTagList("players", Constants.NBT.TAG_STRING);
			for (int i = 0; i < list.tagCount(); i++) {
				plist.add(list.getStringTagAt(i));
			}
			sum.permittedPlayers = plist;
			return sum;
		}
		
		public static NBTTagCompound toNBT(ChunkSummary sum) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("guildowned", sum.ownerIsGuild);
			nbt.setUniqueId("UUID", sum.ownerID);
			nbt.setString("owner", sum.owner);
			nbt.setDouble("price", sum.price);
			nbt.setDouble("leaseprice", sum.leasePrice);
			nbt.setInteger("duration", sum.leaseDuration);
			nbt.setInteger("levelmin", sum.guildLvlMin);
			nbt.setLong("claimend", sum.tempclaimEnd);
			nbt.setBoolean("public", sum.isPublic);
			nbt.setBoolean("forsale", sum.isForSale);
			nbt.setBoolean("outpost", sum.isOutpost);
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < sum.whitelist.size(); i++) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setTag("whiteitem", sum.whitelist.get(i).toNBT());
				list.appendTag(snbt);
			}
			nbt.setTag("whitelist", list);
			list = new NBTTagList();
			for (int i = 0; i < sum.permittedPlayers.size(); i++) {
				list.appendTag(new NBTTagString(sum.permittedPlayers.get(i)));
			}
			nbt.setTag("players", list);
			return nbt;
		}
	}

	public class GuiListChunkMembers extends GuiNewListExtended{
	    private final GuiChunkManager chunkManager;
	    public List<String> mbrNames;
	    private final List<GuiListChunkMembersEntry> entries = Lists.<GuiListChunkMembersEntry>newArrayList();
	    /** Index to the currently selected member */
	    private int selectedIdx = -1;
		
		public GuiListChunkMembers(GuiChunkManager guiGM, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.chunkManager = guiGM;
		}
		
	    public void refreshList(int index)
	    {
	    	entries.clear();
	    	this.mbrNames = chunkManager.chunkList.get(index).permittedPlayers;
	        for (int i = 0; i < mbrNames.size(); i++){
	            this.entries.add(new GuiListChunkMembersEntry(this, mbrNames.get(i)));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListChunkMembersEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListChunkMembersEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListChunkMembersEntry implements GuiNewListExtended.IGuiNewListEntry{
		private final String name; 
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListChunkMembers containingListSel;
		
		public GuiListChunkMembersEntry (GuiListChunkMembers listSelectionIn, String player) {
			containingListSel = listSelectionIn;
			this.name = player;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			this.client.fontRenderer.drawString(name, x+3, y , 16777215);
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {		
		}

	}

	public class GuiListWhitelist extends GuiNewListExtended{
	    private final GuiChunkManager chunkManager;
	    public List<WhitelistItem> whitelist;
	    private final List<GuiListWhitelistEntry> entries = Lists.<GuiListWhitelistEntry>newArrayList();
	    /** Index to the currently selected member */
	    private int selectedIdx = -1;
		
		public GuiListWhitelist(GuiChunkManager guiGM, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.chunkManager = guiGM;
		}
		
	    public void refreshList(int index)
	    {
	    	entries.clear();
	    	this.whitelist = chunkManager.chunkList.get(index).whitelist;
	        for (int i = 0; i < whitelist.size(); i++){
	            this.entries.add(new GuiListWhitelistEntry(this, whitelist.get(i)));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListWhitelistEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListWhitelistEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListWhitelistEntry implements GuiNewListExtended.IGuiNewListEntry{
		private final WhitelistItem wlItem; 
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListWhitelist containingListSel;
		
		public GuiListWhitelistEntry (GuiListWhitelist listSelectionIn, WhitelistItem wlItem) {
			containingListSel = listSelectionIn;
			this.wlItem = wlItem;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			String wlEntry = TextFormatting.GOLD+ wlItem.getBlock().substring(wlItem.getBlock().indexOf(":") != -1 ? wlItem.getBlock().indexOf(":")+1 : 0); 
			wlEntry += wlItem.getEntity().substring(wlItem.getEntity().indexOf("Entity") != -1 ? wlItem.getEntity().indexOf("Entity")+ 6 : 0); 
			wlEntry += TextFormatting.RED+" "+ (wlItem.getCanBreak() ? "Yes":"No") + TextFormatting.BLUE+" "+ (wlItem.getCanInteract() ? "Yes":"No");
			this.client.fontRenderer.drawString(wlEntry, x+3, y , 16777215);
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {		
		}

	}
}
	

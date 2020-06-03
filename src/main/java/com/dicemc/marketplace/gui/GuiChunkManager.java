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
import com.dicemc.marketplace.events.GuiEventHandler;
import com.dicemc.marketplace.events.PlayerEventHandler;
import com.dicemc.marketplace.network.MessageAccountInfoToServer;
import com.dicemc.marketplace.network.MessageChunkToServer;
import com.dicemc.marketplace.network.MessageGuildInfoToServer;
import com.dicemc.marketplace.util.CkPktType;
import com.dicemc.marketplace.util.Reference;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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


public class GuiChunkManager extends GuiScreen{
	public static List<ChunkSummary> chunkList;
	public static List<Integer> mapColors;
	private GuiButton tempClaim, guildClaim, outpostClaim, overlayToggle;
	private static GuiButton publicToggle;
	private GuiTextField sellprice;
	public static double acctPlayer, acctGuild;
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private static GuiListChunkMembers chunkMbrList;
	private static int selectedIdx = 12;
	private boolean overlayOwners = false;
	public static UUID playerGuildID;
	public static boolean canGuildClaim;
	public static boolean canGuildSell;
	public static Map<UUID, Integer> overlayColors = new HashMap<UUID, Integer>();
	private Map<Integer, Color> colorReference = colorSetup();
	private static String response = "";
	private String sellpricelabel = "";
	
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
	
	public static void guiUpdate(List<ChunkSummary> list, List<Integer> mapColors, UUID playerGuildID, boolean canGuildClaim, boolean canGuildSell, String response, double acctP, double acctG) {
		chunkList = list;
		GuiChunkManager.mapColors = mapColors;
		GuiChunkManager.playerGuildID = playerGuildID;
		GuiChunkManager.canGuildClaim = canGuildClaim;
		GuiChunkManager.canGuildSell = canGuildSell;
		GuiChunkManager.response = response;
		acctPlayer = acctP;
		acctGuild = acctG;
		setOverlayColors();
		GuiChunkManager.chunkMbrList.refreshList(selectedIdx);
		GuiChunkManager.publicToggle.displayString = GuiChunkManager.chunkList.get(selectedIdx).isPublic ? "Public: Yes" : "Public: No";
	}
	
	public GuiChunkManager(List<ChunkSummary> list, List<Integer> mapColors, UUID playerGuildID, boolean canGuildClaim, boolean canGuildSell, String response, double acctP, double acctG) {
		chunkList = list;
		this.mapColors = mapColors;
		this.playerGuildID = playerGuildID;
		this.canGuildClaim = canGuildClaim;
		this.canGuildSell = canGuildSell;
		acctPlayer = acctP;
		acctGuild = acctG;
		response = "";
		setOverlayColors();
	}
	
	public void initGui() {
		tempClaim = new GuiButton(11, this.width/2 - 38, 20, 75, 20, "Temp Claim");
		guildClaim = new GuiButton(12, tempClaim.x, tempClaim.y+ 23, 75, 20, "Guild Claim");
		outpostClaim = new GuiButton(13, tempClaim.x, tempClaim.y+ 46, 75, 20, "New Outpost");
		publicToggle = new GuiButton(14, tempClaim.x, tempClaim.y+ 69, 75, 20, chunkList.get(12).isPublic ? "Public: Yes" : "Public: No");
		overlayToggle = new GuiButton(15, 3, this.height - 30, 100, 20, overlayOwners? "Owner Overlay: On" : "Owner Overlay: Off");
		chunkMbrList = new GuiListChunkMembers(this, mc.getMinecraft(), tempClaim.x+tempClaim.width+5, 30, this.width/3, (this.height-60)/2, 10);
		sellprice = new GuiTextField(1, this.fontRenderer, tempClaim.x, this.height-70, 75, 20);
		this.buttonList.add(new GuiButton(10, tempClaim.x, this.height- 30, 75, 20, "Back"));
		this.buttonList.add(tempClaim);
		this.buttonList.add(guildClaim);
		this.buttonList.add(outpostClaim);
		this.buttonList.add(publicToggle);
		this.buttonList.add(overlayToggle);
		//permission updates on init
		accessUpdate();
	}
	
	public void accessUpdate () {
		guildClaim.enabled = false;
		outpostClaim.enabled = false;
		tempClaim.enabled = false;
		sellprice.setVisible(false);
		sellpricelabel = "";
		tempClaim.displayString = "Temp Claim";
		guildClaim.displayString = "Guild Claim";
		outpostClaim.displayString = "New Outpost";
		publicToggle.enabled = false;
		if (chunkList.get(selectedIdx).ownerID.equals(Reference.NIL)) {
			if ((chunkList.get(selectedIdx).price*.1) <= acctPlayer) tempClaim.enabled = true;
			if (canGuildClaim && chunkList.get(selectedIdx).price <= acctGuild) {
				guildClaim.enabled = true;
				outpostClaim.enabled = true;
			}
			else if (!canGuildClaim) {
				guildClaim.enabled = false;
				outpostClaim.enabled = false;				
			}
		}
		else if (!chunkList.get(selectedIdx).ownerID.equals(Reference.NIL)) {
			tempClaim.enabled = false;
			if (!chunkList.get(selectedIdx).ownerIsGuild) {
				if (canGuildClaim && chunkList.get(selectedIdx).price <= acctGuild) {
					guildClaim.enabled = true;
					outpostClaim.enabled = true;
				}
				else if (!canGuildClaim) {
					guildClaim.enabled = false;
					outpostClaim.enabled = false;				
				}
				boolean ispermitted = false;
				for (int i = 0; i < chunkList.get(selectedIdx).permittedPlayers.size(); i++) if (chunkList.get(selectedIdx).permittedPlayers.get(i).equals(mc.player.getName())) {ispermitted = true; break;}
				if (chunkList.get(selectedIdx).ownerID.equals(mc.player.getUniqueID()) || ispermitted) {
					tempClaim.enabled = true;
					tempClaim.displayString = "Extend Time";
					publicToggle.enabled = true;
				}
			}
			else if(chunkList.get(selectedIdx).ownerIsGuild) {
				guildClaim.enabled = false;
				outpostClaim.enabled = false;
				publicToggle.enabled = false;
				if (chunkList.get(selectedIdx).isForSale && canGuildClaim && chunkList.get(selectedIdx).price <= acctGuild && !chunkList.get(selectedIdx).ownerID.equals(playerGuildID)) {
					guildClaim.enabled = true;
					guildClaim.displayString = "Guild Claim";
					outpostClaim.enabled = true;
					outpostClaim.displayString = "New Outpost";
				}
				else if (canGuildSell && chunkList.get(selectedIdx).ownerID.equals(playerGuildID)) {
					guildClaim.enabled = true;
					guildClaim.displayString = chunkList.get(selectedIdx).isForSale ? "Update Price" :"Sell Claim";
					sellprice.setVisible(true);
					sellpricelabel = "Sale Price";
					outpostClaim.enabled = true;
					outpostClaim.displayString = "Abandon Claim";
					publicToggle.enabled = true;
				}
			}
		}
	}
	
	public ChunkPos fromIndex() {
		int cX = mc.player.chunkCoordX + ((selectedIdx%5) - 2);
		int cZ = mc.player.chunkCoordZ + ((selectedIdx/5) - 2);
		return new ChunkPos(cX, cZ);
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (this.sellprice.isFocused() && CoreUtils.validNumberKey(keyCode)) this.sellprice.textboxKeyTyped(typedChar, keyCode);
    }
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 10) { //Exit Button
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		if (button.id == 11) { //Temp Claim Button
			if (button.displayString == "Temp Claim") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.TEMPCLAIM, fromIndex().x, fromIndex().z, ""));
			else if (button.displayString == "Extend Time")	Main.NET.sendToServer(new MessageChunkToServer(CkPktType.EXTEND, fromIndex().x, fromIndex().z, ""));
		}
		if (button.id == 12) { //Guild Claim Button
			if (button.displayString == "Guild Claim") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.CLAIM, fromIndex().x, fromIndex().z, ""));
			else {
				double sellP = -1D;
				try {sellP = Math.abs(Double.valueOf(sellprice.getText()));} catch (NumberFormatException e) {}
				if (sellP != -1) Main.NET.sendToServer( new MessageChunkToServer(CkPktType.SELL, fromIndex().x, fromIndex().z, sellprice.getText()));				
			}
			
		}
		if (button.id == 13) { //Outpost Claim Button
			if (button.displayString == "New Outpost") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.OUTPOST, fromIndex().x, fromIndex().z, ""));
			else if (button.displayString == "Abandon Claim") Main.NET.sendToServer(new MessageChunkToServer(CkPktType.ABANDON, fromIndex().x, fromIndex().z, ""));
		}
		if (button.id == 14) { //Public Toggle Button
			Main.NET.sendToServer(new MessageChunkToServer(CkPktType.PUBLIC, fromIndex().x, fromIndex().z, ""));
		}
		if (button.id == 15) { //Overlay toggle
			overlayOwners = overlayOwners ? false : true;
			overlayToggle.displayString = overlayOwners? "Owner Overlay: On" : "Owner Overlay: Off";
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseX > 3 && mouseX < tempClaim.x - 5 && mouseY > 16 && mouseY < tempClaim.x + 11) {
        	double ivl = ((tempClaim.x - 10)/5);
        	double yModifier = Math.floor((mouseY-16)/ivl)*5;
        	double xModifier = (mouseX-3)/ivl;        	
        	selectedIdx = (int) (Math.floor(xModifier) + Math.floor(yModifier));
        	publicToggle.displayString = chunkList.get(selectedIdx).isPublic ? "Public: Yes" : "Public: No";
        	chunkMbrList.refreshList(selectedIdx);        	
        }
        sellprice.mouseClicked(mouseX, mouseY, mouseButton);
        accessUpdate();
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks)  {
    	String priceLabel =  chunkList.get(selectedIdx).ownerIsGuild ? (chunkList.get(selectedIdx).isForSale ? "Price: $" : "Value: $") : "Guild: $";
    	double tPrice = chunkList.get(selectedIdx).price;
    	double ePrice = 0D;
    	ePrice = chunkList.get(selectedIdx).permittedPlayers.size() > 0 ? (tPrice*0.05*(chunkList.get(selectedIdx).permittedPlayers.size()-1)) : 0D;
    	priceLabel += String.valueOf(tPrice) + (chunkList.get(selectedIdx).isForSale ? TextFormatting.RED+" <FOR SALE>" : 
    		tempClaim.enabled ? TextFormatting.GREEN+" Temp $"+String.valueOf((tPrice*0.1)+(ePrice)) : "");
    	this.drawDefaultBackground();
    	String until = (!chunkList.get(selectedIdx).ownerIsGuild && !chunkList.get(selectedIdx).ownerID.equals(Reference.NIL)) ? TextFormatting.BLUE+" Until "+ String.valueOf(new Timestamp(chunkList.get(selectedIdx).tempclaimEnd)) : "";
    	this.drawString(this.fontRenderer, "Owner: "+chunkList.get(selectedIdx).owner+until, 3, 3, 16777215);
    	this.drawString(this.fontRenderer, priceLabel , chunkMbrList.x, 3, 16777215);
    	this.drawString(this.fontRenderer, TextFormatting.RED+"Outpost: "+String.valueOf(chunkList.get(selectedIdx).isOutpost), tempClaim.x, publicToggle.y+publicToggle.height+15, 16777215);
    	//this.drawString(this.fontRenderer, "Redstone: "+String.valueOf(chunkList.get(selectedIdx).redstone), tempClaim.x, publicToggle.y+publicToggle.height+25, 16777215);
    	this.drawString(this.fontRenderer, "Permitted Players", chunkMbrList.x, 20, 16777215);
    	this.drawString(this.fontRenderer, sellpricelabel, sellprice.x, sellprice.y-10, 16777215);
    	String gBalStr = playerGuildID.equals(Reference.NIL) ? "" : TextFormatting.GOLD+" [Guild: $"+df.format(acctGuild)+"]";
    	this.drawString(this.fontRenderer, TextFormatting.GREEN+"Account: $", chunkMbrList.x, chunkMbrList.y+ chunkMbrList.height+ 5, 16777215);
    	this.drawString(this.fontRenderer, df.format(acctPlayer), chunkMbrList.x, chunkMbrList.y+ chunkMbrList.height+ 15, 16777215);
    	this.drawString(this.fontRenderer, gBalStr, chunkMbrList.x, chunkMbrList.y+ chunkMbrList.height+ 25, 16777215);
    	this.chunkMbrList.drawScreen(mouseX, mouseY, partialTicks);
    	sellprice.drawTextBox();
    	//Map area 
    	double x = 3;
    	double y = 16;
    	double d = tempClaim.x - 5 - x;    
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
        double ivl = ((d-2)/80); //the draw dimensions for a single block on screen.
        for (double yy = 0; yy < 80; yy++) {
        	for (double xx = 0; xx < 80; xx++) {
                Color color = new Color(mapColors.get((int) ((yy*80)+xx))); 
        		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos((x+1)+(xx*ivl)+ivl, (y+1)+(yy*ivl)+ivl	, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
                bufferbuilder.pos((x+1)+(xx*ivl)+ivl, (y+1)+(yy*ivl)		, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
                bufferbuilder.pos((x+1)+(xx*ivl)	, (y+1)+(yy*ivl)		, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();        
                bufferbuilder.pos((x+1)+(xx*ivl)	, (y+1)+(yy*ivl)+ivl	, 0.0D).color(color.getRed(), color.getGreen(), color.getBlue(), 255).endVertex();
                tessellator.draw();
        	}
        }
        //draw the grid
        for (int v = 1; v < 5; v++) {
        	bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(x+(ivl*v*16)+1	, y+d	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x+(ivl*v*16)+1	, y		, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x+(ivl*v*16)	, y		, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        
            bufferbuilder.pos(x+(ivl*v*16)	, y+d	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
        }
        for (int h = 1; h < 5; h++) {
        	bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(x+d	,y+(ivl*h*16)+1	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x+d	,y+(ivl*h*16)	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x		,y+(ivl*h*16)	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();        
            bufferbuilder.pos(x		,y+(ivl*h*16)+1	, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
        }
        //draw the facing arrow
        drawFacingArrow(tessellator, bufferbuilder, ivl);
        //draw selection box
        double boxX = x+ ((double)((int)selectedIdx%5)*(ivl*16));
        double boxY = y+ ((double)((int)selectedIdx/5)*(ivl*16));
        GlStateManager.enableBlend();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(boxX+(ivl*16)	,boxY+(ivl*16)		, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();
        bufferbuilder.pos(boxX+(ivl*16)	,boxY				, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();
        bufferbuilder.pos(boxX			,boxY				, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();        
        bufferbuilder.pos(boxX			,boxY+(ivl*16)		, 0.0D).tex(1.0D, 1.0D).color(70, 151, 255, 128).endVertex();
        tessellator.draw();
        //draw the overlay if enabled
        if (overlayOwners) {
        	for (int i = 0; i < 25; i++) {
        		boxX = x+ ((double)((int)i%5)*(ivl*16));
                boxY = y+ ((double)((int)i/5)*(ivl*16));
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
    	//End of Map Drawing Block
        this.drawCenteredString(this.fontRenderer, response, this.width/2, this.height-45, Integer.parseInt("FFAA00", 16));
    	super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawFacingArrow(Tessellator tess, BufferBuilder buf, double ivl) {
    	double centX = 3+ (40*ivl);
        double centY = 16+(40*ivl);
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
		public boolean redstone;
		public List<String> whitelist;
		public long tempclaimEnd;
		public boolean isPublic;
		public boolean isForSale;
		public boolean isOutpost;
		public List<String> permittedPlayers;
		
		public ChunkSummary() {}
		
		public ChunkSummary(boolean guildOwned, UUID ownerID, String owner, double price, boolean redstone, boolean isPublic, boolean isForSale, boolean isOutpost, long claimEnd, List<String> whitelist, List<String> members) {
			this.ownerIsGuild = guildOwned;
			this.ownerID = ownerID;
			this.owner = owner;
			this.price = price;
			this.redstone = redstone;
			this.isPublic = isPublic;
			this.isForSale = isForSale;
			this.isOutpost = isOutpost;
			tempclaimEnd = claimEnd;
			this.whitelist = whitelist;
			permittedPlayers = members;
		}
		
		public static ChunkSummary fromNBT(NBTTagCompound nbt) {
			ChunkSummary sum = new ChunkSummary();
			sum.ownerIsGuild = nbt.getBoolean("guildowned");
			sum.ownerID = nbt.getUniqueId("UUID");
			sum.owner = nbt.getString("owner");
			sum.price = nbt.getDouble("price");
			sum.redstone = nbt.getBoolean("redstone");
			sum.isPublic = nbt.getBoolean("public");
			sum.isForSale = nbt.getBoolean("forsale");
			sum.isOutpost = nbt.getBoolean("outpost");
			sum.tempclaimEnd = nbt.getLong("claimend");
			List<String> wlist = new ArrayList<String>();
			NBTTagList list = nbt.getTagList("whitelist", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				wlist.add(list.getCompoundTagAt(i).getString("whiteitem"));
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
			nbt.setBoolean("redstone", sum.redstone);
			nbt.setLong("claimend", sum.tempclaimEnd);
			nbt.setBoolean("public", sum.isPublic);
			nbt.setBoolean("forsale", sum.isForSale);
			nbt.setBoolean("outpost", sum.isOutpost);
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < sum.whitelist.size(); i++) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setString("whiteitem", sum.whitelist.get(i));
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

	public class GuiListChunkMembers extends GuiListExtendedMember{
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
	
	public class GuiListChunkMembersEntry implements GuiListExtendedMember.IGuiNewListEntry{
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
}

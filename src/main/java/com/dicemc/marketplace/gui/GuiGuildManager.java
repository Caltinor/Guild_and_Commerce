package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembersEntry;
import com.dicemc.marketplace.network.MessageAccountInfoToServer;
import com.dicemc.marketplace.network.MessageGuildInfoToServer;
import com.dicemc.marketplace.util.Reference;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiGuildManager extends GuiScreen{
	public static Guild guild;
	private static Account acctGuild;
	private static double worthTax, worthGuild, balP;
	private static Map<ChunkPos, Double> chunkValues;
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private static DecimalFormat taxformat = new DecimalFormat("0.00000");
    protected static String screenTitle = "Guild Info";
    private static GuiTextField guildNameTextField, guildFundExchangeTextField, guildTaxTextField;
    private static GuiTextField perm0TF, perm1TF, perm2TF, perm3TF;
    private static GuiListGuildChunks guicoreChunkList, guioutpostChunkList;
    public long time;
    public static Map<String, Boolean> discriminators = new HashMap<String, Boolean>();
    
    public static void syncAccounts(Account acctG, double balancePlayer) {acctGuild = acctG; balP = balancePlayer;}
    
    public static void syncGui(Guild guild, Account acctGuild, double worthT, double worthG, Map<ChunkPos, Double> chunkValues, double balancePlayer) {
    	GuiGuildManager.guild = guild;
    	GuiGuildManager.acctGuild = acctGuild;
    	GuiGuildManager.worthTax = worthT;
    	GuiGuildManager.worthGuild = worthG;
    	GuiGuildManager.chunkValues = chunkValues;
    	GuiGuildManager.balP = balancePlayer;
    	GuiGuildManager.screenTitle = "Guild Info";
    	GuiGuildManager.guicoreChunkList.pos = GuiGuildManager.guild.coreLand;
    	GuiGuildManager.guicoreChunkList.chunkValues = GuiGuildManager.chunkValues;
    	GuiGuildManager.guicoreChunkList.refreshList();
    	GuiGuildManager.guioutpostChunkList.pos = GuiGuildManager.guild.outpostLand;
    	GuiGuildManager.guioutpostChunkList.chunkValues = GuiGuildManager.chunkValues;
    	GuiGuildManager.guioutpostChunkList.refreshList();
		newDescrims();
		GuiGuildManager.guildNameTextField.setText(guild.guildName);
		GuiGuildManager.guildTaxTextField.setText(taxformat.format(guild.guildTax));
		GuiGuildManager.guildTaxTextField.setCursorPositionZero();
		GuiGuildManager.perm0TF.setText(guild.permLevels.get(0));
		GuiGuildManager.perm1TF.setText(guild.permLevels.get(1));
		GuiGuildManager.perm2TF.setText(guild.permLevels.get(2));
		GuiGuildManager.perm3TF.setText(guild.permLevels.get(3));
    }
	
	public GuiGuildManager(Guild guild, Account acctGuild, double worthT, double worthG, Map<ChunkPos, Double> chunkValues, double balancePlayer) {
		this.guild = guild;
		this.acctGuild = acctGuild;
		this.worthTax = worthT;
		this.worthGuild = worthG;
		this.chunkValues = chunkValues;
		balP = balancePlayer;
		newDescrims();
	}
	
	public static void newDescrims() {
		discriminators.put("name", false);
		discriminators.put("open", false);
		discriminators.put("tax", false);
		discriminators.put("permnames", false);
		discriminators.put("permvalues", false);
		discriminators.put("members", false);
	}
	
	public void accessUpdate() {
		if (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("setopen")) for (GuiButton button : this.buttonList) {if (button.id == 12) button.enabled = false;}
		if (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("setwithdraw")) for (GuiButton button : this.buttonList) {if (button.id == 14) button.enabled = false;}
		if (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("setperms")) {
			perm0TF.setEnabled(false);
			perm1TF.setEnabled(false);
			perm2TF.setEnabled(false);
			perm3TF.setEnabled(false);
			for (GuiButton button : this.buttonList) if (button.id == 15) button.enabled = false;
		}
		if (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("setname")) guildNameTextField.setEnabled(false);
		if (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("settax")) guildTaxTextField.setEnabled(false);
	}
	
	public void initGui() {
		this.guildNameTextField = new GuiTextField(0, this.fontRenderer, 8, 30, (this.width / 4), 20);
		this.guildTaxTextField = new GuiTextField(2, this.fontRenderer, (int) (Double.valueOf(this.width) * 0.75), 80, 40, 20);
		this.guildFundExchangeTextField = new GuiTextField(1, this.fontRenderer, (this.width / 3), 44, this.width/4, 20);
		this.guildNameTextField.setText(guild.guildName);
		this.guildTaxTextField.setText(taxformat.format(guild.guildTax));
		this.guildTaxTextField.setCursorPositionZero();
		this.perm0TF = new GuiTextField(2, this.fontRenderer, this.width - (this.width/4 + 8), this.height/2, this.width/4, 20);
		this.perm1TF = new GuiTextField(2, this.fontRenderer, this.width - (this.width/4 + 8), perm0TF.y + 23, this.width/4, 20);
		this.perm2TF = new GuiTextField(2, this.fontRenderer, this.width - (this.width/4 + 8), perm1TF.y + 23, this.width/4, 20);
		this.perm3TF = new GuiTextField(2, this.fontRenderer, this.width - (this.width/4 + 8), perm2TF.y + 23, this.width/4, 20);
		this.perm0TF.setText(guild.permLevels.get(0));
		this.perm1TF.setText(guild.permLevels.get(1));
		this.perm2TF.setText(guild.permLevels.get(2));
		this.perm3TF.setText(guild.permLevels.get(3));
		this.buttonList.add(new GuiButton(10, (this.width / 2)+3, this.height - 28, 75, 20, I18n.format("gui.cancel")));
		this.buttonList.add(new GuiButton(11, (this.width / 2)- 78, this.height - 28, 75, 20, "Save Changes"));
		this.buttonList.add(new GuiButton(12, 8, this.guildNameTextField.y + this.guildNameTextField.height + 3, (this.width / 4), 20, "Open: "+String.valueOf(guild.openToJoin)));
		this.buttonList.add(new GuiButton(13, (this.width / 3)+ guildFundExchangeTextField.width+ 3, guildFundExchangeTextField.y, 20, 20, "+"));
		this.buttonList.add(new GuiButton(14, (this.width / 3)+ guildFundExchangeTextField.width+ 23, guildFundExchangeTextField.y, 20, 20, "-"));
		this.guicoreChunkList = new GuiListGuildChunks(this, guild.coreLand, chunkValues, true, mc, 8, this.height/2, 130, (this.height/2)-30, 10);
		this.guioutpostChunkList = new GuiListGuildChunks(this, guild.outpostLand, chunkValues, false, mc, guicoreChunkList.x+guicoreChunkList.width + 5, guicoreChunkList.y, 130, (this.height/2)-30, 10);
		//permission based enabling of input fields
		accessUpdate();
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.guicoreChunkList.handleMouseInput();
        this.guioutpostChunkList.handleMouseInput();
    }
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 10) { //Cancel Button
			newDescrims();
			Main.NET.sendToServer(new MessageGuildInfoToServer(guild, discriminators));
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		if (button.id == 11) { //Save Button
			try {this.guild.guildTax = Double.valueOf(this.guildTaxTextField.getText());} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageGuildInfoToServer(guild, discriminators));
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		if (button.id == 12) { //Open to Join Toggle	
			guild.openToJoin = guild.openToJoin ? false : true;
			discriminators.put("open", true);
			button.displayString = guild.openToJoin ? "Open: true" : "Open: false";
		}
		if (button.id == 13) { //Deposit Button
			double amount = -1D;
			try {amount = Math.abs(Double.valueOf(this.guildFundExchangeTextField.getText()));} catch (NumberFormatException e) {}
			if (amount != -1) Main.NET.sendToServer(new MessageAccountInfoToServer(acctGuild, Minecraft.getMinecraft().player.getUniqueID(), amount, true));
		}
		if (button.id == 14) {
			double amount = -1D;
			try {amount = Math.abs(Double.valueOf(this.guildFundExchangeTextField.getText()));} catch (NumberFormatException e) {}
			if (amount != -1) Main.NET.sendToServer(new MessageAccountInfoToServer(acctGuild, Minecraft.getMinecraft().player.getUniqueID(), amount, false));
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.guildNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
        this.guildFundExchangeTextField.mouseClicked(mouseX, mouseY, mouseButton);
        this.guildTaxTextField.mouseClicked(mouseX, mouseY, mouseButton);
        this.perm0TF.mouseClicked(mouseX, mouseY, mouseButton);
        this.perm1TF.mouseClicked(mouseX, mouseY, mouseButton);
        this.perm2TF.mouseClicked(mouseX, mouseY, mouseButton);
        this.perm3TF.mouseClicked(mouseX, mouseY, mouseButton);
        this.guicoreChunkList.mouseClicked(mouseX, mouseY, mouseButton);
        this.guioutpostChunkList.mouseClicked(mouseX, mouseY, mouseButton);
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.guicoreChunkList.mouseReleased(mouseX, mouseY, state);
        this.guioutpostChunkList.mouseReleased(mouseX, mouseY, state);
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
		super.keyTyped(typedChar, keyCode);
        if (this.guildNameTextField.isFocused()) {
        	this.guildNameTextField.textboxKeyTyped(typedChar, keyCode);
        	this.guild.guildName = this.guildNameTextField.getText();
        	discriminators.put("name", true);
        }
        if (this.guildFundExchangeTextField.isFocused() && CoreUtils.validNumberKey(keyCode)) this.guildFundExchangeTextField.textboxKeyTyped(typedChar, keyCode);
        if (this.guildTaxTextField.isFocused() && CoreUtils.validNumberKey(keyCode)) {
        	this.guildTaxTextField.textboxKeyTyped(typedChar, keyCode);
        	discriminators.put("tax", true);
        }
        if (this.perm0TF.isFocused()) {
        	this.perm0TF.textboxKeyTyped(typedChar, keyCode);
        	this.guild.permLevels.put(0, this.perm0TF.getText());
        	discriminators.put("permnames", true);
        }
        if (this.perm1TF.isFocused()) {
        	this.perm1TF.textboxKeyTyped(typedChar, keyCode);
        	this.guild.permLevels.put(1, this.perm1TF.getText());
        	discriminators.put("permnames", true);
        }
        if (this.perm2TF.isFocused()) {
        	this.perm2TF.textboxKeyTyped(typedChar, keyCode);
        	this.guild.permLevels.put(2, this.perm2TF.getText());
        	discriminators.put("permnames", true);
        }
        if (this.perm3TF.isFocused()) {
        	this.perm3TF.textboxKeyTyped(typedChar, keyCode);
        	this.guild.permLevels.put(3, this.perm3TF.getText());
        	discriminators.put("permnames", true);
        }
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.guicoreChunkList.drawScreen(mouseX, mouseY, partialTicks);
        this.guioutpostChunkList.drawScreen(mouseX, mouseY, partialTicks);
        this.guildTaxTextField.drawTextBox();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 8, 16777215);
        this.drawString(this.fontRenderer, "Chunks Claimed:", guicoreChunkList.x, guicoreChunkList.y-24, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.BLUE+"Core:"+TextFormatting.WHITE+String.valueOf(guild.coreLand.size()), guicoreChunkList.x, guicoreChunkList.y - 12, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.RED+" Outpost:"+TextFormatting.WHITE+String.valueOf(guild.outpostLand.size()), guioutpostChunkList.x, guioutpostChunkList.y - 12, 16777215);
        this.drawString(this.fontRenderer, "Guild Name", 8, 20, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.GOLD+(TextFormatting.UNDERLINE+"Guild Account Balance"), (this.width / 3), 20, 16777215);
        this.drawString(this.fontRenderer, "$"+df.format(this.acctGuild.balance), (this.width / 3), 32, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.GOLD+"Account:"+TextFormatting.WHITE+" $"+df.format(balP), guildFundExchangeTextField.x, guildFundExchangeTextField.y+ guildFundExchangeTextField.height+5, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.GREEN+"Guild Value", (int) (Double.valueOf(this.width) * 0.75), 20, 16777215);
        this.drawString(this.fontRenderer, "$"+df.format(worthGuild), (int) (Double.valueOf(this.width) * 0.75), 30, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.GREEN+"Taxable Value", (int) (Double.valueOf(this.width) * 0.75), 45, 16777215);
        this.drawString(this.fontRenderer, "$"+df.format(worthTax), (int) (Double.valueOf(this.width) * 0.75), 55, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.GREEN+"Tax Rate", this.guildTaxTextField.x, this.guildTaxTextField.y-10, 16777215);
        this.drawString(this.fontRenderer, df.format(this.guild.guildTax*100)+"%", this.guildTaxTextField.x+this.guildTaxTextField.width+5, this.guildTaxTextField.y+5, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.DARK_PURPLE+"Rank Names", perm0TF.x, perm0TF.y- 10, 16777215);
        this.drawString(this.fontRenderer, "0", perm0TF.x - 10, perm0TF.y+6, 16777215);
        this.drawString(this.fontRenderer, "1", perm1TF.x - 10, perm1TF.y+6, 16777215);
        this.drawString(this.fontRenderer, "2", perm2TF.x - 10, perm2TF.y+6, 16777215);
        this.drawString(this.fontRenderer, "3", perm3TF.x - 10, perm3TF.y+6, 16777215);
        this.guildNameTextField.drawTextBox();
        this.guildFundExchangeTextField.drawTextBox();
        this.perm0TF.drawTextBox();
        this.perm1TF.drawTextBox();
        this.perm2TF.drawTextBox();
        this.perm3TF.drawTextBox();        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public class GuiListGuildChunks extends GuiNewListExtended{
    	private final GuiGuildManager guildManager;
    	private boolean coreChunk;
    	private List<ChunkPos> pos;
    	private Map<ChunkPos, Double> chunkValues;
        private final List<GuiListGuildChunksEntry> entries = Lists.<GuiListGuildChunksEntry>newArrayList();
        /** Index to the currently selected world */
        private int selectedIdx = -1;
        
        public GuiListGuildChunks(GuiGuildManager gm, List<ChunkPos> pos, Map<ChunkPos, Double> chunkValues, boolean coreChunk, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
    		super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
    		this.guildManager = gm;
    		this.pos = pos;
    		this.coreChunk = coreChunk;
    		this.chunkValues = chunkValues;
    		this.refreshList();
    	}
        
        public void refreshList()
        {
        	entries.clear();
           	for (ChunkPos ck : pos) {
                this.entries.add(new GuiListGuildChunksEntry(this, ck, coreChunk));
            }
        }
        
        public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
        @Nullable
	    public GuiListGuildChunksEntry getSelectedMember(){return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;}
    	@Override
    	public GuiListGuildChunksEntry getListEntry(int index) {return entries.get(index);}
    	@Override
    	protected int getSize() {return entries.size();}
    }
    
    public class GuiListGuildChunksEntry implements GuiNewListExtended.IGuiNewListEntry{
    	private Minecraft client = Minecraft.getMinecraft();
    	private final GuiListGuildChunks containingListSel;
    	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
    	private final ChunkPos pos;
    	private boolean coreChunk;
    	
    	public GuiListGuildChunksEntry(GuiListGuildChunks listSelectionIn, ChunkPos pos, boolean coreChunk) {
    		containingListSel = listSelectionIn;
    		this.pos = pos;
    		this.coreChunk = coreChunk;
    	}
    	
    	@Override
    	public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

    	@Override
    	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
    		double price = containingListSel.chunkValues.getOrDefault(pos, 0D);
    		String value = price >=0 ? TextFormatting.WHITE+"$"+ df.format(price) : TextFormatting.RED+"$"+ df.format(-1*price);
    		if (coreChunk) this.client.fontRenderer.drawString(TextFormatting.BLUE+"("+String.valueOf(pos.x)+","+String.valueOf(pos.z)+") "+value, x+3, y, 16777215);
    		if (!coreChunk) this.client.fontRenderer.drawString(TextFormatting.RED+"("+String.valueOf(pos.x)+","+String.valueOf(pos.z)+") "+value, x+3, y, 16777215);
    	}

    	@Override
    	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
    		this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;}

    	@Override
    	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

    }
}

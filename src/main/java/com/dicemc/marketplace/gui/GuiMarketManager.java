package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.core.Marketplace;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembers;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembersEntry;
import com.dicemc.marketplace.item.ModItems;
import com.dicemc.marketplace.network.MessageMarketsToServer;
import com.dicemc.marketplace.util.MktPktType;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiMarketManager extends GuiScreen{
	private GuiButton localToggle, globalToggle, auctionToggle, serverToggle, buyItem, newSale, playerContent, restockSale;
	private GuiTextField bidBox;
	private static GuiListMarket marketList;
	public static Map<UUID, MarketItem> vendList;
	public static double feeBuy, feeSell;
	private static DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	public static UUID locality;
	public static double balP;
	public static int slotIdx = -1;
	public static String response = "";
	public static String header = "";
	public static int listType;
	private String prompt1 = " | RECEIVE |";
	private String prompt2 = " | GIVE |";
	private String prompt3 = " | SUPPLY REMAINING |";

	public static void syncMarket(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response) {
		GuiMarketManager.vendList = vendList;
		GuiMarketManager.feeBuy = feeBuy;
		GuiMarketManager.feeSell = feeSell;
		GuiMarketManager.listType = listType;
		GuiMarketManager.balP = balP;
		GuiMarketManager.response = listType != 3 ? response: "";
		GuiMarketManager.marketList.vendList = sortedMarketList(listType, vendList, GuiMarketManager.locality);
		GuiMarketManager.marketList.listType = listType;
		GuiMarketManager.marketList.locality = GuiMarketManager.locality;
		if (listType == 3) GuiMarketManager.marketList.locality = UUID.fromString(response);
		GuiMarketManager.marketList.refreshList();
		header = (GuiMarketManager.listType == 3) ? "My Transactions"+ TextFormatting.GREEN+"    [Account: $"+df.format(balP)+"]" :"Sell Fee: "+df.format(feeSell*100)+"%    Buy Fee: "+df.format(feeBuy*100)+"%" + TextFormatting.GREEN+"    [Account: $"+df.format(balP)+"]";
	}
	
	private static List<MarketListItem> sortedMarketList(int listType, Map<UUID, MarketItem> inputList, UUID locIn) {
		List<MarketListItem> outputList = new ArrayList<MarketListItem>();
		switch (listType) {
		case 0: {
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (i.getValue().locality.equals(locIn)) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (!i.getValue().locality.equals(locIn)) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			break;
		}
		case 1: {
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (i.getValue().infinite) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (!i.getValue().infinite) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			break;
		}
		case 2: {
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			break;
		}
		case 3: {
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			break;
		}
		case 4: {
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (!i.getValue().vendorGiveItem) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (i.getValue().vendorGiveItem) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			break;
		}
		default:
		}		
		return outputList;
	}
	
	public GuiMarketManager(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response, UUID locality) {
		this.listType = listType;
		this.vendList = vendList;
		this.locality = locality;
		this.feeBuy = feeBuy;
		this.feeSell = feeSell;
		this.balP = balP;
		header = "Sell Fee: "+df.format(feeSell*100)+"%    Buy Fee: "+df.format(feeBuy*100)+"%" + TextFormatting.GREEN+"    [Account: $"+df.format(balP)+"]";
	}
	
	public void initGui() {
		localToggle = new GuiButton (10, 3, 3, 75, 20, "Local" );
		globalToggle = new GuiButton (11, 3, localToggle.y + localToggle.height +3, 75, 20, "Global" );
		auctionToggle = new GuiButton (12, 3, globalToggle.y + globalToggle.height +3, 75, 20, "Auction" );
		serverToggle = new GuiButton (25, 3, auctionToggle.y+ auctionToggle.height +3, 75, 20, "Server");
		newSale = new GuiButton(13, 3, serverToggle.y+serverToggle.height+20, 75, 20, "New Posting");
		buyItem = new GuiButton(16, 3, newSale.y+newSale.height+3, 75, 20, "Buy Selected");
		bidBox = new GuiTextField(20, this.fontRenderer, 3, buyItem.y+buyItem.height+3, 75, 20);
		restockSale = new GuiButton(17, 3, bidBox.y, 75, 20, "Restock");
		playerContent = new GuiButton(14, 3, this.height - 46, 75, 20, "My Sales");		
		this.buttonList.add(localToggle);
		this.buttonList.add(globalToggle);
		this.buttonList.add(auctionToggle);
		this.buttonList.add(serverToggle);
		this.buttonList.add(newSale);
		this.buttonList.add(buyItem);
		this.buttonList.add(playerContent);
		this.buttonList.add(restockSale);
		this.buttonList.add(new GuiButton(15, 3, this.height - 23, 75, 20, "Back"));
		marketList = new GuiListMarket(this, sortedMarketList(0, vendList, locality), locality, listType, mc, 81, 26, this.width-84, this.height-39, 25);
		//update default load settings.
		localToggle.enabled = false;
		restockSale.visible = false;
		bidBox.setVisible(false);
	}
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        marketList.handleMouseInput();
    }
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        marketList.mouseClicked(mouseX, mouseY, mouseButton);
        bidBox.mouseClicked(mouseX, mouseY, mouseButton);
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        marketList.mouseReleased(mouseX, mouseY, state);
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
		super.keyTyped(typedChar, keyCode);
		if (bidBox.isFocused()&& CoreUtils.validNumberKey(keyCode)) bidBox.textboxKeyTyped(typedChar, keyCode);
    }
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 15) {
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		if (button == localToggle) {
			localToggle.enabled = false;
			globalToggle.enabled = true;
			auctionToggle.enabled = true;
			serverToggle.enabled = true;
			playerContent.enabled = true;
			buyItem.displayString = "Buy Selected";
			restockSale.visible = false;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = " | RECEIVE |";
			prompt2 = " | GIVE |";
			prompt3 = " | SUPPLY REMAINING |";
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.LOCAL, 0, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == globalToggle) {
			localToggle.enabled = true;
			globalToggle.enabled = false;
			auctionToggle.enabled = true;
			serverToggle.enabled = true;
			playerContent.enabled = true;
			buyItem.displayString = "Buy Selected";
			restockSale.visible = false;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = " | RECEIVE |";
			prompt2 = " | GIVE |";
			prompt3 = " | SUPPLY REMAINING |";
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.GLOBAL, 1, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == auctionToggle) {
			localToggle.enabled = true;
			globalToggle.enabled = true;
			auctionToggle.enabled = false;
			serverToggle.enabled = true;
			playerContent.enabled = true;
			buyItem.displayString = "Place Bid";
			restockSale.visible = false;
			bidBox.setVisible(true);
			slotIdx = -1;
			prompt1 = " Select an item to bid for. Enter your bid amount. Press Place Bid.";
			prompt2 = "";
			prompt3 = "";
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.AUCTION, 2, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == serverToggle) {
			localToggle.enabled = true;
			globalToggle.enabled = true;
			auctionToggle.enabled = true;
			serverToggle.enabled = false;
			playerContent.enabled = true;
			buyItem.displayString = "Buy Selected";
			restockSale.visible = false;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = " | RECEIVE |";
			prompt2 = " | GIVE |";
			prompt3 = "";
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.SERVER, 2, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == playerContent) {
			localToggle.enabled = true;
			globalToggle.enabled = true;
			auctionToggle.enabled = true;
			playerContent.enabled = false;
			buyItem.displayString = "Remove";
			restockSale.visible = true;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = " | GIVE |";
			prompt2 = " | RECEIVE |";
			prompt3 = " | SUPPLY REMAINING |";
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.PERSONAL, 3, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == buyItem && marketList.selectedElement != -1) {
			if (this.listType == 0 || this.listType == 1 || this.listType == 4) {
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.BUY, this.listType, marketList.getSelectedMember().posting.key, ItemStack.EMPTY, 0D, true));}
			else if (this.listType == 2 && bidBox.getText().length() > 0) {
				double bidAmount = -1D;
				try {bidAmount = Double.valueOf(this.bidBox.getText());} catch (NumberFormatException e) {}
				if (bidAmount != -1) Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.BUY, this.listType, marketList.getSelectedMember().posting.key, ItemStack.EMPTY, bidAmount, true));}
			else if (this.listType == 3 && marketList.getSelectedMember().posting.item.price == -1) {
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.COLLECT, 0, Reference.NIL, marketList.getSelectedMember().posting.item.item, 0D, true));}
			else if (this.listType == 3 && marketList.getSelectedMember().posting.item.price != -1) {
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.REMOVE, 0, marketList.getSelectedMember().posting.key, ItemStack.EMPTY, 0D, false));}
		}
		if (button == restockSale && marketList.selectedElement != -1 && marketList.getSelectedMember().posting.item.price > -1) {
			if (!marketList.getSelectedMember().posting.item.locality.equals(marketList.locality) || !marketList.getSelectedMember().posting.item.highestBidder.equals(marketList.locality)) {
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.RESTOCK, 0, marketList.getSelectedMember().posting.key, ItemStack.EMPTY, 0D, true));}
			else if (marketList.getSelectedMember().posting.item.locality.equals(marketList.locality) && marketList.getSelectedMember().posting.item.highestBidder.equals(marketList.locality)) {
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.RESTOCK, 0, marketList.getSelectedMember().posting.key, ItemStack.EMPTY, 0D, true));}
		}
		if (button == newSale) {
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.SALE_GUI_LAUNCH, 4, Reference.NIL, ItemStack.EMPTY, 0D, true));
		}
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawString(this.fontRenderer, header, marketList.x, 3, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.GOLD+"Status Message: "+response, marketList.x, this.height-10, 16777215);        
        this.drawString(this.fontRenderer, TextFormatting.DARK_GREEN+prompt1, marketList.x, 16, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.DARK_GREEN+prompt2, marketList.x+100, 16, 16777215);
        this.drawString(this.fontRenderer, TextFormatting.DARK_GREEN+prompt3, marketList.x+200, 16, 16777215);
        marketList.drawScreen(mouseX, mouseY, partialTicks);
        bidBox.drawTextBox();
        if (slotIdx >=0) {
        	if (marketList.getListEntry(slotIdx).posting.item.vendorGiveItem && mouseX > marketList.x && mouseX < marketList.x+20 && mouseY > marketList.getListEntry(slotIdx).getY() && mouseY < marketList.getListEntry(slotIdx).getY()+20) {
	        	RenderHelper.enableGUIStandardItemLighting();
	        	renderToolTip(marketList.getListEntry(slotIdx).posting.item.item, mouseX, mouseY);
	        	RenderHelper.disableStandardItemLighting();
        	}
        	else if (!marketList.getListEntry(slotIdx).posting.item.vendorGiveItem && mouseX > marketList.x+100 && mouseX < marketList.x+120 && mouseY > marketList.getListEntry(slotIdx).getY() && mouseY < marketList.getListEntry(slotIdx).getY()+20) {
	        	RenderHelper.enableGUIStandardItemLighting();
	        	renderToolTip(marketList.getListEntry(slotIdx).posting.item.item, mouseX, mouseY);
	        	RenderHelper.disableStandardItemLighting();
        	}
        	else {slotIdx = -1;} 
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
	
	public class GuiListMarket extends GuiListExtendedMember{
	    private final GuiMarketManager guildManager;
	    public List<MarketListItem> vendList;
	    public UUID locality;
	    private final List<GuiListMarketEntry> entries = Lists.<GuiListMarketEntry>newArrayList();
	    private int selectedIdx = -1;
	    public int hoveredSlot = -1;
	    public int listType = -1;
		
		public GuiListMarket(GuiMarketManager guiGM, List<MarketListItem> vendList, UUID locality, int listType, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.guildManager = guiGM;
			this.vendList = vendList;
			this.locality = locality;
			this.listType = listType;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	        for (MarketListItem entry : vendList) {
	            this.entries.add(new GuiListMarketEntry(this, entry, locality));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListMarketEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListMarketEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListMarketEntry implements GuiListExtendedMember.IGuiNewListEntry{
	    private final GuiListMarket containingListSel;
	    private Minecraft client = Minecraft.getMinecraft();
	    private MarketListItem posting;
	    private String line1, line2, line3, line4, line5;
	    private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	    private int slotY;
	    
	    public int getY() {return slotY;}
		
		public GuiListMarketEntry (GuiListMarket listIn, MarketListItem posting, UUID locality) {
			line5 = "";
			this.containingListSel = listIn;
			this.posting = posting;
			if (containingListSel.listType < 2 || containingListSel.listType == 4) {
				line1 = posting.item.vendorGiveItem? "" : TextFormatting.GOLD+"$"+df.format(posting.item.price);
				line3 = posting.item.vendorGiveItem? TextFormatting.GOLD+"$"+df.format(posting.item.price) : "";	
				line4 = TextFormatting.LIGHT_PURPLE+(posting.item.infinite ? "Server" : String.valueOf(posting.item.vendStock)) + " Remaining";
				if (containingListSel.listType == 0) line4 += posting.item.locality.equals(locality) ? "" : TextFormatting.RED+" [NOT LOCAL SALE]";
			}
			if (containingListSel.listType == 2) {
				line1 = "Current Bid:   "+TextFormatting.GOLD+"$" + df.format(posting.item.price);
				line1 += (posting.item.highestBidder.equals(mc.player.getUniqueID())) ? TextFormatting.RED+" [YOUR BID]" : (posting.item.highestBidder.equals(Reference.NIL)) ? TextFormatting.WHITE+" [No Bids]" : TextFormatting.WHITE+" [Active Auction]";
				line2 = TextFormatting.WHITE+"Auction Ends: ";
				line2 += (posting.item.bidEnd < 3600000) ? TextFormatting.RED+"" : (posting.item.bidEnd < 86400000) ? TextFormatting.YELLOW+"" : TextFormatting.GREEN+"";
				line2 += String.valueOf(new Timestamp(posting.item.bidEnd));
			}
			if (containingListSel.listType == 3) {
				if (posting.item.price == -1) {
					line1 = "Queued Item";
					line2 = TextFormatting.GRAY+"(select remove to place in inventory)";
				}
				else if (!posting.item.locality.equals(locality)) {
					line5 = "Local Listing";
					line3 = posting.item.vendorGiveItem ? TextFormatting.GOLD+"$"+String.valueOf(posting.item.price) : "";
					line1 = posting.item.vendorGiveItem ? "" : TextFormatting.GOLD+"$"+String.valueOf(posting.item.price);
					line4 = TextFormatting.LIGHT_PURPLE+String.valueOf(posting.item.vendStock)+" Remaining";
				}
				else if (posting.item.locality.equals(locality) && posting.item.highestBidder.equals(locality)) {
					line5 = TextFormatting.BLUE+"Global Listing";
					line3 = posting.item.vendorGiveItem ? TextFormatting.GOLD+"$"+String.valueOf(posting.item.price) : "";
					line1 = posting.item.vendorGiveItem ? "" : TextFormatting.GOLD+"$"+String.valueOf(posting.item.price);
					line4 = TextFormatting.LIGHT_PURPLE+String.valueOf(posting.item.vendStock)+" Remaining";
				}
				else if (posting.item.locality.equals(locality) && !posting.item.highestBidder.equals(locality)) {
					line4 = TextFormatting.YELLOW+"Auction Listing";
					line3 = posting.item.highestBidder.equals(Reference.NIL) ? "" : TextFormatting.RED+"[ACTIVE BIDDING]";
					line1 = TextFormatting.GOLD+"$"+String.valueOf(posting.item.price);
				}				
				else {
					line1 = "Uhm... it";
					line2 = "Didn't work";
				}
			}
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(line1, x+30, y+3, 16777215);
	        this.client.fontRenderer.drawString(line2, x+30, y+13 , 16777215);
	        this.client.fontRenderer.drawString(line3, x+130, y+3 , 16777215);
	        this.client.fontRenderer.drawString(line4, x+230, y+3 , 16777215);
	        this.client.fontRenderer.drawString(line5, x+230, y+13, 16777215);
	        RenderHelper.enableGUIStandardItemLighting();
	        FontRenderer font = posting.item.item.getItem().getFontRenderer(posting.item.item);
	        if (font == null) font = fontRenderer;
	        int itemX = posting.item.vendorGiveItem ? x+3: x+103;
	        int costX = !posting.item.vendorGiveItem ? x+3: x+103;
	        itemRender.renderItemAndEffectIntoGUI(posting.item.item, itemX, y);
	        itemRender.renderItemOverlayIntoGUI(font, posting.item.item, itemX, y, null);
	        if (containingListSel.listType != 2 && !line1.equalsIgnoreCase("Queued Item") && !line4.equalsIgnoreCase(TextFormatting.YELLOW+"Auction Listing")) {
		        itemRender.renderItemAndEffectIntoGUI(new ItemStack(ModItems.MONEYBAG), costX, y);
		        itemRender.renderItemOverlayIntoGUI(font, new ItemStack(ModItems.MONEYBAG), costX, y, null);
	        }
	        if (mouseX > itemX && mouseX < itemX+20 && mouseY > y && mouseY < y+20) {GuiMarketManager.slotIdx = slotIndex; slotY = y;}
	        RenderHelper.disableStandardItemLighting();	        
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {		
		}
	}
	public static class MarketListItem {
		UUID key;
		MarketItem item;		
		public MarketListItem(UUID key, MarketItem item) {this.key = key; this.item = item;}
	}
}


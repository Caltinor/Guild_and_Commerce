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

import org.lwjgl.input.Keyboard;

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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiMarketManager extends GuiScreen{
	private GuiButton localToggle, globalToggle, auctionToggle, serverToggle, buyItem, newSale, playerContent, restockSale;
	private GuiButton sortPriceAsc, sortPriceDes, sortNameAsc, sortNameDes, sortMySalesOn, sortMySalesOff;
	private GuiTextField bidBox;
	private GuiListMarket marketList;
	private Map<UUID, MarketItem> vendList;
	private double feeBuy, feeSell;
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private UUID locality;
	private double balP;
	private int slotIdx = -1;
	private String response = "";
	private String header = "";
	private int listType;
	private int sortType;
	private String prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
	private String prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
	private String prompt3 = new TextComponentTranslation("gui.market.prompt3").getFormattedText();
	
	public void setSlotIdx(int idx) {slotIdx = idx;}

	public void syncMarket(int listType, Map<UUID, MarketItem> vendList, double feeBuy, double feeSell, double balP, String response) {
		this.vendList = vendList;
		this.feeBuy = feeBuy;
		this.feeSell = feeSell;
		this.listType = listType;
		this.balP = balP;
		this.response = listType != 3 ? response: "";
		this.marketList.vendList = sortedMarketList(listType, vendList, this.locality, 0, this.sortMySalesOff.enabled);
		this.marketList.listType = listType;
		this.marketList.locality = this.locality;
		if (listType == 3) this.marketList.locality = UUID.fromString(response);
		this.marketList.refreshList();
		TextComponentTranslation tctAcct = new TextComponentTranslation("gui.market.headeraccount", df.format(balP));
		tctAcct.setStyle(new Style().setColor(TextFormatting.GREEN));
		header = (this.listType == 3) ? new TextComponentTranslation("gui.market.header2", tctAcct).getFormattedText() : new TextComponentTranslation("gui.market.header1", df.format(feeSell*100), df.format(feeBuy*100), tctAcct).getFormattedText();
	}
	
	public static List<MarketListItem> sortedMarketList(int listType, Map<UUID, MarketItem> inputList, UUID locIn, int sortType, boolean includeOwnSales) {
		List<MarketListItem> outputList = new ArrayList<MarketListItem>();
		switch (listType) {
		case 0: {
			List<MarketListItem> holderList = new ArrayList<MarketListItem>();
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (i.getValue().locality.equals(locIn)) { 
					if (includeOwnSales) holderList.add(new MarketListItem(i.getKey(), i.getValue()));
					else if (!includeOwnSales && !i.getValue().vendor.equals(Minecraft.getMinecraft().player.getUniqueID()))  holderList.add(new MarketListItem(i.getKey(), i.getValue()));
				}
			}
			switch (sortType) {
			case 1: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				break;
			}
			case 2: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				Collections.reverse(holderList);
				break;
			}
			case 3: {
				Collections.sort(holderList);
				break;
			}
			case 4: {
				Collections.sort(holderList);
				Collections.reverse(holderList);
				break;
			}
			default:
			}
			outputList = holderList;
			holderList = new ArrayList<MarketListItem>();
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (!i.getValue().locality.equals(locIn)) {
					if (includeOwnSales) holderList.add(new MarketListItem(i.getKey(), i.getValue()));
					else if (!includeOwnSales && !i.getValue().vendor.equals(Minecraft.getMinecraft().player.getUniqueID()))  holderList.add(new MarketListItem(i.getKey(), i.getValue()));
				}
			}
			switch (sortType) {
			case 1: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				break;
			}
			case 2: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				Collections.reverse(holderList);
				break;
			}
			case 3: {
				Collections.sort(holderList);
				break;
			}
			case 4: {
				Collections.sort(holderList);
				Collections.reverse(holderList);
				break;
			}
			default:
			}
			outputList.addAll(holderList);
			break;
		}
		case 1: {
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) { 
				if (includeOwnSales) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
				else if (!includeOwnSales && !i.getValue().vendor.equals(Minecraft.getMinecraft().player.getUniqueID()))  outputList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			switch (sortType) {
			case 1: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(outputList, IPC);
				break;
			}
			case 2: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(outputList, IPC);
				Collections.reverse(outputList);
				break;
			}
			case 3: {
				Collections.sort(outputList);
				break;
			}
			case 4: {
				Collections.sort(outputList);
				Collections.reverse(outputList);
				break;
			}
			default:
			}
			break;
		}
		case 2: {
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (includeOwnSales) outputList.add(new MarketListItem(i.getKey(), i.getValue()));
				else if (!includeOwnSales && !i.getValue().vendor.equals(Minecraft.getMinecraft().player.getUniqueID()))  outputList.add(new MarketListItem(i.getKey(), i.getValue()));
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
			List<MarketListItem> holderList = new ArrayList<MarketListItem>();
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (!i.getValue().vendorGiveItem) holderList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			switch (sortType) {
			case 1: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				break;
			}
			case 2: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				Collections.reverse(holderList);
				break;
			}
			case 3: {
				Collections.sort(holderList);
				break;
			}
			case 4: {
				Collections.sort(holderList);
				Collections.reverse(holderList);
				break;
			}
			default:
			}
			outputList = holderList;
			holderList = new ArrayList<MarketListItem>();
			for (Map.Entry<UUID, MarketItem> i : inputList.entrySet()) {
				if (i.getValue().vendorGiveItem) holderList.add(new MarketListItem(i.getKey(), i.getValue()));
			}
			switch (sortType) {
			case 1: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				break;
			}
			case 2: {
				ItemPriceCompare IPC = new ItemPriceCompare();
				Collections.sort(holderList, IPC);
				Collections.reverse(holderList);
				break;
			}
			case 3: {
				Collections.sort(holderList);
				break;
			}
			case 4: {
				Collections.sort(holderList);
				Collections.reverse(holderList);
				break;
			}
			default:
			}
			outputList.addAll(holderList);
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
		TextComponentTranslation tctAcct = new TextComponentTranslation("gui.market.headeraccount", df.format(balP));
		tctAcct.setStyle(new Style().setColor(TextFormatting.GREEN));
		header = new TextComponentTranslation("gui.market.header1", df.format(feeSell*100), df.format(feeBuy*100), tctAcct).getFormattedText();
	}
	
	public void initGui() {
		localToggle = new GuiButton (10, 3, 3, 75, 20, new TextComponentTranslation("gui.market.local").getFormattedText());
		globalToggle = new GuiButton (11, 3, localToggle.y + localToggle.height +3, 75, 20, new TextComponentTranslation("gui.market.global").getFormattedText());
		auctionToggle = new GuiButton (12, 3, globalToggle.y + globalToggle.height +3, 75, 20, new TextComponentTranslation("gui.market.auction").getFormattedText());
		serverToggle = new GuiButton (25, 3, auctionToggle.y+ auctionToggle.height +3, 75, 20, new TextComponentTranslation("gui.market.server").getFormattedText());
		newSale = new GuiButton(13, 3, serverToggle.y+serverToggle.height+20, 75, 20, new TextComponentTranslation("gui.market.newposting").getFormattedText());
		buyItem = new GuiButton(16, 3, newSale.y+newSale.height+3, 75, 20, new TextComponentTranslation("gui.market.buyselected").getFormattedText());
		bidBox = new GuiTextField(20, this.fontRenderer, 3, buyItem.y+buyItem.height+3, 75, 20);
		restockSale = new GuiButton(17, 3, bidBox.y, 75, 20, new TextComponentTranslation("gui.market.restock").getFormattedText());
		playerContent = new GuiButton(14, 3, this.height - 46, 75, 20, new TextComponentTranslation("gui.market.mysales").getFormattedText());		
		this.buttonList.add(localToggle);
		this.buttonList.add(globalToggle);
		this.buttonList.add(auctionToggle);
		this.buttonList.add(serverToggle);
		this.buttonList.add(newSale);
		this.buttonList.add(buyItem);
		this.buttonList.add(playerContent);
		this.buttonList.add(restockSale);
		this.buttonList.add(new GuiButton(15, 3, this.height - 23, 75, 20, new TextComponentTranslation("gui.back").getFormattedText()));
		marketList = new GuiListMarket(this, sortedMarketList(0, vendList, locality, 0, true), locality, listType, mc, 81, 26, this.width-114, this.height-39, 25);
		sortPriceAsc = new GuiButton(50, this.width-25, marketList.y, 20, 20, "$>");
		sortPriceDes = new GuiButton(51, this.width-25, marketList.y+ 25, 20, 20, "$<");
		sortNameAsc = new GuiButton(52, this.width-25, sortPriceDes.y + 25, 20, 20, "AZ");
		sortNameDes = new GuiButton(53, this.width-25, sortNameAsc.y +25, 20, 20, "ZA");
		sortMySalesOn = new GuiButton(54, this.width-25, sortNameDes.y +25, 20, 20, "+Me");
		sortMySalesOff = new GuiButton(55, this.width-25, sortMySalesOn.y+25, 20, 20, "-Me");
		this.buttonList.add(sortPriceAsc);
		this.buttonList.add(sortPriceDes);
		this.buttonList.add(sortNameAsc);
		this.buttonList.add(sortNameDes);
		this.buttonList.add(sortMySalesOn);
		this.buttonList.add(sortMySalesOff);
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
		if (!bidBox.isFocused()) {
			if (keyCode == Keyboard.KEY_S) {
				localToggle.enabled = true;
				globalToggle.enabled = true;
				auctionToggle.enabled = true;
				serverToggle.enabled = false;
				playerContent.enabled = true;
				buyItem.displayString = new TextComponentTranslation("gui.market.buyselected").getFormattedText();
				restockSale.visible = false;
				bidBox.setVisible(false);
				slotIdx = -1;
				prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
				prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
				prompt3 = "";
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.SERVER, 4, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
			}
			if (keyCode == Keyboard.KEY_G) {
				localToggle.enabled = true;
				globalToggle.enabled = false;
				auctionToggle.enabled = true;
				serverToggle.enabled = true;
				playerContent.enabled = true;
				buyItem.displayString = new TextComponentTranslation("gui.market.buyselected").getFormattedText();
				restockSale.visible = false;
				bidBox.setVisible(false);
				slotIdx = -1;
				prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
				prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
				prompt3 = new TextComponentTranslation("gui.market.prompt3").getFormattedText();
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.GLOBAL, 1, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
			}
			if (keyCode == Keyboard.KEY_A) {
				localToggle.enabled = true;
				globalToggle.enabled = true;
				auctionToggle.enabled = false;
				serverToggle.enabled = true;
				playerContent.enabled = true;
				buyItem.displayString = new TextComponentTranslation("gui.market.placebid").getFormattedText();
				restockSale.visible = false;
				bidBox.setVisible(true);
				slotIdx = -1;
				prompt1 = new TextComponentTranslation("gui.market.prompt4").getFormattedText();
				prompt2 = "";
				prompt3 = "";
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.AUCTION, 2, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
			}
			if (keyCode == Keyboard.KEY_F) {
				localToggle.enabled = false;
				globalToggle.enabled = true;
				auctionToggle.enabled = true;
				serverToggle.enabled = true;
				playerContent.enabled = true;
				buyItem.displayString = new TextComponentTranslation("gui.market.buyselected").getFormattedText();
				restockSale.visible = false;
				bidBox.setVisible(false);
				slotIdx = -1;
				prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
				prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
				prompt3 = new TextComponentTranslation("gui.market.prompt3").getFormattedText();
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.LOCAL, 0, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
			}
			if (keyCode == Keyboard.KEY_D) {
				localToggle.enabled = true;
				globalToggle.enabled = true;
				auctionToggle.enabled = true;
				playerContent.enabled = false;
				buyItem.displayString = new TextComponentTranslation("gui.market.remove").getFormattedText();
				restockSale.visible = true;
				bidBox.setVisible(false);
				slotIdx = -1;
				prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
				prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
				prompt3 = new TextComponentTranslation("gui.market.prompt3").getFormattedText();
				Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.PERSONAL, 3, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
			}
			if (keyCode == Keyboard.KEY_B && marketList.selectedElement != -1) {
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
		}
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
			buyItem.displayString = new TextComponentTranslation("gui.market.buyselected").getFormattedText();
			restockSale.visible = false;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
			prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
			prompt3 = new TextComponentTranslation("gui.market.prompt3").getFormattedText();
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.LOCAL, 0, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == globalToggle) {
			localToggle.enabled = true;
			globalToggle.enabled = false;
			auctionToggle.enabled = true;
			serverToggle.enabled = true;
			playerContent.enabled = true;
			buyItem.displayString = new TextComponentTranslation("gui.market.buyselected").getFormattedText();
			restockSale.visible = false;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
			prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
			prompt3 = new TextComponentTranslation("gui.market.prompt3").getFormattedText();
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.GLOBAL, 1, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == auctionToggle) {
			localToggle.enabled = true;
			globalToggle.enabled = true;
			auctionToggle.enabled = false;
			serverToggle.enabled = true;
			playerContent.enabled = true;
			buyItem.displayString = new TextComponentTranslation("gui.market.placebid").getFormattedText();
			restockSale.visible = false;
			bidBox.setVisible(true);
			slotIdx = -1;
			prompt1 = new TextComponentTranslation("gui.market.prompt4").getFormattedText();
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
			buyItem.displayString = new TextComponentTranslation("gui.market.buyselected").getFormattedText();
			restockSale.visible = false;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
			prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
			prompt3 = "";
			Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.SERVER, 4, mc.player.getUniqueID(), ItemStack.EMPTY, 0D, true));
		}
		if (button == playerContent) {
			localToggle.enabled = true;
			globalToggle.enabled = true;
			auctionToggle.enabled = true;
			playerContent.enabled = false;
			buyItem.displayString = new TextComponentTranslation("gui.market.remove").getFormattedText();
			restockSale.visible = true;
			bidBox.setVisible(false);
			slotIdx = -1;
			prompt1 = new TextComponentTranslation("gui.market.prompt1").getFormattedText();
			prompt2 = new TextComponentTranslation("gui.market.prompt2").getFormattedText();
			prompt3 = new TextComponentTranslation("gui.market.prompt3").getFormattedText();
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
		if (button == sortPriceAsc) {
			sortPriceAsc.enabled = false;
			sortPriceDes.enabled = true;
			sortNameAsc.enabled = true;
			sortNameDes.enabled = true;
			sortType = 1;
			marketList.vendList = sortedMarketList(listType, vendList, locality, sortType, sortMySalesOn.enabled);
			marketList.refreshList();
		}
		if (button == sortPriceDes) {
			sortPriceAsc.enabled = true;
			sortPriceDes.enabled = false;
			sortNameAsc.enabled = true;
			sortNameDes.enabled = true;
			sortType = 2;
			marketList.vendList = sortedMarketList(listType, vendList, locality, sortType, sortMySalesOn.enabled);
			marketList.refreshList();
		}
		if (button == sortNameAsc) {
			sortPriceAsc.enabled = true;
			sortPriceDes.enabled = true;
			sortNameAsc.enabled = false;
			sortNameDes.enabled = true;
			sortType = 3;
			marketList.vendList = sortedMarketList(listType, vendList, locality, sortType, sortMySalesOn.enabled);
			marketList.refreshList();
		}
		if (button == sortNameDes) {
			sortPriceAsc.enabled = true;
			sortPriceDes.enabled = true;
			sortNameAsc.enabled = true;
			sortNameDes.enabled = false;
			sortType = 4;
			marketList.vendList = sortedMarketList(listType, vendList, locality, sortType, sortMySalesOn.enabled);
			marketList.refreshList();
		}
		if (button == sortMySalesOn) {
			sortMySalesOn.enabled = false;
			sortMySalesOff.enabled = true; 
			marketList.vendList = sortedMarketList(listType, vendList, locality, sortType, true);
			marketList.refreshList();
		}
		if (button == sortMySalesOff) {
			sortMySalesOn.enabled = true;
			sortMySalesOff.enabled = false;
			marketList.vendList = sortedMarketList(listType, vendList, locality, sortType, false);
			marketList.refreshList();
		}
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawString(this.fontRenderer, header, marketList.x, 3, 16777215);
        this.drawString(this.fontRenderer, new TextComponentTranslation("gui.market.status",response).setStyle(new Style().setColor(TextFormatting.GOLD)).getFormattedText(), marketList.x, this.height-10, 16777215);        
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
	
	public class GuiListMarket extends GuiNewListExtended{
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
	
	public class GuiListMarketEntry implements GuiNewListExtended.IGuiNewListEntry{
	    private final GuiListMarket containingListSel;
	    private Minecraft client = Minecraft.getMinecraft();
	    MarketListItem posting;
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
				line4 = TextFormatting.LIGHT_PURPLE+(posting.item.infinite ? new TextComponentTranslation("gui.market.stock.unlimited").getFormattedText() : new TextComponentTranslation("gui.market.stock.remaining", String.valueOf(posting.item.vendStock)).getFormattedText());
				if (containingListSel.listType == 0) line5 += posting.item.locality.equals(locality) ? "" : TextFormatting.RED+ new TextComponentTranslation("gui.market.notlocal").getFormattedText();
			}
			if (containingListSel.listType == 2) {
				line1 = new TextComponentTranslation("gui.market.currentbid", TextFormatting.GOLD+"$" + df.format(posting.item.price)).getFormattedText();
				line1 += (posting.item.highestBidder.equals(mc.player.getUniqueID())) ? new TextComponentTranslation("gui.market.yourbid").setStyle(new Style().setColor(TextFormatting.RED)).getFormattedText()  
						: (posting.item.highestBidder.equals(Reference.NIL)) ? new TextComponentTranslation("gui.market.nobids").setStyle(new Style().setColor(TextFormatting.WHITE)).getFormattedText() 
								: new TextComponentTranslation("gui.market.activeauction").setStyle(new Style().setColor(TextFormatting.WHITE)).getFormattedText();
				line2 = new TextComponentTranslation("gui.market.auctionends").setStyle(new Style().setColor(TextFormatting.WHITE)).getFormattedText();
				line2 += (posting.item.bidEnd < 3600000) ? TextFormatting.RED+"" : (posting.item.bidEnd < 86400000) ? TextFormatting.YELLOW+"" : TextFormatting.GREEN+"";
				line2 += String.valueOf(new Timestamp(posting.item.bidEnd));
			}
			if (containingListSel.listType == 3) {
				if (posting.item.price == -1) {
					line1 = new TextComponentTranslation("gui.market.queueditem").getFormattedText();
					line2 = new TextComponentTranslation("gui.market.queuetakeprompt").setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText();
				}
				else if (!posting.item.locality.equals(locality)) {
					line5 = new TextComponentTranslation("gui.market.listinglocal").getFormattedText();
					line3 = posting.item.vendorGiveItem ? TextFormatting.GOLD+"$"+String.valueOf(posting.item.price) : "";
					line1 = posting.item.vendorGiveItem ? "" : TextFormatting.GOLD+"$"+String.valueOf(posting.item.price);
					line4 = new TextComponentTranslation("gui.market.stock.remaining", String.valueOf(posting.item.vendStock)).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)).getFormattedText();
				}
				else if (posting.item.locality.equals(locality) && posting.item.highestBidder.equals(locality)) {
					line5 = new TextComponentTranslation("gui.market.listingglobal").setStyle(new Style().setColor(TextFormatting.BLUE)).getFormattedText();
					line3 = posting.item.vendorGiveItem ? TextFormatting.GOLD+"$"+String.valueOf(posting.item.price) : "";
					line1 = posting.item.vendorGiveItem ? "" : TextFormatting.GOLD+"$"+String.valueOf(posting.item.price);
					line4 = new TextComponentTranslation("gui.market.stock.remaining", String.valueOf(posting.item.vendStock)).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)).getFormattedText();
				}
				else if (posting.item.locality.equals(locality) && !posting.item.highestBidder.equals(locality)) {
					line4 = new TextComponentTranslation("gui.market.listingauction").setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText();
					line3 = posting.item.highestBidder.equals(Reference.NIL) ? "" : new TextComponentTranslation("gui.market.activebidding").setStyle(new Style().setColor(TextFormatting.RED)).getFormattedText();
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
	        this.client.fontRenderer.drawString(line4, x+200, y+3 , 16777215);
	        this.client.fontRenderer.drawString(line5, x+200, y+13, 16777215);
	        RenderHelper.enableGUIStandardItemLighting();
	        FontRenderer font = posting.item.item.getItem().getFontRenderer(posting.item.item);
	        if (font == null) font = fontRenderer;
	        int itemX = posting.item.vendorGiveItem ? x+3: x+103;
	        int costX = !posting.item.vendorGiveItem ? x+3: x+103;
	        itemRender.renderItemAndEffectIntoGUI(posting.item.item, itemX, y);
	        itemRender.renderItemOverlayIntoGUI(font, posting.item.item, itemX, y, null);
	        if (containingListSel.listType != 2 && !line1.equalsIgnoreCase(new TextComponentTranslation("gui.market.queueditem").getFormattedText()) && !line4.equalsIgnoreCase(new TextComponentTranslation("gui.market.listingAuction").setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText())) {
		        itemRender.renderItemAndEffectIntoGUI(new ItemStack(ModItems.MONEYBAG), costX, y);
		        itemRender.renderItemOverlayIntoGUI(font, new ItemStack(ModItems.MONEYBAG), costX, y, null);
	        }
	        if (mouseX > itemX && mouseX < itemX+20 && mouseY > y && mouseY < y+20) {this.containingListSel.guildManager.setSlotIdx(slotIndex); slotY = y;}
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
	
	public static class MarketListItem implements Comparable<MarketListItem>{
		UUID key;
		MarketItem item;		
		public MarketListItem(UUID key, MarketItem item) {this.key = key; this.item = item;}
		
		@Override
		public int compareTo(MarketListItem o) {
			return this.item.item.getDisplayName().compareTo(o.item.item.getDisplayName());
		}
	}
	
	public static class ItemPriceCompare implements Comparator<MarketListItem> {
		@Override
		public int compare(MarketListItem o1, MarketListItem o2) {
			if (o1.item.price < o2.item.price) return -1;
			if (o1.item.price > o2.item.price) return 1;
			return 0;
		}	
	}
}


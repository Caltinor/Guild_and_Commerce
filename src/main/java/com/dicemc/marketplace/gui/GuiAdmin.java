package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiGuildManager.GuiListGuildChunks;
import com.dicemc.marketplace.gui.GuiGuildManager.GuiListGuildChunksEntry;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembers;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembersEntry;
import com.dicemc.marketplace.gui.GuiMarketManager.GuiListMarket;
import com.dicemc.marketplace.gui.GuiMarketManager.GuiListMarketEntry;
import com.dicemc.marketplace.gui.GuiMarketManager.MarketListItem;
import com.dicemc.marketplace.item.ModItems;
import com.dicemc.marketplace.network.MessageAdminToServer;
import com.dicemc.marketplace.network.MessageMemberInfoToServer;
import com.dicemc.marketplace.util.AdminGuiType;
import com.dicemc.marketplace.util.MktPktType;
import com.dicemc.marketplace.util.Reference;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;

public class GuiAdmin extends GuiScreen{
	//Gui Structural variables
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private DecimalFormat taxformat = new DecimalFormat("0.00000");
	private AdminGuiType activeMenu = AdminGuiType.NONE;
	private GuiButton toggleAccount, toggleGuild, toggleMarket, exitButton;
	//Account Menu Objects;
	private GuiButton toggleAccountGuild, toggleAccountPlayer, accountSet, accountAdd, accountRemove;
	private GuiTextField balanceBox;
	private GuiListAccount guiListAccounts;
	private boolean isPlayerList = true;
	//guild select menu objects;
	private GuiButton selectGuild, deleteGuild;
	private GuiListNameList guildList;
	//guild main menu objects
	private GuiButton setOpen, set0, set1, set2, set3, openLand, openMembers, saveGuild;
	private GuiTextField nameBox, taxBox, perm0, perm1, perm2, perm3;
	private GuiListAdminPerms listGuildPerms;
	//guild land menu objects
	private GuiListAdminGuildChunks guicoreChunkList, guioutpostChunkList;
	private GuiButton landPublic, landForSale, landOutpost, landSave;
	private GuiTextField landValue;
	//guild member menu objects
	private GuiListAdminGuildMembers listGuildMembers;
	private GuiTextField mbrInviteBox;
	private GuiButton mbrAdd, mbrSub, mbr0, mbr1, mbr2, mbr3;
	//market menu objects
	private GuiButton toggleLocal, toggleGlobal, toggleAuction, toggleServer;
	private GuiButton addSale, editSave, toggleVendorGive, toggleInfinite, saleExpire, saleRemove;
	private GuiTextField priceBox, stockBox;
	private GuiListAdminMarket marketList;
	private boolean isVendorGive = true;
	private boolean isInfinite = false;
	private int selectedMarket = -1;
	//Gui Data variables
	private int slotIdx = -1;
	private Guild guiGuild = new Guild(Reference.NIL);
	private Map<UUID, String> mbrNames = new HashMap<UUID, String>();
	private Map<Account, String> accountList = new HashMap<Account, String>();
	private Map<UUID, String> nameList = new HashMap<UUID, String>();
	private List<MarketListItem> vendList = new ArrayList<MarketListItem>();
	private List<ChunkPos> pos = new ArrayList<ChunkPos>();
	private Map<ChunkPos, Double> chunkValues = new HashMap<ChunkPos, Double>();
	private String vendorName, locName, bidderName;
	private double value = 0D;
	private boolean isPublic = false;
	private boolean isForSale = false;
	private boolean isOutpost = false;
	
	
	public void syncAccounts(Map<Account, String> accountList) {this.accountList = accountList; guiListAccounts.refreshList();}
	
	public void syncGuildList(Map<UUID, String> nameList) {this.nameList = nameList; guildList.refreshList();}
	
	public void syncGuildData(String name, boolean open, double tax, String perm0, String perm1, String perm2, String perm3, Map<String, Integer> guildPerms) {
		guiGuild.guildName = name;
		guiGuild.openToJoin = open;
		guiGuild.guildTax = tax;
		guiGuild.permLevels.put(0, perm0);
		guiGuild.permLevels.put(1, perm1);
		guiGuild.permLevels.put(2, perm2);
		guiGuild.permLevels.put(3, perm3);
		guiGuild.permissions = guildPerms;
		updateVisibility();
		listGuildPerms.refreshList();
	}
	
	public void syncGuildLand(List<ChunkPos> posCore, List<ChunkPos> posOutpost, Map<ChunkPos, Double> chunkValues) {
		guicoreChunkList.pos = posCore;
		guicoreChunkList.chunkValues = chunkValues;
		guicoreChunkList.refreshList();
		guioutpostChunkList.pos = posOutpost;
		guioutpostChunkList.chunkValues = chunkValues;
		guioutpostChunkList.refreshList();
		updateVisibility();
	}
	
	public void syncGuildLandDetail(double value, boolean isPublic, boolean isForSale, boolean isOutpost) {
		this.value = value;
		this.isPublic = isPublic;
		this.isForSale = isForSale;
		this.isOutpost = isOutpost;
		updateVisibility();
	}
	
	public void syncGuildMembers(Map<UUID, Integer> members, Map<UUID,String> mbrNames) {
		guiGuild.members = members;
		this.mbrNames = mbrNames;
		listGuildMembers.refreshList();
		updateVisibility();
	}
	
	public void syncMarkets(List<MarketListItem> list) {this.vendList = list; marketList.listType = selectedMarket; marketList.refreshList();}
	
	public void syncMarketDetail(String vendorName, String locName, String bidderName) {
		this.vendorName = vendorName;
		this.locName = locName;
		this.bidderName = bidderName;
		this.updateVisibility();
	}
	
	public GuiAdmin() {
		activeMenu = AdminGuiType.MARKET;
		selectedMarket = -1;
	}
	
	public void initGui() {
		toggleAccount = new GuiButton(1, 3, 3, 75, 20, "Accounts");
		toggleGuild = new GuiButton(2, 3, toggleAccount.y + 23, 75, 20, "Guild");
		toggleMarket = new GuiButton(3, 3, toggleGuild.y + 23, 75, 20, "Markets");
		exitButton = new GuiButton(4, 3, this.height - 30, 75, 20, "Exit");
		this.buttonList.add(toggleAccount);
		this.buttonList.add(toggleGuild);
		this.buttonList.add(toggleMarket);
		this.buttonList.add(exitButton);
		//account menu specific objects
		guiListAccounts = new GuiListAccount(this, mc, accountList, 83, 30, (this.width - 86)/2, this.height - 33, 10);
		toggleAccountPlayer = new GuiButton(10, guiListAccounts.x + guiListAccounts.width + 3, 3, guiListAccounts.width/2 - 2, 20, "Players");
		toggleAccountGuild = new GuiButton(11, toggleAccountPlayer.x + toggleAccountPlayer.width + 3, 3, guiListAccounts.width/2 - 2, 20, "Guilds");
		balanceBox = new GuiTextField(12, this.fontRenderer, guiListAccounts.x + guiListAccounts.width + 3, this.height/2, guiListAccounts.width - 6, 20);
		accountSet = new GuiButton(13, balanceBox.x, balanceBox.y+balanceBox.height+3, toggleAccountGuild.width, 20, "Set");
		accountAdd = new GuiButton(14, accountSet.x+ accountSet.width +3, balanceBox.y+balanceBox.height+3, toggleAccountGuild.width, 20, "Add");
		accountRemove = new GuiButton(15, this.width - (this.width/5) - 38, this.height - 30, 120, 20, "Remove Account");
		this.buttonList.add(toggleAccountPlayer);
		this.buttonList.add(toggleAccountGuild);
		this.buttonList.add(accountSet);
		this.buttonList.add(accountAdd);
		this.buttonList.add(accountRemove);
		guiListAccounts.visible = false;
		toggleAccountPlayer.visible = false;
		toggleAccountGuild.visible = false;
		balanceBox.setVisible(false);
		accountSet.visible = false;
		accountAdd.visible = false;
		accountRemove.visible = false;
		//Guild Select menu specific objects
		selectGuild = new GuiButton(20, (this.width - 80)/2 + 42, this.height - 30, 75, 20, "Select Guild");
		deleteGuild = new GuiButton(21, this.width - 80, this.height-30, 75, 20, "DELETE GUILD");
		guildList = new GuiListNameList(this, mc, nameList, 83, 30, this.width - 90, this.height - 65, 12);
		this.buttonList.add(selectGuild);
		this.buttonList.add(deleteGuild);
		selectGuild.visible = false;
		deleteGuild.visible = false;
		guildList.visible = false;
		//Guild Main menu specific objects
		nameBox = new GuiTextField(37, this.fontRenderer, 85, 20, 150, 20);
		setOpen = new GuiButton(30, nameBox.x+ nameBox.width + 3, 20, 75, 20, "");
		taxBox = new GuiTextField(38, this.fontRenderer, setOpen.x, setOpen.y+ setOpen.height + 15, 60, 20);
		set0 = new GuiButton(31, setOpen.x, taxBox.y + taxBox.height + 5, 20, 20, "0");
		set1 = new GuiButton(32, setOpen.x+20, set0.y, 20, 20, "1");
		set2 = new GuiButton(33, setOpen.x+40, set0.y, 20, 20, "2");
		set3 = new GuiButton(34, setOpen.x+60, set0.y, 20, 20, "3");
		openLand = new GuiButton(35, this.width - 80, 5, 75, 20, "Land Menu");
		openMembers = new GuiButton(36,openLand.x, 30, 75, 20, "Member Menu");
		this.buttonList.add(setOpen);
		this.buttonList.add(set0);
		this.buttonList.add(set1);
		this.buttonList.add(set2);
		this.buttonList.add(set3);
		this.buttonList.add(openLand);
		this.buttonList.add(openMembers);			
		perm0 = new GuiTextField(301, this.fontRenderer, nameBox.x, 70, this.width/4, 20);
		perm1 = new GuiTextField(302, this.fontRenderer, nameBox.x, perm0.y + perm0.height + 5, perm0.width, 20);
		perm2 = new GuiTextField(303, this.fontRenderer, nameBox.x, perm1.y + perm1.height + 5, perm1.width, 20);
		perm3 = new GuiTextField(304, this.fontRenderer, nameBox.x, perm2.y + perm2.height + 5, perm2.width, 20);
		saveGuild = new GuiButton(37, perm3.x, perm3.y+perm3.height+5, perm3.width, 20, "Save Changes");
		this.buttonList.add(saveGuild);
		listGuildPerms = new GuiListAdminPerms(this, mc, perm3.x+perm3.width+5, set0.y+set0.height+3, this.width - (perm3.x+perm3.width+8), this.height-(set0.y+set0.height+3)- 3, 12);
		setOpen.visible = false;
		set0.visible = false;
		set1.visible = false;
		set2.visible = false;
		set3.visible = false;
		openLand.visible = false;
		openMembers.visible = false;
		nameBox.setVisible(false);
		taxBox.setVisible(false);
		perm0.setVisible(false);
		perm1.setVisible(false);
		perm2.setVisible(false);
		perm3.setVisible(false);
		saveGuild.visible = false;
		listGuildPerms.visible = false;
		//guild Land menu specific objects
		guicoreChunkList = new GuiListAdminGuildChunks(this, pos, chunkValues, true, mc, 83, 15, 130, (this.height-50)/2, 10);
		guioutpostChunkList = new GuiListAdminGuildChunks(this, pos, chunkValues, false, mc, 83, guicoreChunkList.y+guicoreChunkList.height+30, 130, guicoreChunkList.height, 10);
		landValue = new GuiTextField(40, this.fontRenderer, guicoreChunkList.x+guicoreChunkList.width + 30, this.height/4, 100, 20);
		landPublic = new GuiButton(41, landValue.x, landValue.y+landValue.height+5, 100, 20, "");
		landForSale = new GuiButton(42, landValue.x, landPublic.y+landPublic.height+5, 100, 20, "");
		landOutpost = new GuiButton(43, landValue.x, landForSale.y+landForSale.height+5, 100, 20, "");
		landSave = new GuiButton(44, landOutpost.x+12, landOutpost.y+landOutpost.height+5, 75, 20, "Save Changes");
		this.buttonList.add(landPublic);
		this.buttonList.add(landForSale);
		this.buttonList.add(landOutpost);
		this.buttonList.add(landSave);
		guicoreChunkList.visible = false;
		guioutpostChunkList.visible =false;
		landValue.setVisible(false);
		landPublic.visible = false;
		landForSale.visible = false;
		landOutpost.visible = false;
		landSave.visible = false;
		//guild members menu specific objects
		listGuildMembers = new GuiListAdminGuildMembers(this, mc, 83, 15, 130, this.height-20, 10);
		mbrInviteBox = new GuiTextField(50, this.fontRenderer, listGuildMembers.x+listGuildMembers.width+20, listGuildMembers.y, 100, 20);
		mbrAdd = new GuiButton(51, mbrInviteBox.x, mbrInviteBox.y+ mbrInviteBox.height +5, 75, 20, "Add Invite");
		mbrSub = new GuiButton(52, mbrAdd.x, mbrAdd.y+mbrAdd.height+5, 75, 20, "Remove");
		mbr0 = new GuiButton(53, mbrAdd.x, mbrSub.y+mbrSub.height+5, 20, 20, "0");
		mbr1 = new GuiButton(54, mbrAdd.x, mbr0.y+mbr0.height, 20, 20, "1");
		mbr2 = new GuiButton(55, mbrAdd.x, mbr1.y+mbr1.height, 20, 20, "2");
		mbr3 = new GuiButton(56, mbrAdd.x, mbr2.y+mbr2.height, 20, 20, "3");
		this.buttonList.add(mbrAdd);
		this.buttonList.add(mbrSub);
		this.buttonList.add(mbr0);
		this.buttonList.add(mbr1);
		this.buttonList.add(mbr2);
		this.buttonList.add(mbr3);
		listGuildMembers.visible = false;
		mbrInviteBox.setVisible(false);
		mbrAdd.visible = false;
		mbrSub.visible = false;
		mbr0.visible = false;
		mbr1.visible = false;
		mbr2.visible = false;
		mbr3.visible = false;
		//markets menu specific objects
		marketList = new GuiListAdminMarket(this, vendList, Reference.NIL, 0, mc, 83, 40, (this.width-80)/2, this.height-45, 25);
		toggleLocal = new GuiButton(60, 83, 5, 50, 20, "Local");
		toggleGlobal = new GuiButton(61, toggleLocal.x + toggleLocal.width, 5, toggleLocal.width, 20, "Global");
		toggleAuction = new GuiButton(62, toggleGlobal.x + toggleLocal.width, 5, toggleLocal.width, 20, "Auction");
		toggleServer = new GuiButton(63, toggleAuction.x + toggleLocal.width, 5, toggleLocal.width, 20, "Server");
		addSale = new GuiButton(64, toggleServer.x + toggleServer.width + 5, 5, 75, 20, "Add Posting");
		editSave = new GuiButton(65, marketList.x + marketList.width + 3, this.height - 30, 75, 20, "Save Changes");
		saleRemove = new GuiButton(66, editSave.x + editSave.width + 2, editSave.y, editSave.width, 20, "Remove");
		saleExpire = new GuiButton(67, saleRemove.x, saleRemove.y - 21, editSave.width, 20, "Expire");
		priceBox = new GuiTextField(601, this.fontRenderer, marketList.x + marketList.width + 3, marketList.y + 22, 75, 20);
		stockBox = new GuiTextField(602, this.fontRenderer, marketList.x + marketList.width + 3, marketList.y + 55, 75, 20);
		toggleInfinite = new GuiButton(68, stockBox.x+stockBox.width+2, stockBox.y, 70, 20, "");
		toggleVendorGive = new GuiButton(69, priceBox.x+priceBox.width+2, priceBox.y, 70, 20, "");	
		this.buttonList.add(toggleLocal);
		this.buttonList.add(toggleGlobal);
		this.buttonList.add(toggleAuction);
		this.buttonList.add(toggleServer);
		this.buttonList.add(addSale);
		this.buttonList.add(editSave);
		this.buttonList.add(toggleVendorGive);
		this.buttonList.add(toggleInfinite);
		this.buttonList.add(saleExpire);
		this.buttonList.add(saleRemove);
		marketList.visible = false;
		toggleLocal.visible = false;
		toggleGlobal.visible = false;
		toggleAuction.visible = false;
		toggleServer.visible = false;
		addSale.visible = false;
		editSave.visible = false;
		toggleVendorGive.visible = false;
		toggleInfinite.visible = false;
		saleExpire.visible = false;
		saleRemove.visible = false;
		priceBox.setVisible(false);
		stockBox.setVisible(false);
		updateVisibility();
	}
	
	public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (guiListAccounts.visible) {guiListAccounts.handleMouseInput();}
        if (guildList.visible) {guildList.handleMouseInput();}
        if (marketList.visible) {marketList.handleMouseInput();}
        if (listGuildPerms.visible) {listGuildPerms.handleMouseInput();}
        if (guicoreChunkList.visible) {guicoreChunkList.handleMouseInput();}
        if (guioutpostChunkList.visible) {guioutpostChunkList.handleMouseInput();}
        if (listGuildMembers.visible) {listGuildMembers.handleMouseInput();}
    }
	
	public void updateVisibility() {
		//Main menu buttons
		toggleAccount.enabled = activeMenu == AdminGuiType.ACCOUNT ? false : true;
		toggleGuild.enabled = activeMenu == AdminGuiType.GUILD_SELECT ? false : true;
		toggleMarket.enabled = activeMenu == AdminGuiType.MARKET ? false : true;
		//account objects
		guiListAccounts.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		toggleAccountPlayer.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		toggleAccountGuild.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		balanceBox.setVisible(activeMenu == AdminGuiType.ACCOUNT);
		accountSet.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		accountAdd.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		accountRemove.visible = activeMenu == AdminGuiType.ACCOUNT ? true: false;
		toggleAccountPlayer.enabled = isPlayerList ? false : true;
		toggleAccountGuild.enabled = isPlayerList ? true : false;
		//guild select objects
		selectGuild.visible = activeMenu == AdminGuiType.GUILD_SELECT;
		deleteGuild.visible = activeMenu == AdminGuiType.GUILD_SELECT;
		guildList.visible = activeMenu == AdminGuiType.GUILD_SELECT;
		//guild main objects
		setOpen.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		setOpen.displayString = guiGuild.openToJoin ? "Public" : "Private";
		set0.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		set1.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		set2.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		set3.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		openLand.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		openMembers.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		nameBox.setVisible(activeMenu == AdminGuiType.GUILD_MAIN ? true : false);
		nameBox.setText(guiGuild.guildName);
		nameBox.setCursorPositionZero();
		taxBox.setVisible(activeMenu == AdminGuiType.GUILD_MAIN ? true : false);
		taxBox.setText(taxformat.format(guiGuild.guildTax));
		perm0.setVisible(activeMenu == AdminGuiType.GUILD_MAIN ? true : false);
		perm0.setText(guiGuild.permLevels.getOrDefault(0, "Leader"));
		perm0.setCursorPositionZero();
		perm1.setVisible(activeMenu == AdminGuiType.GUILD_MAIN ? true : false);
		perm1.setText(guiGuild.permLevels.getOrDefault(1, "Dignitary"));
		perm1.setCursorPositionZero();
		perm2.setVisible(activeMenu == AdminGuiType.GUILD_MAIN ? true : false);
		perm2.setText(guiGuild.permLevels.getOrDefault(2, "Trustee"));
		perm2.setCursorPositionZero();
		perm3.setVisible(activeMenu == AdminGuiType.GUILD_MAIN ? true : false);
		perm3.setText(guiGuild.permLevels.getOrDefault(3, "Member"));
		perm3.setCursorPositionZero();
		saveGuild.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		listGuildPerms.visible = activeMenu == AdminGuiType.GUILD_MAIN ? true : false;
		//guild land objects
		guicoreChunkList.visible = activeMenu == AdminGuiType.GUILD_LAND ? true : false;
		guioutpostChunkList.visible = activeMenu == AdminGuiType.GUILD_LAND ? true : false;
		landValue.setVisible(activeMenu == AdminGuiType.GUILD_LAND ? true : false);
		landValue.setText(df.format(value));
		landPublic.visible = activeMenu == AdminGuiType.GUILD_LAND ? true : false;
		landPublic.displayString = isPublic ? "Public Land" : "Private Land";
		landForSale.visible = activeMenu == AdminGuiType.GUILD_LAND ? true : false;
		landForSale.displayString = "For Sale: "+ (isForSale ? TextFormatting.RED+"Yes" : TextFormatting.BLUE+"No");
		landOutpost.visible = activeMenu == AdminGuiType.GUILD_LAND ? true : false;
		landOutpost.displayString = "Outpost: " + (isOutpost ? TextFormatting.RED+"Yes" : TextFormatting.BLUE+"No");
		landSave.visible = activeMenu == AdminGuiType.GUILD_LAND ? true : false;
		//guild member objects
		listGuildMembers.visible = activeMenu == AdminGuiType.GUILD_MEMBER ? true : false;
		mbrInviteBox.setVisible(activeMenu == AdminGuiType.GUILD_MEMBER ? true : false);
		mbrAdd.visible = activeMenu == AdminGuiType.GUILD_MEMBER ? true : false;
		mbrSub.visible = activeMenu == AdminGuiType.GUILD_MEMBER ? true : false;
		mbr0.visible = activeMenu == AdminGuiType.GUILD_MEMBER ? true : false;
		mbr1.visible = activeMenu == AdminGuiType.GUILD_MEMBER ? true : false;
		mbr2.visible = activeMenu == AdminGuiType.GUILD_MEMBER ? true : false;
		mbr3.visible = activeMenu == AdminGuiType.GUILD_MEMBER ? true : false;
		//market objects
		marketList.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleLocal.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleGlobal.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleAuction.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleServer.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		addSale.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		editSave.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleVendorGive.visible = (activeMenu == AdminGuiType.MARKET && selectedMarket != 2) ? true : false;
		isVendorGive = (marketList.selectedIdx >= 0 ? (marketList.getSelectedMember().posting.item.vendorGiveItem ? true : false) : false);
		toggleVendorGive.displayString = isVendorGive ? "Giving" : "Requesting";
		toggleInfinite.visible = (activeMenu == AdminGuiType.MARKET && selectedMarket != 2) ? true : false;
		isInfinite = (marketList.selectedIdx >= 0 ? (marketList.getSelectedMember().posting.item.infinite ? true : false) : false);
		toggleInfinite.displayString = isInfinite ? "Infinite" : "Remaining";
		saleExpire.visible = (activeMenu == AdminGuiType.MARKET && selectedMarket == 2) ? true : false;
		saleRemove.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleLocal.enabled = selectedMarket == 0 ? false : true;
		toggleGlobal.enabled = selectedMarket == 1 ? false : true;
		toggleAuction.enabled = selectedMarket == 2 ? false : true;
		toggleServer.enabled = selectedMarket == 3 ? false : true;
		priceBox.setVisible(activeMenu == AdminGuiType.MARKET);
		priceBox.setText(marketList.selectedIdx >= 0 ? df.format(marketList.getSelectedMember().posting.item.price) : "0");
		stockBox.setVisible(activeMenu == AdminGuiType.MARKET && selectedMarket != 2);
		stockBox.setEnabled(marketList.selectedIdx >= 0 ? !marketList.getSelectedMember().posting.item.infinite : false);
		stockBox.setText(marketList.selectedIdx >= 0 ? String.valueOf(marketList.getSelectedMember().posting.item.vendStock) : "0");
		//market sell objects
	}
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == exitButton) mc.player.closeScreen();
		//Main menu buttons
		if (button == toggleAccount) {
			activeMenu = AdminGuiType.ACCOUNT;
			isPlayerList = true;
			Main.NET.sendToServer(new MessageAdminToServer(false));
			updateVisibility();
		}
		if (button == toggleGuild) {
			activeMenu = AdminGuiType.GUILD_SELECT;
			Main.NET.sendToServer(new MessageAdminToServer(0));
			updateVisibility();
		}
		if (button == toggleMarket) {
			marketList.selectedIdx = -1;
			selectedMarket = 0;
			activeMenu = AdminGuiType.MARKET;
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.LOCAL));
			updateVisibility();
		}
		//Account Menu Buttons
		if (button == toggleAccountPlayer) {
			isPlayerList = true;
			guiListAccounts.selectedIdx = -1;
			guiListAccounts.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(false));
		}
		if (button == toggleAccountGuild) {
			isPlayerList = false;
			guiListAccounts.selectedIdx = -1;
			guiListAccounts.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(true));
		}
		if (button == accountSet && guiListAccounts.selectedIdx >= 0) {
			double amount = 0D;
			try {amount = Double.valueOf(balanceBox.getText());} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(!isPlayerList, guiListAccounts.getSelectedMember().owner, amount));
		}
		if (button == accountAdd  && guiListAccounts.selectedIdx >= 0) {
			double amount = guiListAccounts.getSelectedMember().balance;
			try {amount += Double.valueOf(balanceBox.getText());} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(!isPlayerList, guiListAccounts.getSelectedMember().owner, amount));
		}
		if (button == accountRemove  && guiListAccounts.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(!isPlayerList, guiListAccounts.getSelectedMember().owner));
			guiListAccounts.selectedIdx = -1;
			guiListAccounts.selectedElement = -1;
		}
		//Market menu buttons
		if (button == toggleLocal) {
			selectedMarket = 0;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.LOCAL));		
		}
		if (button == toggleGlobal) {
			selectedMarket = 1;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.GLOBAL));
		}
		if (button == toggleAuction) {
			selectedMarket = 2;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.AUCTION));
		}
		if (button == toggleServer) {
			selectedMarket = 3;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.SERVER));
		}
		if (button == addSale) {
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.SALE_GUI_LAUNCH));
		}
		if (button == toggleVendorGive) {
			isVendorGive = isVendorGive ? false: true;
			toggleVendorGive.displayString = isVendorGive ? "Giving" : "Requesting";
		}
		if (button == toggleInfinite) {
			isInfinite = isInfinite ? false : true;
			toggleInfinite.displayString = isInfinite ? "Infinite" : "Remaining";
			stockBox.setEnabled(isInfinite ? false : true);
		}
		if (button == editSave && marketList.selectedIdx >= 0) {			
			MktPktType type = selectedMarket == 0 ? MktPktType.LOCAL : (selectedMarket == 1 ? MktPktType.GLOBAL : (selectedMarket == 2 ? MktPktType.AUCTION : MktPktType.SERVER));
			double amount = -1D;
			try {Math.abs(amount = Double.valueOf(priceBox.getText()));} catch (NumberFormatException e) {}
			int stock = 0;
			try {Math.abs(stock = Integer.valueOf(stockBox.getText()));} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(type, marketList.getSelectedMember().posting.key, amount, isVendorGive, stock, isInfinite, marketList.getSelectedMember().posting.item.bidEnd));
		}
		if (button == saleExpire && marketList.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.AUCTION, marketList.getSelectedMember().posting.key, true));
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;			
		}
		if (button == saleRemove && marketList.selectedIdx >= 0) {
			MktPktType type = selectedMarket == 0 ? MktPktType.LOCAL : (selectedMarket == 1 ? MktPktType.GLOBAL : (selectedMarket == 2 ? MktPktType.AUCTION : MktPktType.SERVER));
			Main.NET.sendToServer(new MessageAdminToServer(type, marketList.getSelectedMember().posting.key, false));
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
		}
		//guild menu actions
		if (button == selectGuild && guildList.selectedIdx >= 0) {
			activeMenu = AdminGuiType.GUILD_MAIN;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 0));
		}
		if (button == deleteGuild && guildList.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 3));
			Main.NET.sendToServer(new MessageAdminToServer(0));
			updateVisibility();
		}
		//guild main actions
		if (button == setOpen) {
			guiGuild.openToJoin = guiGuild.openToJoin ? false : true;
			setOpen.displayString = guiGuild.openToJoin ? "Public" : "Private";
		}
		if (button == set0 && listGuildPerms.selectedIdx >= 0) {
			guiGuild.permissions.put(listGuildPerms.getSelectedMember().permIndex, 0);
			listGuildPerms.refreshList();
		}
		if (button == set1 && listGuildPerms.selectedIdx >= 0) {
			guiGuild.permissions.put(listGuildPerms.getSelectedMember().permIndex, 1);
			listGuildPerms.refreshList();
		}
		if (button == set2 && listGuildPerms.selectedIdx >= 0) {
			guiGuild.permissions.put(listGuildPerms.getSelectedMember().permIndex, 2);
			listGuildPerms.refreshList();
		}
		if (button == set3 && listGuildPerms.selectedIdx >= 0) {
			guiGuild.permissions.put(listGuildPerms.getSelectedMember().permIndex, 3);
			listGuildPerms.refreshList();
		}
		if (button == saveGuild) {
			double amount = 0D;
			try {amount = Math.abs(Double.valueOf(taxBox.getText()));} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, nameBox.getText(), guiGuild.openToJoin, amount, perm0.getText(), perm1.getText(), perm2.getText(), perm3.getText(), guiGuild.permissions));
		}
		if (button == openLand) {
			activeMenu = AdminGuiType.GUILD_LAND;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 1));
		}
		if (button == openMembers) {
			activeMenu = AdminGuiType.GUILD_MEMBER;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 2));
		}
		//guild land actions
		if (button == landPublic) {
			isPublic = isPublic ? false : true;
			landPublic.displayString = isPublic ? "Public Land" : "Private Land";
		}
		if (button == landForSale) {
			isForSale = isForSale ? false : true;
			landForSale.displayString = "For Sale: "+ (isForSale ? TextFormatting.RED+"Yes" : TextFormatting.BLUE+"No");
		}
		if (button == landOutpost) {
			isOutpost = isOutpost ? false : true;
			landOutpost.displayString = "Outpost: " + (isOutpost ? TextFormatting.RED+"Yes" : TextFormatting.BLUE+"No");
		}
		if (button == landSave) {
			double amount = 0D;
			try {amount = Math.abs(Double.valueOf(landValue.getText()));} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(guicoreChunkList.getSelectedMember().pos.x, guicoreChunkList.getSelectedMember().pos.z, amount, isPublic, isForSale, isOutpost));
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 1));
		}
		//guild member menu actions
		if (button == mbrAdd) {
			Main.NET.sendToServer(new MessageMemberInfoToServer(mbrInviteBox.getText(), guildList.getSelectedMember().entityID, true));
		}
		if (button == mbrSub) {
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, listGuildMembers.getSelectedMember().player, -2));
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 2));
		}
		if (button == mbr0 && listGuildMembers.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, listGuildMembers.getSelectedMember().player, 0));
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 2));
		}
		if (button == mbr1 && listGuildMembers.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, listGuildMembers.getSelectedMember().player, 1));
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 2));
		}
		if (button == mbr2 && listGuildMembers.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, listGuildMembers.getSelectedMember().player, 2));
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 2));
		}
		if (button == mbr3 && listGuildMembers.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, listGuildMembers.getSelectedMember().player, 3));
			Main.NET.sendToServer(new MessageAdminToServer(guildList.getSelectedMember().entityID, 2));
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (guiListAccounts.visible) {guiListAccounts.mouseClicked(mouseX, mouseY, mouseButton);}
		if (balanceBox.getVisible()) {balanceBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (priceBox.getVisible()) {priceBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (stockBox.getVisible()) {stockBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (guildList.visible) {guildList.mouseClicked(mouseX, mouseY, mouseButton);}
		if (marketList.visible) {marketList.mouseClicked(mouseX, mouseY, mouseButton);}
		if (nameBox.getVisible()) {nameBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (taxBox.getVisible()) {taxBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (perm0.getVisible()) {perm0.mouseClicked(mouseX, mouseY, mouseButton);}
		if (perm1.getVisible()) {perm1.mouseClicked(mouseX, mouseY, mouseButton);}
		if (perm2.getVisible()) {perm2.mouseClicked(mouseX, mouseY, mouseButton);}
		if (perm3.getVisible()) {perm3.mouseClicked(mouseX, mouseY, mouseButton);}
		if (listGuildPerms.visible) {listGuildPerms.mouseClicked(mouseX, mouseY, mouseButton);}
		if (guicoreChunkList.visible) {guicoreChunkList.mouseClicked(mouseX, mouseY, mouseButton);}
        if (guioutpostChunkList.visible) {guioutpostChunkList.mouseClicked(mouseX, mouseY, mouseButton);}
        if (landValue.getVisible()) {landValue.mouseClicked(mouseX, mouseY, mouseButton);}
		if (listGuildMembers.visible) {listGuildMembers.mouseClicked(mouseX, mouseY, mouseButton);}
		if (mbrInviteBox.getVisible()) {mbrInviteBox.mouseClicked(mouseX, mouseY, mouseButton);}
	}
	
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (guiListAccounts.visible) {guiListAccounts.mouseReleased(mouseX, mouseY, state);}
		if (guildList.visible) {guildList.mouseReleased(mouseX, mouseY, state);}
		if (marketList.visible) {marketList.mouseReleased(mouseX, mouseY, state);}
		if (marketList.selectedIdx >= 0 && mouseX > marketList.x && mouseX < marketList.x+marketList.width-6 && mouseY > marketList.y && mouseY < marketList.height+marketList.y) {
			MktPktType type = selectedMarket == 0 ? MktPktType.LOCAL : (selectedMarket == 1 ? MktPktType.GLOBAL : (selectedMarket == 2 ? MktPktType.AUCTION : MktPktType.SERVER));
			Main.NET.sendToServer(new MessageAdminToServer(type, marketList.getSelectedMember().posting.key));
		}
		if (listGuildPerms.visible) {listGuildPerms.mouseReleased(mouseX, mouseY, state);}
		if (guicoreChunkList.visible) {guicoreChunkList.mouseReleased(mouseX, mouseY, state);}
		if (guicoreChunkList.selectedIdx >= 0 && mouseX > guicoreChunkList.x && mouseX < guicoreChunkList.x+guicoreChunkList.width-6 && mouseY > guicoreChunkList.y && mouseY < guicoreChunkList.y+guicoreChunkList.height) {
			guioutpostChunkList.selectedIdx = -1;
			guioutpostChunkList.selectedElement = -1;
			Main.NET.sendToServer(new MessageAdminToServer(guicoreChunkList.getSelectedMember().pos.x, guicoreChunkList.getSelectedMember().pos.z));
		}
        if (guioutpostChunkList.visible) {guioutpostChunkList.mouseReleased(mouseX, mouseY, state);}
        if (guioutpostChunkList.selectedIdx >= 0 && mouseX > guioutpostChunkList.x && mouseX < guioutpostChunkList.x+guioutpostChunkList.width-6 && mouseY > guioutpostChunkList.y && mouseY < guioutpostChunkList.y+guioutpostChunkList.height) {
			guicoreChunkList.selectedIdx = -1;
			guicoreChunkList.selectedElement = -1;
			Main.NET.sendToServer(new MessageAdminToServer(guioutpostChunkList.getSelectedMember().pos.x, guioutpostChunkList.getSelectedMember().pos.z));
		}
        if (listGuildMembers.visible) {listGuildMembers.mouseReleased(mouseX, mouseY, state);}
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (balanceBox.getVisible() && CoreUtils.validNumberKey(keyCode)) balanceBox.textboxKeyTyped(typedChar, keyCode);
		if (priceBox.getVisible() && CoreUtils.validNumberKey(keyCode)) priceBox.textboxKeyTyped(typedChar, keyCode);
		if (stockBox.getVisible() && CoreUtils.validNumberKey(keyCode)) stockBox.textboxKeyTyped(typedChar, keyCode);
		if (nameBox.getVisible()) nameBox.textboxKeyTyped(typedChar, keyCode);
		if (taxBox.getVisible() && CoreUtils.validNumberKey(keyCode)) taxBox.textboxKeyTyped(typedChar, keyCode);
		if (perm0.getVisible()) perm0.textboxKeyTyped(typedChar, keyCode);
		if (perm1.getVisible()) perm1.textboxKeyTyped(typedChar, keyCode);
		if (perm2.getVisible()) perm2.textboxKeyTyped(typedChar, keyCode);
		if (perm3.getVisible()) perm3.textboxKeyTyped(typedChar, keyCode);
		if (landValue.getVisible()) landValue.textboxKeyTyped(typedChar, keyCode);
		if (mbrInviteBox.getVisible()) mbrInviteBox.textboxKeyTyped(typedChar, keyCode);
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    	this.drawDefaultBackground();
    	switch (activeMenu) {
    	//draw only the objects for the specfic screen toggled
    	case ACCOUNT: {
    		this.drawString(this.fontRenderer, TextFormatting.GOLD+"Account Balance:", balanceBox.x, balanceBox.y - 22, 16777215);
    		double balance = guiListAccounts.selectedIdx >= 0 ? guiListAccounts.getSelectedMember().balance : 0;
    		this.drawString(this.fontRenderer, TextFormatting.GOLD+"$"+df.format(balance), balanceBox.x, balanceBox.y - 11, 16777215);
    		this.drawString(this.fontRenderer, "Account Owner List", guiListAccounts.x, guiListAccounts.y - 11, 16777215);
    		this.drawString(this.fontRenderer, TextFormatting.GRAY+"Negative numbers when clicking", balanceBox.x, balanceBox.y+46, 16777215);
    		this.drawString(this.fontRenderer, TextFormatting.GRAY+"'add' reduce the balance.", balanceBox.x, balanceBox.y+57, 16777215);
    		balanceBox.drawTextBox();
    		guiListAccounts.drawScreen(mouseX, mouseY, partialTicks);
    		break;
    	}
    	case GUILD_SELECT: {
    		this.drawString(this.fontRenderer, TextFormatting.GREEN+"GUILD LIST", selectGuild.x, 15, 16777215);
    		guildList.drawScreen(mouseX, mouseY, partialTicks);
    		break;
    	}
    	case GUILD_MAIN: {
    		this.drawString(this.fontRenderer, "Guild Name", nameBox.x, nameBox.y-10, 16777215);
    		this.drawString(this.fontRenderer, "Tax Rate", taxBox.x, taxBox.y-10, 16777215);
    		this.drawString(this.fontRenderer, "Rank Names", perm0.x, perm0.y-10, 16777215);
    		nameBox.drawTextBox();
    		taxBox.drawTextBox();
    		perm0.drawTextBox();
    		perm1.drawTextBox();
    		perm2.drawTextBox();
    		perm3.drawTextBox();
    		listGuildPerms.drawScreen(mouseX, mouseY, partialTicks);
    		break;
    	}
    	case GUILD_LAND: {
    		this.drawString(this.fontRenderer, "Core Land", guicoreChunkList.x, guicoreChunkList.y-10, 16777215);
    		this.drawString(this.fontRenderer, "Outpost Land", guioutpostChunkList.x, guioutpostChunkList.y-10, 16777215);
    		this.drawString(this.fontRenderer, "Land Value", landValue.x, landValue.y-10, 16777215);
    		guicoreChunkList.drawScreen(mouseX, mouseY, partialTicks);
    		guioutpostChunkList.drawScreen(mouseX, mouseY, partialTicks);
    		landValue.drawTextBox();
    		break;
    	}
    	case GUILD_MEMBER: {
    		this.drawString(this.fontRenderer, "Member List", listGuildMembers.x, listGuildMembers.y-10, 16777215);
    		listGuildMembers.drawScreen(mouseX, mouseY, partialTicks);
    		mbrInviteBox.drawTextBox();
    		break;
    	}
    	case MARKET: {
    		this.drawString(this.fontRenderer, "Market List", marketList.x, marketList.y - 11, 16777215);
    		int baseX = marketList.x+marketList.width+2;
    		int baseY = marketList.y;
    		this.drawString(this.fontRenderer, "Vendor: "+vendorName, baseX, baseY, 16777215);
    		this.drawString(this.fontRenderer, TextFormatting.GOLD+"Price", baseX, baseY+11, 16777215);
    		if (selectedMarket != 2) this.drawString(this.fontRenderer, TextFormatting.LIGHT_PURPLE+"Supply Remaining", baseX, baseY + 44, 16777215);
    		String line1 = selectedMarket == 2 ? "Highest Bidder: " : "Locality:";
    		String line2 = selectedMarket == 2 ? bidderName : locName;
    		long end = marketList.selectedIdx >= 0 ? marketList.getSelectedMember().posting.item.bidEnd : 0;
    		String line3 = selectedMarket == 2 ? TextFormatting.AQUA+"Bidding Ends: " : "";
    		String line4 = selectedMarket == 2 ? String.valueOf(new Timestamp(end)) : "";
    		this.drawString(this.fontRenderer, line1, baseX, stockBox.y+stockBox.height+10, 16777215);
    		this.drawString(this.fontRenderer, line2, baseX, stockBox.y+stockBox.height+21, 16777215);
    		this.drawString(this.fontRenderer, line3, baseX, stockBox.y+stockBox.height+32, 16777215);
    		this.drawString(this.fontRenderer, line4, baseX, stockBox.y+stockBox.height+43, 16777215);
    		marketList.drawScreen(mouseX, mouseY, partialTicks);
    		priceBox.drawTextBox();
    		stockBox.drawTextBox();
    		//render tooltips on marketList
	        if (slotIdx >=0) {
            	if (mouseX > marketList.x && mouseX < marketList.x+20 && mouseY > marketList.getListEntry(slotIdx).getY() && mouseY < marketList.getListEntry(slotIdx).getY()+20) {
    	        	RenderHelper.enableGUIStandardItemLighting();
    	        	renderToolTip(marketList.getListEntry(slotIdx).posting.item.item, mouseX, mouseY);
    	        	RenderHelper.disableStandardItemLighting();
            	}
            	else {slotIdx = -1;} 
            }
    		break;
    	}
    	case MARKET_SELL: {
    		break;
    	}
    	default:
    	}		
		//default screen objects
    	super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public class GuiListAccount extends GuiNewListExtended{
	    private final GuiAdmin parentGui;
	    public Map<Account, String> accountList;
	    private final List<GuiListAccountEntry> entries = Lists.<GuiListAccountEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListAccount(GuiAdmin parentGui, Minecraft mcIn, Map<Account, String> accountList, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.parentGui = parentGui;
			this.accountList = accountList;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	        accountList = parentGui.accountList;

	        for (Map.Entry<Account, String> entry : accountList.entrySet()){
	            this.entries.add(new GuiListAccountEntry(this, entry.getKey().owner, entry.getKey().balance, entry.getValue()));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListAccountEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListAccountEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListAccountEntry implements GuiNewListExtended.IGuiNewListEntry{
		private final UUID owner;
		private final double balance;
		private final String name; 
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListAccount containingListSel;
	    private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
		
		public GuiListAccountEntry (GuiListAccount listSelectionIn, UUID owner, double balance, String name) {
			containingListSel = listSelectionIn;
			this.owner = owner;
			this.balance = balance;
			this.name = name;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(name +TextFormatting.GOLD+" $"+ df.format(balance), x+3, y , 16777215);
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

	}

	public class GuiListNameList extends GuiNewListExtended{
	    private final GuiAdmin parentGui;
	    public Map<UUID, String> nameList;
	    private final List<GuiListNameListEntry> entries = Lists.<GuiListNameListEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListNameList(GuiAdmin parentGui, Minecraft mcIn, Map<UUID, String> nameList, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.parentGui = parentGui;
			this.nameList = nameList;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	        nameList = parentGui.nameList;

	        for (Map.Entry<UUID, String> entry : nameList.entrySet()){
	            this.entries.add(new GuiListNameListEntry(this, entry.getKey(), entry.getValue()));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListNameListEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListNameListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListNameListEntry implements GuiNewListExtended.IGuiNewListEntry{
		private final UUID entityID;
		private final String name; 
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListNameList containingListSel;
		
		public GuiListNameListEntry (GuiListNameList listSelectionIn, UUID entityID, String name) {
			containingListSel = listSelectionIn;
			this.entityID = entityID;
			this.name = name;
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

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

	}

	public class GuiListAdminMarket extends GuiNewListExtended{
	    private final GuiAdmin parentGui;
	    public List<MarketListItem> vendList;
	    public UUID locality;
	    private final List<GuiListAdminMarketEntry> entries = Lists.<GuiListAdminMarketEntry>newArrayList();
	    private int selectedIdx = -1;
	    public int hoveredSlot = -1;
	    public int listType = -1;
		
		public GuiListAdminMarket(GuiAdmin parentGui, List<MarketListItem> vendList, UUID locality, int listType, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.parentGui = parentGui;
			this.vendList = vendList;
			this.locality = locality;
			this.listType = listType;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	    	vendList = parentGui.vendList;
	        for (MarketListItem entry : vendList) {
	            this.entries.add(new GuiListAdminMarketEntry(this, entry, locality));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListAdminMarketEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListAdminMarketEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListAdminMarketEntry implements GuiNewListExtended.IGuiNewListEntry{
	    private final GuiListAdminMarket containingListSel;
	    private Minecraft client = Minecraft.getMinecraft();
	    MarketListItem posting;
	    private String line1, line2;
	    private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	    private int slotY;
	    
	    public int getY() {return slotY;}
		
		public GuiListAdminMarketEntry (GuiListAdminMarket listIn, MarketListItem posting, UUID locality) {
			this.containingListSel = listIn;
			this.posting = posting;
			if (containingListSel.listType != 2) {
				line1 = TextFormatting.GOLD+"$"+df.format(posting.item.price);				
				line2 = TextFormatting.LIGHT_PURPLE+(posting.item.infinite ? "INFINITE" : "Supply="+String.valueOf(posting.item.vendStock));	
				line2 += (posting.item.vendorGiveItem) ? TextFormatting.GREEN+" (Giving)": TextFormatting.BLUE+" (Requesting)";
			}
			if (containingListSel.listType == 2) {
				line1 = TextFormatting.GOLD+"$" + df.format(posting.item.price);
				line1 += (posting.item.highestBidder.equals(Reference.NIL)) ? TextFormatting.WHITE+" [No Bids]" : TextFormatting.RED+" [Active]";
				line2 = (posting.item.bidEnd < 3600000) ? TextFormatting.RED+"" : (posting.item.bidEnd < 86400000) ? TextFormatting.YELLOW+"" : TextFormatting.GREEN+"";
				line2 += String.valueOf(new Timestamp(posting.item.bidEnd));
			}
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(line1, x+30, y+3, 16777215);
	        this.client.fontRenderer.drawString(line2, x+30, y+13 , 16777215);
	        RenderHelper.enableGUIStandardItemLighting();
	        FontRenderer font = posting.item.item.getItem().getFontRenderer(posting.item.item);
	        if (font == null) font = fontRenderer;
	        itemRender.renderItemAndEffectIntoGUI(posting.item.item, x+3, y);
	        itemRender.renderItemOverlayIntoGUI(font, posting.item.item, x+3, y, null);
	        if (mouseX > x+3 && mouseX < x+23 && mouseY > y && mouseY < y+20) {containingListSel.parentGui.slotIdx = slotIndex; slotY = y;}
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

	public class GuiListAdminGuildMembers extends GuiNewListExtended{
	    private final GuiAdmin guildManager;
	    public Guild guild;
	    public Map<UUID, String> mbrNames;
	    private final List<GuiListAdminGuildMembersEntry> entries = Lists.<GuiListAdminGuildMembersEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListAdminGuildMembers(GuiAdmin guiGM, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.guildManager = guiGM;
			this.guild = guildManager.guiGuild;
			this.mbrNames = guildManager.mbrNames;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	this.guild = guildManager.guiGuild;
	    	this.mbrNames = guildManager.mbrNames;
	    	entries.clear();
	        Map<UUID, Integer> members = guild.members;

	        for (Map.Entry<UUID, Integer> entry : members.entrySet()){
	            this.entries.add(new GuiListAdminGuildMembersEntry(this, entry.getKey(), entry.getValue(), guild.permLevels, mbrNames));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListAdminGuildMembersEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListAdminGuildMembersEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListAdminGuildMembersEntry implements GuiNewListExtended.IGuiNewListEntry{
		public final UUID player;
		private final String name; 
		public int permLvl;
		private Map<Integer, String> perms;
		private  Map<UUID, String> mbrNames;
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListAdminGuildMembers containingListSel;
		
		public GuiListAdminGuildMembersEntry (GuiListAdminGuildMembers listSelectionIn, UUID player, int permLvl, Map<Integer, String> permLvls, Map<UUID, String> mbrNames) {
			containingListSel = listSelectionIn;
			this.mbrNames = mbrNames;
			this.player = player;
			this.permLvl = permLvl;
			this.perms = permLvls;		
			this.name = mbrNames.get(player);
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			String rankFormat = "";
	        switch(permLvl) {
	        case 0: {
	        	rankFormat = TextFormatting.DARK_GREEN + perms.getOrDefault(0, "Leader");
	        	break;
	        }
	        case 1: {
	        	rankFormat = TextFormatting.DARK_PURPLE + perms.getOrDefault(1, "Dignitary");
	        	break;
	        }
	        case 2: {
	        	rankFormat = TextFormatting.BLUE + perms.getOrDefault(2, "Trustee");
	        	break;
	        }
	        case 3: {
	        	rankFormat = perms.getOrDefault(3, "Member");
	        	break;
	        }
	        case -1: {
	        	rankFormat = TextFormatting.DARK_RED + "Invited";
	        	break;
	        }
	        default:
	        break;
	        }
	        
	        this.client.fontRenderer.drawString(name+" :"+rankFormat, x+3, y , 16777215);
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {		
		}

	}

	public class GuiListAdminPerms extends GuiNewListExtended{
	    private final GuiAdmin guildManager;
	    public Map<String, Integer> perms;
	    public Map<Integer, String> permRanks;
	    public final String permIndex[] = new String[11];
	    private final List<GuiListAdminPermsEntry> entries = Lists.<GuiListAdminPermsEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListAdminPerms(GuiAdmin guiGM, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.guildManager = guiGM;
			this.perms = guildManager.guiGuild.permissions;
			this.permRanks = guildManager.guiGuild.permLevels;
			permIndex[0] = "setname";
			permIndex[1] = "setopen";
			permIndex[2] = "settax";
			permIndex[3] = "setperms";
			permIndex[4] = "setinvite";
			permIndex[5] = "setkick";
			permIndex[6] = "setclaim";
			permIndex[7] = "setsell";
			permIndex[8] = "setwithdraw";
			permIndex[9] = "setpromotedemote";
			permIndex[10] = "managesublet";
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	this.perms = guildManager.guiGuild.permissions;
			this.permRanks = guildManager.guiGuild.permLevels;
	    	this.entries.clear();
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[0], TextFormatting.YELLOW+"Change Guild Name  ", colorize(perms.get(permIndex[0]))+permRanks.get(perms.get(permIndex[0]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[1], TextFormatting.YELLOW+"Change Open-To-Join", colorize(perms.get(permIndex[1]))+permRanks.get(perms.get(permIndex[1]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[2], TextFormatting.YELLOW+"Change Guild Tax   ", colorize(perms.get(permIndex[2]))+permRanks.get(perms.get(permIndex[2]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[3], TextFormatting.YELLOW+"Change Permissions ", colorize(perms.get(permIndex[3]))+permRanks.get(perms.get(permIndex[3]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[4], TextFormatting.YELLOW+"Change Invite Level", colorize(perms.get(permIndex[4]))+permRanks.get(perms.get(permIndex[4]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[5], TextFormatting.YELLOW+"Change Kick Level  ", colorize(perms.get(permIndex[5]))+permRanks.get(perms.get(permIndex[5]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[6], TextFormatting.YELLOW+"Change Claim Level ", colorize(perms.get(permIndex[6]))+permRanks.get(perms.get(permIndex[6]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[7], TextFormatting.YELLOW+"Change Sell Level  ", colorize(perms.get(permIndex[7]))+permRanks.get(perms.get(permIndex[7]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[8], TextFormatting.YELLOW+"Change Withdraw Lvl", colorize(perms.get(permIndex[8]))+permRanks.get(perms.get(permIndex[8]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[9], TextFormatting.YELLOW+"Change Member Ranks", colorize(perms.get(permIndex[9]))+permRanks.get(perms.get(permIndex[9]))));
	        this.entries.add(new GuiListAdminPermsEntry(this, permIndex[10],TextFormatting.YELLOW+"Manage Subletting  ", colorize(perms.get(permIndex[10]))+permRanks.get(perms.get(permIndex[10]))));
	    }
	    
	    private String colorize(int rank) {
	    	switch (rank) {
	    	case 0: {return TextFormatting.DARK_GREEN+"";}
	    	case 1: {return TextFormatting.DARK_PURPLE+"";}
	    	case 2: {return TextFormatting.BLUE+"";}
	    	case 3: {return TextFormatting.WHITE+"";}
	    	default: return "";}
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListAdminPermsEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListAdminPermsEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListAdminPermsEntry implements GuiNewListExtended.IGuiNewListEntry{
		private final String permIndex, permString, rankString; 
		private Minecraft client = Minecraft.getMinecraft();
		private final GuiListAdminPerms containingListSel;
		
		public GuiListAdminPermsEntry (GuiListAdminPerms listIn, String permIndex, String permString, String rankString) {
			containingListSel = listIn;
			this.permIndex = permIndex;
			this.permString = permString;
			this.rankString = rankString;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(permString, x+3, y , 16777215);
	        this.client.fontRenderer.drawString(rankString, x+120, y, 16777215);
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {		
		}

	}

	public class GuiListAdminGuildChunks extends GuiNewListExtended{
    	private final GuiAdmin guildManager;
    	private boolean coreChunk;
    	private List<ChunkPos> pos;
    	private Map<ChunkPos, Double> chunkValues;
        private final List<GuiListAdminGuildChunksEntry> entries = Lists.<GuiListAdminGuildChunksEntry>newArrayList();
        /** Index to the currently selected world */
        private int selectedIdx = -1;
        
        public GuiListAdminGuildChunks(GuiAdmin gm, List<ChunkPos> pos, Map<ChunkPos, Double> chunkValues, boolean coreChunk, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
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
                this.entries.add(new GuiListAdminGuildChunksEntry(this, ck, coreChunk));
            }
        }
        
        public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
        @Nullable
	    public GuiListAdminGuildChunksEntry getSelectedMember(){return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;}
    	@Override
    	public GuiListAdminGuildChunksEntry getListEntry(int index) {return entries.get(index);}
    	@Override
    	protected int getSize() {return entries.size();}
    }
	    
	public class GuiListAdminGuildChunksEntry implements GuiNewListExtended.IGuiNewListEntry{
    	private Minecraft client = Minecraft.getMinecraft();
    	private final GuiListAdminGuildChunks containingListSel;
    	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
    	private final ChunkPos pos;
    	private boolean coreChunk;
    	
    	public GuiListAdminGuildChunksEntry(GuiListAdminGuildChunks listSelectionIn, ChunkPos pos, boolean coreChunk) {
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

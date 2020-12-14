package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.text.DecimalFormat;

import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GuiAccountManager extends GuiScreen{
	public static final ResourceLocation BG_SRC = new ResourceLocation(Reference.MOD_ID+":guis/accountgui.png");
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private double balP;
	private int stackSize = 1;
	private GuiButton plus1, plus10, plus100, minus1, minus10, minus100;
	private GuiButton submitButton, stack1, stack10, stack32, stack64, exitButton;
	private GuiTextField valueBox;
	private int guiX, guiY, guiW;

	public GuiAccountManager(double balP) {
		this.balP = balP;
	}
	
	public void initGui() {
		guiW = 146;
		guiX = this.width/2-(guiW/2);
		guiY = this.height/2-41;		
		int d = (guiW-6)/6;
		//initialize buttons
		int id = 0;
		plus1 = new GuiButton(id++, guiX+3, guiY+15, d, 20, "+1");
		plus10 = new GuiButton(id++, plus1.x+plus1.width, plus1.y, d, 20, "+10");
		plus100 = new GuiButton(id++, plus10.x+plus10.width, plus1.y, d, 20, "+100");
		valueBox = new GuiTextField(id++, fontRenderer, guiX+3, plus1.y+plus1.height, d*3, 20);
		minus1 = new GuiButton(id++, guiX+3, valueBox.y+valueBox.height, d, 20, "-1");
		minus10 = new GuiButton(id++, minus1.x+minus1.width, minus1.y, d, 20, "-10");
		minus100 = new GuiButton(id++, minus10.x+minus10.width, minus1.y, d, 20, "-100");
		submitButton = new GuiButton(id++, valueBox.x+valueBox.width+5, valueBox.y, valueBox.width, 20, new TextComponentTranslation("gui.account.withdraw").getFormattedText());
		stack1 = new GuiButton(id++, submitButton.x, plus1.y, d, 20, "x1");
		stack10 = new GuiButton(id++, stack1.x+stack1.width, plus1.y, d, 20, "x10");
		exitButton = new GuiButton(id++, stack10.x+stack10.width, plus1.y, d, 20, "X");
		stack32 = new GuiButton(id++, submitButton.x, minus1.y, d, 20, "x32");
		stack64 = new GuiButton(id++, stack32.x+stack32.width, minus1.y, d, 20, "x64");
		//add buttons to gui
		this.buttonList.add(plus1);
		this.buttonList.add(plus10);
		this.buttonList.add(plus100);
		this.buttonList.add(minus1);
		this.buttonList.add(minus10);
		this.buttonList.add(minus100);
		this.buttonList.add(submitButton);
		this.buttonList.add(stack1);
		this.buttonList.add(stack10);
		this.buttonList.add(stack32);
		this.buttonList.add(stack64);
		this.buttonList.add(exitButton);
		//initial settings
		valueBox.setText("0");
		updateVisibility();
	}
	
	private void updateVisibility() {
		stack1.enabled = (stackSize != 1);
		stack10.enabled = (stackSize != 10);
		stack32.enabled = (stackSize != 32);
		stack64.enabled = (stackSize != 64);
	}
	
	private double currentValue() {
		double current = 0;
		String fromBox = valueBox.getText();
		fromBox = fromBox.replaceAll(",", "");
		try {current = Double.valueOf(fromBox);} catch (NumberFormatException e) {}
		return current;
	}
	
	private void updateValue(double v) {
		double current = currentValue();
		double newVal = current+v;
		if (newVal < 0) return;
		if ((newVal*stackSize) <= balP) {
			valueBox.setText(df.format(newVal));
		}
	}
	
	public void handleMouseInput()  throws IOException { super.handleMouseInput();}
	
	@SuppressWarnings("static-access")
	protected void actionPerformed(GuiButton button) {
		if (button.equals(exitButton)) {this.mc.getMinecraft().displayGuiScreen(new GuiInventory(this.mc.player));}
		if (button.equals(stack1) && (currentValue()*1 <= balP)) {
			stackSize = 1;
			updateVisibility();
		}
		if (button.equals(stack10) && (currentValue()*10 <= balP)) {
			stackSize = 10;
			updateVisibility();
		}
		if (button.equals(stack32) && (currentValue()*32 <= balP)) {
			stackSize = 32;
			updateVisibility();
		}
		if (button.equals(stack64) && (currentValue()*64 <= balP)) {
			stackSize = 64;
			updateVisibility();
		}
		if (button.equals(plus1)) updateValue(1);
		if (button.equals(plus10)) updateValue(10);
		if (button.equals(plus100)) updateValue(100);
		if (button.equals(minus1)) updateValue(-1);
		if (button.equals(minus10)) updateValue(-10);
		if (button.equals(minus100)) updateValue(-100);
		if (button.equals(submitButton) && currentValue() > 0) {
			this.mc.player.sendChatMessage("/a wit "+String.valueOf(currentValue())+" "+String.valueOf(stackSize));
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		valueBox.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	protected void mouseReleased(int mouseX, int mouseY, int state) { super.mouseReleased(mouseX, mouseY, state);}
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (this.valueBox.isFocused() && CoreUtils.validNumberKey(keyCode)) valueBox.textboxKeyTyped(typedChar, keyCode);
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.mc.getTextureManager().bindTexture(BG_SRC);
		this.drawTexturedModalRect(guiX, guiY, 0, 0, 146, 82);
		this.drawString(fontRenderer, TextFormatting.DARK_GREEN+"$"+df.format(balP), guiX+3, guiY+4, 16777215);
		valueBox.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}

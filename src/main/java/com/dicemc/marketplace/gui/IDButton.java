package com.dicemc.marketplace.gui;

import net.minecraft.client.gui.GuiButton;

public class IDButton<T extends Enum> extends GuiButton{
	public T state;

	public IDButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {super(buttonId, x, y, widthIn, heightIn, buttonText);}	
	public IDButton(int buttonId, int x, int y, String buttonText) {super(buttonId, x, y, buttonText);}
	
	public void setState(boolean enabled, T state) {this.enabled = enabled; this.state = state;}
}

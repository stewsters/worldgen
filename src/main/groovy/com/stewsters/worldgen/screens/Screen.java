package com.stewsters.worldgen.screens;

import squidpony.squidgrid.gui.swing.SwingPane;

import java.awt.event.KeyEvent;


public interface Screen {
    public void displayOutput(SwingPane display);

    public Screen respondToUserInput(KeyEvent key);
}
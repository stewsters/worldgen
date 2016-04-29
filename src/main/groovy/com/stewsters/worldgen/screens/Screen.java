package com.stewsters.worldgen.screens;

import squidpony.squidgrid.gui.swing.SwingPane;

import java.awt.event.KeyEvent;


public interface Screen {
    void displayOutput(SwingPane display);

    Screen respondToUserInput(KeyEvent key);
}
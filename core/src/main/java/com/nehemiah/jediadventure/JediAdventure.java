package com.nehemiah.jediadventure;

import com.badlogic.gdx.Game;
import com.nehemiah.jediadventure.screens.GameScreen;

public class JediAdventure extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
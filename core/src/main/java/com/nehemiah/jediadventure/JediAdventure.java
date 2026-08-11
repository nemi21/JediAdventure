package com.nehemiah.jediadventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.nehemiah.jediadventure.screens.GameScreen;
import com.nehemiah.jediadventure.screens.MainMenuScreen;

public class JediAdventure extends Game {

    @Override
    public void create() {
        showMainMenu();
    }

    public void showMainMenu() {
        changeScreen(
                new MainMenuScreen(this)
        );
    }

    public void startGame() {
        changeScreen(
                new GameScreen(this)
        );
    }

    private void changeScreen(Screen newScreen) {
        Screen previousScreen = getScreen();

        setScreen(newScreen);

        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
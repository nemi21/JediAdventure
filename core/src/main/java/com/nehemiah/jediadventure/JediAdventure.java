package com.nehemiah.jediadventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.nehemiah.jediadventure.screens.GameScreen;
import com.nehemiah.jediadventure.screens.MainMenuScreen;
import com.nehemiah.jediadventure.state.GameState;

public class JediAdventure extends Game {

    private GameState gameState;

    @Override
    public void create() {
        gameState = new GameState();
        showMainMenu();
    }

    public void showMainMenu() {
        changeScreen(
                new MainMenuScreen(this)
        );
    }

    public void startGame() {
        gameState.reset();

        changeScreen(
                new GameScreen(
                        this,
                        gameState
                )
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
package com.nehemiah.jediadventure.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nehemiah.jediadventure.entities.Player;
import com.nehemiah.jediadventure.entities.TrainingDroid;
import com.nehemiah.jediadventure.JediAdventure;
import com.nehemiah.jediadventure.ui.DialogueBox;
import com.nehemiah.jediadventure.state.GameState;
import com.nehemiah.jediadventure.state.GameState.TrainingApproach;

public class GameScreen implements Screen {

    public static final float VIEW_WIDTH = 1280f;
    public static final float VIEW_HEIGHT = 720f;
    public static final float LEVEL_WIDTH = 3200f;

    private static final float PLAYER_RESPAWN_X = 100f;
    private static final float PLAYER_RESPAWN_Y = 64f;
    
    private static final float FALL_RESPAWN_LIMIT = -160f;

    private static final float CHECKPOINT_RESPAWN_X = 1840f;
    private static final float CHECKPOINT_RESPAWN_Y = 64f;

    private static final float DROID_START_X = 760f;
    private static final float DROID_START_Y = 64f;
    
    private static final int PAUSE_RESUME_OPTION = 0;
    private static final int PAUSE_CONTROLS_OPTION = 1;
    private static final int PAUSE_MAIN_MENU_OPTION = 2;
    private static final int PAUSE_OPTION_COUNT = 3;
    
    private boolean levelComplete;
    private boolean paused;
    private boolean showingPauseControls;
    private int selectedPauseOption;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    private final Player player;
    private final TrainingDroid trainingDroid;

    private final List<Rectangle> platforms;
    private final Rectangle exitDoor;
    private final Rectangle checkpoint;
    private final List<Rectangle> oneWayPlatforms;

    private float activeRespawnX;
    private float activeRespawnY;
    private float checkpointMessageTimer;

    private boolean checkpointActivated;
    private final Rectangle trainingTerminal;
    private final Rectangle terminalInteractionArea;
    private final DialogueBox dialogueBox;
    
    private final JediAdventure game;
    private final GameState gameState;

    public GameScreen(
            JediAdventure game,
            GameState gameState) {

        this.game = game;
        this.gameState = gameState;
        camera = new OrthographicCamera();
        viewport = new FitViewport(
                VIEW_WIDTH,
                VIEW_HEIGHT,
                camera
        );
        
        
        paused = false;
        showingPauseControls = false;
        selectedPauseOption = PAUSE_RESUME_OPTION;
        
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();

        platforms = new ArrayList<>();
        
        oneWayPlatforms = new ArrayList<>();
        
        oneWayPlatforms.add(
                new Rectangle(1500f, 360f, 200f, 20f)
        );

        oneWayPlatforms.add(
                new Rectangle(1880f, 480f, 220f, 20f)
        );

        oneWayPlatforms.add(
                new Rectangle(2380f, 400f, 220f, 20f)
        );

     // Ground before the first pit.
        platforms.add(
                new Rectangle(0f, 0f, 1320f, 64f)
        );

        // Ground between the two pits.
        platforms.add(
                new Rectangle(1440f, 0f, 740f, 64f)
        );

        // Ground after the second pit.
        platforms.add(
                new Rectangle(2320f, 0f, 880f, 64f)
        );
        
     // Tall wall used to test wall sliding and wall jumping.
        platforms.add(
                new Rectangle(
                        2820f,
                        64f,
                        100f,
                        320f
                )
        );

        platforms.add(new Rectangle(220f, 160f, 240f, 32f));
        platforms.add(new Rectangle(560f, 280f, 220f, 32f));
        platforms.add(new Rectangle(900f, 180f, 220f, 32f));

        platforms.add(new Rectangle(1400f, 140f, 220f, 32f));
        platforms.add(new Rectangle(1650f, 250f, 220f, 32f));
        platforms.add(new Rectangle(1950f, 360f, 220f, 32f));
        platforms.add(new Rectangle(2300f, 250f, 220f, 32f));
        platforms.add(new Rectangle(2600f, 140f, 220f, 32f));

        player = new Player(
                PLAYER_RESPAWN_X,
                PLAYER_RESPAWN_Y
        );

        trainingDroid = new TrainingDroid(
                DROID_START_X,
                DROID_START_Y,
                700f,
                1050f
        );

        exitDoor = new Rectangle(
                3100f,
                64f,
                48f,
                100f
        );
        
        trainingTerminal = new Rectangle(
                1220f,
                64f,
                48f,
                72f
        );

        terminalInteractionArea = new Rectangle(
                trainingTerminal.x - 65f,
                trainingTerminal.y - 10f,
                trainingTerminal.width + 130f,
                trainingTerminal.height + 20f
        );
        
        checkpoint = new Rectangle(
                1780f,
                64f,
                40f,
                100f
        );

        activeRespawnX = PLAYER_RESPAWN_X;
        activeRespawnY = PLAYER_RESPAWN_Y;

        checkpointActivated = false;
        checkpointMessageTimer = 0f;

        dialogueBox = new DialogueBox();

        levelComplete = false;
    }

    @Override
    public void show() {
        viewport.update(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight(),
                true
        );
    }

    @Override
    public void render(float deltaTime) {
        boolean dialogueWasOpen =
                dialogueBox.isOpen();

        if (dialogueWasOpen) {
            int selectedChoice =
                    dialogueBox.updateInput();

            if (selectedChoice == 0) {
                gameState.setTrainingApproach(
                        TrainingApproach.OBSERVE_FIRST
                );
            } else if (selectedChoice == 1) {
                gameState.setTrainingApproach(
                        TrainingApproach.TRUST_THE_FORCE
                );
            }
        } else if (handlePauseInput()) {
            return;
        }

        float physicsDelta =
                Math.min(deltaTime, 1f / 30f);

        if (!paused && !dialogueBox.isOpen()) {
        	if (levelComplete) {
        	    if (checkForRestart()) {
        	        return;
        	    }
        	} else {
        	    updateGame(physicsDelta);
        	}
        }

        ScreenUtils.clear(
                0.02f,
                0.03f,
                0.08f,
                1f
        );

        viewport.apply();
        updateCamera();

        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);

        drawBackgroundGrid();
        drawLevel();
        drawPlayer();
        drawHealthDisplay();
        
        if (checkpointMessageTimer > 0f
                && !paused
                && !dialogueBox.isOpen()) {

            drawCheckpointMessage();
        }

        if (!paused
                && !dialogueBox.isOpen()
                && !levelComplete
                && isPlayerNearTerminal()) {

            drawInteractionPrompt();
        }

        if (levelComplete) {
            drawCompletionMessage();
        }

        if (dialogueBox.isOpen()) {
            dialogueBox.render(
                    shapeRenderer,
                    spriteBatch,
                    font,
                    camera
            );
        }

        if (paused) {
            if (showingPauseControls) {
                drawPauseControls();
            } else {
                drawPauseMenu();
            }
        }
    }
    
    private boolean handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!paused) {
                paused = true;
                showingPauseControls = false;
                selectedPauseOption = PAUSE_RESUME_OPTION;
            } else if (showingPauseControls) {
                showingPauseControls = false;
            } else {
                paused = false;
            }

            return false;
        }

        if (!paused) {
            return false;
        }

        if (showingPauseControls) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(
                            Input.Keys.BACKSPACE)) {

                showingPauseControls = false;
            }

            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)) {

            selectedPauseOption =
                    (selectedPauseOption - 1
                    + PAUSE_OPTION_COUNT)
                    % PAUSE_OPTION_COUNT;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {

            selectedPauseOption =
                    (selectedPauseOption + 1)
                    % PAUSE_OPTION_COUNT;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            return activatePauseOption();
        }

        return false;
    }

    private boolean activatePauseOption() {
        if (selectedPauseOption == PAUSE_RESUME_OPTION) {
            paused = false;
            return false;
        }

        if (selectedPauseOption == PAUSE_CONTROLS_OPTION) {
            showingPauseControls = true;
            return false;
        }

        if (selectedPauseOption == PAUSE_MAIN_MENU_OPTION) {
            game.showMainMenu();
            return true;
        }

        return false;
    }

    private void updateGame(float deltaTime) {
    	player.update(
    	        deltaTime,
    	        LEVEL_WIDTH,
    	        platforms,
    	        oneWayPlatforms
    	);
        
        updateCheckpointAndFalling(deltaTime);
        trainingDroid.update(deltaTime);

        checkPlayerAttack();
        checkDroidContact();
        checkExitDoor();
        checkTerminalInteraction();

        if (!player.isAlive()) {
            player.respawn(
                    activeRespawnX,
                    activeRespawnY
            );
        }
    }
    
    private void updateCheckpointAndFalling(
            float deltaTime) {

        checkpointMessageTimer =
                Math.max(
                        0f,
                        checkpointMessageTimer - deltaTime
                );

        if (!checkpointActivated
                && player.getBounds().overlaps(checkpoint)) {

            checkpointActivated = true;

            activeRespawnX = CHECKPOINT_RESPAWN_X;
            activeRespawnY = CHECKPOINT_RESPAWN_Y;

            checkpointMessageTimer = 2f;
        }

        if (player.getBounds().y < FALL_RESPAWN_LIMIT) {
            player.respawn(
                    activeRespawnX,
                    activeRespawnY
            );
        }
    }
    
    private void checkTerminalInteraction() {
        if (!isPlayerNearTerminal()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            dialogueBox.open(
                    "TRAINING TERMINAL",
                    "How will you approach the next challenge?",
                    "Observe the situation before acting.",
                    "Strike quickly and trust the Force."
            );
        }
    }

    private boolean isPlayerNearTerminal() {
        return player.getBounds().overlaps(
                terminalInteractionArea
        );
    }

    private void checkPlayerAttack() {
        Rectangle attackBounds =
                player.getAttackBounds();

        if (attackBounds == null
                || !trainingDroid.isAlive()) {
            return;
        }

        if (attackBounds.overlaps(
                trainingDroid.getBounds())) {

            float playerCenterX =
                    player.getBounds().x
                    + player.getBounds().width / 2f;

            trainingDroid.takeDamage(
                    1,
                    playerCenterX
            );

            player.markAttackHit();
        }
    }

    private void checkDroidContact() {
        if (!trainingDroid.isAlive()) {
            return;
        }

        Rectangle droidBounds =
                trainingDroid.getBounds();

        if (player.getBounds().overlaps(droidBounds)) {
            float droidCenterX =
                    droidBounds.x
                    + droidBounds.width / 2f;

            player.takeDamage(
                    1,
                    droidCenterX,
                    LEVEL_WIDTH
            );
        }
    }

    private void checkExitDoor() {
        if (trainingDroid.isAlive()) {
            return;
        }

        if (player.getBounds().overlaps(exitDoor)) {
            levelComplete = true;
            gameState.completeTraining();
        }
    }

    private boolean checkForRestart() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.showTrainingDebrief();
            return true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetLevel();
        }

        return false;
    }

    private void resetLevel() {
        player.respawn(
                PLAYER_RESPAWN_X,
                PLAYER_RESPAWN_Y
        );
        
        activeRespawnX = PLAYER_RESPAWN_X;
        activeRespawnY = PLAYER_RESPAWN_Y;

        checkpointActivated = false;
        checkpointMessageTimer = 0f;

        trainingDroid.reset(
                DROID_START_X,
                DROID_START_Y
        );

        levelComplete = false;
        dialogueBox.close();
    }

    private void updateCamera() {
        Rectangle playerBounds =
                player.getBounds();

        float playerCenterX =
                playerBounds.x
                + playerBounds.width / 2f;

        float halfViewWidth =
                VIEW_WIDTH / 2f;

        camera.position.x = MathUtils.clamp(
                playerCenterX,
                halfViewWidth,
                LEVEL_WIDTH - halfViewWidth
        );

        camera.position.y = VIEW_HEIGHT / 2f;
        camera.update();
    }

    private void drawBackgroundGrid() {
        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
                0.08f,
                0.12f,
                0.20f,
                1f
        );

        for (float x = 0f;
                x <= LEVEL_WIDTH;
                x += 64f) {

            shapeRenderer.line(
                    x,
                    0f,
                    x,
                    VIEW_HEIGHT
            );
        }

        for (float y = 0f;
                y <= VIEW_HEIGHT;
                y += 64f) {

            shapeRenderer.line(
                    0f,
                    y,
                    LEVEL_WIDTH,
                    y
            );
        }

        shapeRenderer.end();
    }
    
    private void drawCheckpointMessage() {
        spriteBatch.begin();

        drawCenteredText(
                "CHECKPOINT REACHED",
                camera.position.x,
                VIEW_HEIGHT - 90f,
                1.4f,
                Color.CYAN
        );

        spriteBatch.end();
    }
    
    private void drawTrainingTerminal() {
        shapeRenderer.setColor(
                0.28f,
                0.32f,
                0.38f,
                1f
        );
        

        shapeRenderer.rect(
                trainingTerminal.x,
                trainingTerminal.y,
                trainingTerminal.width,
                trainingTerminal.height
        );

        shapeRenderer.setColor(
                0.12f,
                0.15f,
                0.20f,
                1f
        );

        shapeRenderer.rect(
                trainingTerminal.x + 6f,
                trainingTerminal.y + 35f,
                trainingTerminal.width - 12f,
                28f
        );

        TrainingApproach trainingApproach =
                gameState.getTrainingApproach();

        if (trainingApproach
                == TrainingApproach.OBSERVE_FIRST) {

            shapeRenderer.setColor(
                    0.20f,
                    1f,
                    0.40f,
                    1f
            );
        } else if (trainingApproach
                == TrainingApproach.TRUST_THE_FORCE) {

            shapeRenderer.setColor(
                    0.70f,
                    0.30f,
                    1f,
                    1f
            );
        } else {
            shapeRenderer.setColor(
                    0.20f,
                    0.70f,
                    1f,
                    1f
            );
        }

        shapeRenderer.rect(
                trainingTerminal.x + 11f,
                trainingTerminal.y + 43f,
                trainingTerminal.width - 22f,
                12f
        );
    }

    private void drawLevel() {
        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.22f,
                0.25f,
                0.32f,
                1f
        );

        for (Rectangle platform : platforms) {
            shapeRenderer.rect(
                    platform.x,
                    platform.y,
                    platform.width,
                    platform.height
            );
        }

        if (trainingDroid.isAlive()) {
            shapeRenderer.setColor(
                    0.65f,
                    0.12f,
                    0.12f,
                    1f
            );
        } else {
            shapeRenderer.setColor(
                    0.12f,
                    0.75f,
                    0.30f,
                    1f
            );
        }

        shapeRenderer.rect(
                exitDoor.x,
                exitDoor.y,
                exitDoor.width,
                exitDoor.height
        );
        
        shapeRenderer.setColor(
                0.20f,
                0.48f,
                0.65f,
                1f
        );

        for (Rectangle platform : oneWayPlatforms) {
            shapeRenderer.rect(
                    platform.x,
                    platform.y,
                    platform.width,
                    platform.height
            );
        }

        drawTrainingTerminal();
        drawCheckpoint();
        trainingDroid.render(shapeRenderer);

        shapeRenderer.end();
    }
    
    private void drawCheckpoint() {
        // Checkpoint stand.
        shapeRenderer.setColor(
                0.30f,
                0.34f,
                0.40f,
                1f
        );

        shapeRenderer.rect(
                checkpoint.x + 16f,
                checkpoint.y,
                8f,
                checkpoint.height - 20f
        );

        // Checkpoint base.
        shapeRenderer.rect(
                checkpoint.x + 5f,
                checkpoint.y,
                checkpoint.width - 10f,
                12f
        );

        if (checkpointActivated) {
            shapeRenderer.setColor(
                    0.20f,
                    1f,
                    0.40f,
                    1f
            );
        } else {
            shapeRenderer.setColor(
                    0.20f,
                    0.70f,
                    1f,
                    1f
            );
        }

        shapeRenderer.circle(
                checkpoint.x + checkpoint.width / 2f,
                checkpoint.y + checkpoint.height - 12f,
                12f
        );
    }

    private void drawPlayer() {
        spriteBatch.begin();
        player.render(spriteBatch);
        spriteBatch.end();
    }
    
    private void drawInteractionPrompt() {
        spriteBatch.begin();

        drawCenteredText(
                "Press E to use the training terminal",
                camera.position.x,
                120f,
                1.1f,
                Color.CYAN
        );

        spriteBatch.end();
    }

    private void drawHealthDisplay() {
        float displayX =
                camera.position.x
                - VIEW_WIDTH / 2f
                + 30f;

        float displayY =
                VIEW_HEIGHT - 45f;

        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.18f,
                0.18f,
                0.20f,
                1f
        );

        for (int i = 0;
                i < player.getMaximumHealth();
                i++) {

            shapeRenderer.rect(
                    displayX + i * 35f,
                    displayY,
                    28f,
                    18f
            );
        }

        shapeRenderer.setColor(
                0.85f,
                0.12f,
                0.12f,
                1f
        );

        for (int i = 0;
                i < player.getHealth();
                i++) {

            shapeRenderer.rect(
                    displayX + i * 35f,
                    displayY,
                    28f,
                    18f
            );
        }

        shapeRenderer.end();
    }

    private void drawCompletionMessage() {
        float panelWidth = 640f;
        float panelHeight = 180f;

        float panelX =
                camera.position.x
                - panelWidth / 2f;

        float panelY =
                camera.position.y
                - panelHeight / 2f;

        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.03f,
                0.04f,
                0.08f,
                1f
        );

        shapeRenderer.rect(
                panelX,
                panelY,
                panelWidth,
                panelHeight
        );

        shapeRenderer.end();

        spriteBatch.begin();

        font.setColor(Color.GOLD);
        font.getData().setScale(2f);

        font.getData().setScale(1.05f);

        glyphLayout.setText(
                font,
                "Press Enter to continue"
        );

        font.draw(
                spriteBatch,
                glyphLayout,
                camera.position.x
                        - glyphLayout.width / 2f,
                panelY + 75f
        );

        glyphLayout.setText(
                font,
                "Press R to replay"
        );

        font.draw(
                spriteBatch,
                glyphLayout,
                camera.position.x
                        - glyphLayout.width / 2f,
                panelY + 42f
        );

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        glyphLayout.setText(
                font,
                "Press R to restart"
        );

        font.draw(
                spriteBatch,
                glyphLayout,
                camera.position.x
                        - glyphLayout.width / 2f,
                panelY + 60f
        );

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);

        spriteBatch.end();
    }
    
    private void drawPauseMenu() {
        float panelWidth = 620f;
        float panelHeight = 520f;

        float panelX =
                camera.position.x - panelWidth / 2f;

        float panelY =
                camera.position.y - panelHeight / 2f;

        float buttonWidth = 400f;
        float buttonHeight = 60f;

        float buttonX =
                camera.position.x - buttonWidth / 2f;

        float[] buttonYPositions = {
                camera.position.y + 30f,
                camera.position.y - 60f,
                camera.position.y - 150f
        };

        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.02f,
                0.04f,
                0.09f,
                1f
        );

        shapeRenderer.rect(
                panelX,
                panelY,
                panelWidth,
                panelHeight
        );

        for (int i = 0; i < PAUSE_OPTION_COUNT; i++) {
            if (i == selectedPauseOption) {
                shapeRenderer.setColor(
                        0.16f,
                        0.55f,
                        0.85f,
                        1f
                );
            } else {
                shapeRenderer.setColor(
                        0.10f,
                        0.15f,
                        0.25f,
                        1f
                );
            }

            shapeRenderer.rect(
                    buttonX,
                    buttonYPositions[i],
                    buttonWidth,
                    buttonHeight
            );
        }

        shapeRenderer.end();

        spriteBatch.begin();

        drawCenteredText(
                "GAME PAUSED",
                camera.position.x,
                camera.position.y + 205f,
                2.5f,
                Color.GOLD
        );

        drawCenteredText(
                "RESUME",
                camera.position.x,
                buttonYPositions[PAUSE_RESUME_OPTION] + 40f,
                1.4f,
                Color.WHITE
        );

        drawCenteredText(
                "CONTROLS",
                camera.position.x,
                buttonYPositions[PAUSE_CONTROLS_OPTION] + 40f,
                1.4f,
                Color.WHITE
        );

        drawCenteredText(
                "RETURN TO MAIN MENU",
                camera.position.x,
                buttonYPositions[PAUSE_MAIN_MENU_OPTION] + 40f,
                1.4f,
                Color.WHITE
        );

        drawCenteredText(
                "Use W/S or arrows and press Enter",
                camera.position.x,
                panelY + 45f,
                1f,
                Color.LIGHT_GRAY
        );

        spriteBatch.end();
    }

    private void drawPauseControls() {
        float panelWidth = 620f;
        float panelHeight = 520f;

        float panelX =
                camera.position.x - panelWidth / 2f;

        float panelY =
                camera.position.y - panelHeight / 2f;

        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.02f,
                0.04f,
                0.09f,
                1f
        );

        shapeRenderer.rect(
                panelX,
                panelY,
                panelWidth,
                panelHeight
        );

        shapeRenderer.end();

        spriteBatch.begin();

        drawCenteredText(
                "CONTROLS",
                camera.position.x,
                camera.position.y + 205f,
                2.5f,
                Color.GOLD
        );

        drawCenteredText(
                "Move: A / D or Left / Right",
                camera.position.x,
                camera.position.y + 110f,
                1.3f,
                Color.WHITE
        );

        drawCenteredText(
                "Jump: Space, W, or Up",
                camera.position.x,
                camera.position.y + 45f,
                1.3f,
                Color.WHITE
        );

        drawCenteredText(
                "Lightsaber: J or Left Mouse",
                camera.position.x,
                camera.position.y - 20f,
                1.3f,
                Color.WHITE
        );

        drawCenteredText(
                "Pause: Escape",
                camera.position.x,
                camera.position.y - 85f,
                1.3f,
                Color.WHITE
        );

        drawCenteredText(
                "Press Escape, Enter, or Backspace to return",
                camera.position.x,
                panelY + 55f,
                1f,
                Color.LIGHT_GRAY
        );

        spriteBatch.end();
    }

    private void drawCenteredText(
            String text,
            float centerX,
            float y,
            float scale,
            Color color) {

        font.getData().setScale(scale);
        font.setColor(color);

        glyphLayout.setText(font, text);

        font.draw(
                spriteBatch,
                glyphLayout,
                centerX - glyphLayout.width / 2f,
                y
        );

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(
                width,
                height,
                true
        );
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        player.dispose();
        font.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }
}
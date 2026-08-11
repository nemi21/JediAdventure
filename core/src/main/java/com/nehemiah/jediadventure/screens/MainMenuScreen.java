package com.nehemiah.jediadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nehemiah.jediadventure.JediAdventure;

public class MainMenuScreen implements Screen {

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;

    private static final int START_OPTION = 0;
    private static final int CONTROLS_OPTION = 1;
    private static final int EXIT_OPTION = 2;
    private static final int OPTION_COUNT = 3;

    private final JediAdventure game;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    private final Rectangle[] menuButtons;
    private final Rectangle backButton;
    private final Vector2 mousePosition;

    private int selectedOption;
    private boolean showingControls;

    public MainMenuScreen(JediAdventure game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(
                VIEW_WIDTH,
                VIEW_HEIGHT,
                camera
        );

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();

        menuButtons = new Rectangle[] {
                new Rectangle(440f, 350f, 400f, 64f),
                new Rectangle(440f, 255f, 400f, 64f),
                new Rectangle(440f, 160f, 400f, 64f)
        };

        backButton = new Rectangle(
                490f,
                80f,
                300f,
                60f
        );

        mousePosition = new Vector2();

        selectedOption = START_OPTION;
        showingControls = false;
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
        if (handleInput()) {
            return;
        }

        ScreenUtils.clear(
                0.01f,
                0.02f,
                0.06f,
                1f
        );

        viewport.apply();

        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);

        drawBackground();

        if (showingControls) {
            drawControls();
        } else {
            drawMainMenu();
        }
    }

    private boolean handleInput() {
        updateMousePosition();

        if (showingControls) {
            return handleControlsInput();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)) {

            selectedOption =
                    (selectedOption - 1 + OPTION_COUNT)
                    % OPTION_COUNT;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {

            selectedOption =
                    (selectedOption + 1)
                    % OPTION_COUNT;
        }

        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i].contains(mousePosition)) {
                selectedOption = i;

                if (Gdx.input.isButtonJustPressed(
                        Input.Buttons.LEFT)) {

                    return activateSelectedOption();
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            return activateSelectedOption();
        }

        return false;
    }

    private boolean handleControlsInput() {
        boolean returnPressed =
                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);

        boolean backClicked =
                backButton.contains(mousePosition)
                && Gdx.input.isButtonJustPressed(
                        Input.Buttons.LEFT
                );

        if (returnPressed || backClicked) {
            showingControls = false;
        }

        return false;
    }

    private boolean activateSelectedOption() {
        if (selectedOption == START_OPTION) {
            game.startGame();
            return true;
        }

        if (selectedOption == CONTROLS_OPTION) {
            showingControls = true;
            return false;
        }

        if (selectedOption == EXIT_OPTION) {
            Gdx.app.exit();
            return true;
        }

        return false;
    }

    private void updateMousePosition() {
        mousePosition.set(
                Gdx.input.getX(),
                Gdx.input.getY()
        );

        viewport.unproject(mousePosition);
    }

    private void drawBackground() {
        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.75f,
                0.85f,
                1f,
                1f
        );

        for (int i = 0; i < 70; i++) {
            float starX =
                    (i * 179f + 83f) % VIEW_WIDTH;

            float starY =
                    (i * 97f + 41f) % VIEW_HEIGHT;

            float starSize =
                    1f + i % 3;

            shapeRenderer.circle(
                    starX,
                    starY,
                    starSize
            );
        }

        shapeRenderer.end();
    }

    private void drawMainMenu() {
        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        for (int i = 0; i < menuButtons.length; i++) {
            Rectangle button = menuButtons[i];

            if (i == selectedOption) {
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
                    button.x,
                    button.y,
                    button.width,
                    button.height
            );
        }

        shapeRenderer.end();

        spriteBatch.begin();

        drawCenteredText(
                "JEDI ADVENTURE",
                VIEW_WIDTH / 2f,
                590f,
                3f,
                Color.GOLD
        );

        drawCenteredText(
                "A New Journey Begins",
                VIEW_WIDTH / 2f,
                525f,
                1.3f,
                Color.LIGHT_GRAY
        );

        drawButtonText(
                "START GAME",
                menuButtons[START_OPTION]
        );

        drawButtonText(
                "CONTROLS",
                menuButtons[CONTROLS_OPTION]
        );

        drawButtonText(
                "EXIT GAME",
                menuButtons[EXIT_OPTION]
        );

        drawCenteredText(
                "Use W/S, arrow keys, mouse, or Enter",
                VIEW_WIDTH / 2f,
                80f,
                1f,
                Color.LIGHT_GRAY
        );

        spriteBatch.end();
    }

    private void drawControls() {
        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.04f,
                0.07f,
                0.13f,
                1f
        );

        shapeRenderer.rect(
                290f,
                160f,
                700f,
                430f
        );

        boolean backSelected =
                backButton.contains(mousePosition);

        if (backSelected) {
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
                backButton.x,
                backButton.y,
                backButton.width,
                backButton.height
        );

        shapeRenderer.end();

        spriteBatch.begin();

        drawCenteredText(
                "CONTROLS",
                VIEW_WIDTH / 2f,
                640f,
                2.5f,
                Color.GOLD
        );

        drawCenteredText(
                "Move: A / D or Left / Right",
                VIEW_WIDTH / 2f,
                515f,
                1.35f,
                Color.WHITE
        );

        drawCenteredText(
                "Jump: Space, W, or Up",
                VIEW_WIDTH / 2f,
                450f,
                1.35f,
                Color.WHITE
        );

        drawCenteredText(
                "Lightsaber: J or Left Mouse",
                VIEW_WIDTH / 2f,
                385f,
                1.35f,
                Color.WHITE
        );

        drawCenteredText(
                "Return to Menu: Escape",
                VIEW_WIDTH / 2f,
                320f,
                1.35f,
                Color.WHITE
        );

        drawCenteredText(
                "Defeat the droid to unlock the exit",
                VIEW_WIDTH / 2f,
                235f,
                1.15f,
                Color.LIGHT_GRAY
        );

        drawCenteredText(
                "BACK",
                backButton.x + backButton.width / 2f,
                backButton.y + 40f,
                1.4f,
                Color.WHITE
        );

        spriteBatch.end();
    }

    private void drawButtonText(
            String text,
            Rectangle button) {

        drawCenteredText(
                text,
                button.x + button.width / 2f,
                button.y + 42f,
                1.5f,
                Color.WHITE
        );
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
        font.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }
}
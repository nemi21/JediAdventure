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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nehemiah.jediadventure.JediAdventure;
import com.nehemiah.jediadventure.state.GameState;
import com.nehemiah.jediadventure.state.GameState.TrainingApproach;

public class TrainingDebriefScreen implements Screen {

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;

    private final JediAdventure game;
    private final GameState gameState;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    public TrainingDebriefScreen(
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

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();
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
    	if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
    	    game.showOpeningBriefing();
    	    return;
    	}

    	if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
    	    game.showMainMenu();
    	    return;
    	}

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.replayTraining();
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
        drawDebrief();
    }

    private void drawBackground() {
        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.70f,
                0.82f,
                1f,
                1f
        );

        for (int i = 0; i < 75; i++) {
            float starX =
                    (i * 181f + 47f) % VIEW_WIDTH;

            float starY =
                    (i * 103f + 29f) % VIEW_HEIGHT;

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

    private void drawDebrief() {
        float panelX = 190f;
        float panelY = 105f;
        float panelWidth = 900f;
        float panelHeight = 510f;

        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
                0.03f,
                0.05f,
                0.11f,
                1f
        );

        shapeRenderer.rect(
                panelX,
                panelY,
                panelWidth,
                panelHeight
        );

        shapeRenderer.end();

        TrainingApproach approach =
                gameState.getTrainingApproach();

        String approachName;
        String resultMessage;
        Color approachColor;

        if (approach == TrainingApproach.OBSERVE_FIRST) {
            approachName = "OBSERVE FIRST";

            resultMessage =
                    "The terminal records a patient and analytical "
                    + "approach. Luke studies the situation before "
                    + "committing himself to action.";

            approachColor = Color.GREEN;
        } else if (approach
                == TrainingApproach.TRUST_THE_FORCE) {

            approachName = "TRUST THE FORCE";

            resultMessage =
                    "The terminal records a decisive and instinctive "
                    + "approach. Luke acts quickly and places his "
                    + "confidence in the guidance of the Force.";

            approachColor = Color.PURPLE;
        } else {
            approachName = "NO APPROACH RECORDED";

            resultMessage =
                    "Luke completed the training without recording "
                    + "an answer at the terminal. Some decisions can "
                    + "be avoided, but that may also have consequences.";

            approachColor = Color.LIGHT_GRAY;
        }

        spriteBatch.begin();

        drawCenteredText(
                "TRAINING DEBRIEF",
                VIEW_WIDTH / 2f,
                570f,
                2.6f,
                Color.GOLD
        );

        drawCenteredText(
                "Recorded approach",
                VIEW_WIDTH / 2f,
                505f,
                1.1f,
                Color.LIGHT_GRAY
        );

        drawCenteredText(
                approachName,
                VIEW_WIDTH / 2f,
                455f,
                1.6f,
                approachColor
        );

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);

        font.draw(
                spriteBatch,
                resultMessage,
                260f,
                385f,
                760f,
                Align.center,
                true
        );

        String completionText =
                gameState.isTrainingCompleted()
                ? "Training status: Complete"
                : "Training status: Incomplete";

        drawCenteredText(
                completionText,
                VIEW_WIDTH / 2f,
                270f,
                1.15f,
                Color.CYAN
        );

        drawCenteredText(
                "Press R to replay the training",
                VIEW_WIDTH / 2f,
                190f,
                1f,
                Color.LIGHT_GRAY
        );

        drawCenteredText(
        		"Press Enter to continue or Escape for the main menu",
                VIEW_WIDTH / 2f,
                150f,
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
        font.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }
}
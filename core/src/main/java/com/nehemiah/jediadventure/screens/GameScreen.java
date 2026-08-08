package com.nehemiah.jediadventure.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.nehemiah.jediadventure.entities.Player;
import com.nehemiah.jediadventure.entities.TrainingDroid;

public class GameScreen implements Screen {

    public static final float VIEW_WIDTH = 1280f;
    public static final float VIEW_HEIGHT = 720f;
    public static final float LEVEL_WIDTH = 3200f;

    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final ShapeRenderer shapeRenderer;

    private final Player player;
    private final TrainingDroid trainingDroid;

    private final List<Rectangle> platforms;
    private final Rectangle exitDoor;

    public GameScreen() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();

        platforms = new ArrayList<>();

        platforms.add(new Rectangle(0f, 0f, LEVEL_WIDTH, 64f));

        platforms.add(new Rectangle(220f, 160f, 240f, 32f));
        platforms.add(new Rectangle(560f, 280f, 220f, 32f));
        platforms.add(new Rectangle(900f, 180f, 220f, 32f));

        platforms.add(new Rectangle(1400f, 140f, 220f, 32f));
        platforms.add(new Rectangle(1650f, 250f, 220f, 32f));
        platforms.add(new Rectangle(1950f, 360f, 220f, 32f));
        platforms.add(new Rectangle(2300f, 250f, 220f, 32f));
        platforms.add(new Rectangle(2600f, 140f, 220f, 32f));

        player = new Player(100f, 64f);
        trainingDroid = new TrainingDroid(760f, 64f);

        exitDoor = new Rectangle(3100f, 64f, 48f, 100f);
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
        float physicsDelta = Math.min(deltaTime, 1f / 30f);

        player.update(physicsDelta, LEVEL_WIDTH, platforms);
        trainingDroid.update(physicsDelta);

        checkPlayerAttack();

        ScreenUtils.clear(0.02f, 0.03f, 0.08f, 1f);

        viewport.apply();
        updateCamera();

        shapeRenderer.setProjectionMatrix(camera.combined);

        drawBackgroundGrid();
        drawLevel();
    }

    private void checkPlayerAttack() {
        Rectangle attackBounds = player.getAttackBounds();

        if (attackBounds == null || !trainingDroid.isAlive()) {
            return;
        }

        if (attackBounds.overlaps(trainingDroid.getBounds())) {
            trainingDroid.takeDamage(1);
        }
    }

    private void updateCamera() {
        Rectangle playerBounds = player.getBounds();

        float playerCenterX =
                playerBounds.x + playerBounds.width / 2f;

        float halfViewWidth = VIEW_WIDTH / 2f;

        camera.position.x = MathUtils.clamp(
                playerCenterX,
                halfViewWidth,
                LEVEL_WIDTH - halfViewWidth
        );

        camera.position.y = VIEW_HEIGHT / 2f;
        camera.update();
    }

    private void drawBackgroundGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.08f, 0.12f, 0.20f, 1f);

        for (float x = 0f; x <= LEVEL_WIDTH; x += 64f) {
            shapeRenderer.line(x, 0f, x, VIEW_HEIGHT);
        }

        for (float y = 0f; y <= VIEW_HEIGHT; y += 64f) {
            shapeRenderer.line(0f, y, LEVEL_WIDTH, y);
        }

        shapeRenderer.end();
    }

    private void drawLevel() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.22f, 0.25f, 0.32f, 1f);

        for (Rectangle platform : platforms) {
            shapeRenderer.rect(
                    platform.x,
                    platform.y,
                    platform.width,
                    platform.height
            );
        }

        shapeRenderer.setColor(0.65f, 0.12f, 0.12f, 1f);
        shapeRenderer.rect(
                exitDoor.x,
                exitDoor.y,
                exitDoor.width,
                exitDoor.height
        );

        trainingDroid.render(shapeRenderer);
        player.render(shapeRenderer);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        shapeRenderer.dispose();
    }
}
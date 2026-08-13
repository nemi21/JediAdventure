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

public class OpeningBriefingScreen implements Screen {

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;

    private final JediAdventure game;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    private final String[] pageTitles;
    private final String[] speakers;
    private final String[] pageMessages;

    private int currentPage;

    public OpeningBriefingScreen(
            JediAdventure game,
            GameState gameState) {

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

        pageTitles = new String[] {
                "THE GALAXY AFTER ENDOR",
                "A VOICE IN THE FORCE",
                "THE IMPERIAL CLAIMANT",
                "CHAPTER ONE"
        };

        speakers = new String[] {
                "GALACTIC SITUATION",
                "OBI-WAN KENOBI",
                "NEW REPUBLIC INTELLIGENCE",
                "MISSION BRIEFING"
        };

        pageMessages = new String[] {
                buildGalacticSituation(),
                buildObiWanMessage(gameState),
                buildImperialMessage(),
                buildMissionMessage()
        };

        currentPage = 0;
    }

    private String buildGalacticSituation() {
        return "Six months have passed since the Battle of Endor. "
                + "The Emperor and Darth Vader are dead, but the "
                + "Galactic Empire has not fallen. Imperial admirals, "
                + "governors, and warlords now fight for territory "
                + "and the right to inherit the throne. Meanwhile, "
                + "the young New Republic struggles to protect the "
                + "worlds that have declared their freedom.";
    }

    private String buildObiWanMessage(
            GameState gameState) {

        String message =
                "Luke, before the Purge, a small Jedi sanctuary "
                + "was hidden beyond the recognized frontier. "
                + "I cannot tell you what remains there. I know "
                + "only that the Force still gathers around that "
                + "place.";

        TrainingApproach approach =
                gameState.getTrainingApproach();

        if (approach
                == TrainingApproach.OBSERVE_FIRST) {

            message +=
                    "\n\nYour willingness to observe before acting "
                    + "will serve you. But remember: patience must "
                    + "not become hesitation.";

        } else if (approach
                == TrainingApproach.TRUST_THE_FORCE) {

            message +=
                    "\n\nTrust the Force, Luke, but listen carefully. "
                    + "Instinct and certainty are not always the "
                    + "same thing.";

        } else {
            message +=
                    "\n\nThe road ahead will demand choices that "
                    + "cannot always be postponed.";
        }

        return message;
    }

    private String buildImperialMessage() {
        return "New Republic scouts have detected Imperial forces "
                + "entering the same sector. They serve Grand Moff "
                + "Rethan Veyr, an ambitious warlord building his "
                + "own claim to the Imperial throne. Veyr believes "
                + "that capturing or killing Luke Skywalker will "
                + "prove that he has avenged both the Emperor and "
                + "Darth Vader.";
    }

    private String buildMissionMessage() {
        return "Travel to the forgotten world with R2-D2. Locate "
                + "the Jedi sanctuary and discover what survived "
                + "the Purge. Prevent Veyr's forces from claiming "
                + "its knowledge, but remember that the people of "
                + "the planet may already be trapped between the "
                + "Imperial factions.\n\n"
                + "The search for the lost sanctuary begins.";
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
        if (Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE)) {

            game.showMainMenu();
            return;
        }

        boolean previousPressed =
                Gdx.input.isKeyJustPressed(
                        Input.Keys.LEFT)
                || Gdx.input.isKeyJustPressed(
                        Input.Keys.BACKSPACE);

        if (previousPressed && currentPage > 0) {
            currentPage--;
        }

        boolean continuePressed =
                Gdx.input.isKeyJustPressed(
                        Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(
                        Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(
                        Input.Keys.RIGHT);

        if (continuePressed) {
            if (currentPage < pageMessages.length - 1) {
                currentPage++;
            } else {
                // Temporary destination until Mission One exists.
                game.showMainMenu();
                return;
            }
        }

        ScreenUtils.clear(
                0.005f,
                0.01f,
                0.035f,
                1f
        );

        viewport.apply();

        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawBriefing();
    }

    private void drawBackground() {
        shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
        );

        for (int i = 0; i < 90; i++) {
            float starX =
                    (i * 197f + 61f) % VIEW_WIDTH;

            float starY =
                    (i * 109f + 37f) % VIEW_HEIGHT;

            float starSize =
                    1f + i % 3;

            shapeRenderer.setColor(
                    0.65f,
                    0.78f,
                    1f,
                    1f
            );

            shapeRenderer.circle(
                    starX,
                    starY,
                    starSize
            );
        }

        shapeRenderer.setColor(
                0.03f,
                0.06f,
                0.13f,
                1f
        );

        shapeRenderer.rect(
                150f,
                85f,
                980f,
                550f
        );

        shapeRenderer.setColor(
                0.12f,
                0.42f,
                0.68f,
                1f
        );

        shapeRenderer.rect(
                150f,
                625f,
                980f,
                10f
        );

        shapeRenderer.end();
    }

    private void drawBriefing() {
        spriteBatch.begin();

        drawCenteredText(
                pageTitles[currentPage],
                VIEW_WIDTH / 2f,
                575f,
                2.2f,
                Color.GOLD
        );

        drawCenteredText(
                speakers[currentPage],
                VIEW_WIDTH / 2f,
                505f,
                1.1f,
                Color.CYAN
        );

        font.getData().setScale(1.25f);
        font.setColor(Color.WHITE);

        font.draw(
                spriteBatch,
                pageMessages[currentPage],
                230f,
                440f,
                820f,
                Align.center,
                true
        );

        String pageNumber =
                "Page "
                + (currentPage + 1)
                + " of "
                + pageMessages.length;

        drawCenteredText(
                pageNumber,
                VIEW_WIDTH / 2f,
                155f,
                0.9f,
                Color.LIGHT_GRAY
        );

        String continueText;

        if (currentPage
                == pageMessages.length - 1) {

            continueText =
                    "Enter: Finish briefing"
                    + "    Backspace: Previous"
                    + "    Escape: Main Menu";
        } else {
            continueText =
                    "Enter: Continue"
                    + "    Backspace: Previous"
                    + "    Escape: Main Menu";
        }

        drawCenteredText(
                continueText,
                VIEW_WIDTH / 2f,
                115f,
                0.9f,
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

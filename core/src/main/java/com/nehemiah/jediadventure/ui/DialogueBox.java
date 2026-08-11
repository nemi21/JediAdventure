package com.nehemiah.jediadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

public class DialogueBox {

    private static final float PANEL_WIDTH = 1120f;
    private static final float PANEL_HEIGHT = 300f;

    private boolean open;

    private String speaker;
    private String message;
    private String[] choices;

    private int selectedChoice;

    public DialogueBox() {
        open = false;
        speaker = "";
        message = "";
        choices = new String[0];
        selectedChoice = 0;
    }

    public void open(
            String speaker,
            String message,
            String... choices) {

        this.speaker = speaker;
        this.message = message;
        this.choices = choices.clone();

        selectedChoice = 0;
        open = true;
    }

    public int updateInput() {
        if (!open) {
            return -1;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return -1;
        }

        if (choices.length == 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                close();
            }

            return -1;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)) {

            selectedChoice =
                    (selectedChoice - 1 + choices.length)
                    % choices.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {

            selectedChoice =
                    (selectedChoice + 1)
                    % choices.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            int result = selectedChoice;
            close();
            return result;
        }

        return -1;
    }

    public void render(
            ShapeRenderer shapeRenderer,
            SpriteBatch spriteBatch,
            BitmapFont font,
            OrthographicCamera camera) {

        if (!open) {
            return;
        }

        float panelX =
                camera.position.x - PANEL_WIDTH / 2f;

        float panelY = 20f;

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
                PANEL_WIDTH,
                PANEL_HEIGHT
        );

        for (int i = 0; i < choices.length; i++) {
            float optionY =
                    panelY + 115f - i * 58f;

            if (i == selectedChoice) {
                shapeRenderer.setColor(
                        0.16f,
                        0.55f,
                        0.85f,
                        1f
                );
            } else {
                shapeRenderer.setColor(
                        0.08f,
                        0.12f,
                        0.20f,
                        1f
                );
            }

            shapeRenderer.rect(
                    panelX + 40f,
                    optionY,
                    PANEL_WIDTH - 80f,
                    46f
            );
        }

        shapeRenderer.end();

        spriteBatch.begin();

        font.setColor(Color.GOLD);
        font.getData().setScale(1.35f);

        font.draw(
                spriteBatch,
                speaker,
                panelX + 40f,
                panelY + 270f
        );

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.9f);

        font.draw(
                spriteBatch,
                "W/S: Choose    Enter: Select    Escape: Close",
                panelX + 450f,
                panelY + 270f,
                PANEL_WIDTH - 490f,
                Align.right,
                false
        );

        font.setColor(Color.WHITE);
        font.getData().setScale(1.1f);

        font.draw(
                spriteBatch,
                message,
                panelX + 40f,
                panelY + 220f,
                PANEL_WIDTH - 80f,
                Align.left,
                true
        );

        for (int i = 0; i < choices.length; i++) {
            float optionY =
                    panelY + 115f - i * 58f;

            if (i == selectedChoice) {
                font.setColor(Color.GOLD);
            } else {
                font.setColor(Color.WHITE);
            }

            font.draw(
                    spriteBatch,
                    (i + 1) + ". " + choices[i],
                    panelX + 60f,
                    optionY + 31f
            );
        }

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);

        spriteBatch.end();
    }

    public boolean isOpen() {
        return open;
    }

    public void close() {
        open = false;
    }
}
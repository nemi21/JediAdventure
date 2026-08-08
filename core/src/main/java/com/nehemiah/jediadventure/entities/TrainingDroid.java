package com.nehemiah.jediadventure.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class TrainingDroid {

    private static final float WIDTH = 40f;
    private static final float HEIGHT = 56f;
    private static final float DAMAGE_FLASH_DURATION = 0.20f;

    private final Rectangle bounds;

    private int health;
    private float damageFlashTimer;
    private boolean alive;

    public TrainingDroid(float startingX, float startingY) {
        bounds = new Rectangle(
                startingX,
                startingY,
                WIDTH,
                HEIGHT
        );

        health = 3;
        alive = true;
        damageFlashTimer = 0f;
    }

    public void update(float deltaTime) {
        damageFlashTimer =
                Math.max(0f, damageFlashTimer - deltaTime);
    }

    public void takeDamage(int damage) {
        if (!alive || damageFlashTimer > 0f) {
            return;
        }

        health -= damage;
        damageFlashTimer = DAMAGE_FLASH_DURATION;

        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!alive) {
            return;
        }

        if (damageFlashTimer > 0f) {
            shapeRenderer.setColor(1f, 0.20f, 0.20f, 1f);
        } else {
            shapeRenderer.setColor(0.55f, 0.58f, 0.65f, 1f);
        }

        // Droid body.
        shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height - 12f
        );

        // Droid head.
        shapeRenderer.circle(
                bounds.x + bounds.width / 2f,
                bounds.y + bounds.height - 8f,
                12f
        );

        // Red sensor.
        shapeRenderer.setColor(1f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(
                bounds.x + 12f,
                bounds.y + bounds.height - 11f,
                16f,
                5f
        );

        // Health markers.
        shapeRenderer.setColor(0.20f, 1f, 0.35f, 1f);

        for (int i = 0; i < health; i++) {
            shapeRenderer.rect(
                    bounds.x + i * 13f,
                    bounds.y + bounds.height + 12f,
                    10f,
                    5f
            );
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return alive;
    }
}
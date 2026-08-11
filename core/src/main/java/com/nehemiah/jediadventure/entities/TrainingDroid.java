package com.nehemiah.jediadventure.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class TrainingDroid {

    private static final float WIDTH = 40f;
    private static final float HEIGHT = 56f;

    private static final float PATROL_SPEED = 110f;

    private static final float DAMAGE_FLASH_DURATION = 0.20f;
    private static final float HIT_STUN_DURATION = 0.14f;
    private static final float KNOCKBACK_DISTANCE = 55f;

    private final Rectangle bounds;
    private final float patrolMinimumX;
    private final float patrolMaximumX;
    
    private static final int MAX_HEALTH = 3;

    private int health;

    private float patrolDirection;
    private float damageFlashTimer;
    private float hitStunTimer;

    private boolean alive;

    public TrainingDroid(
            float startingX,
            float startingY,
            float patrolMinimumX,
            float patrolMaximumX) {

        bounds = new Rectangle(
                startingX,
                startingY,
                WIDTH,
                HEIGHT
        );

        this.patrolMinimumX = patrolMinimumX;
        this.patrolMaximumX = patrolMaximumX;

        health = MAX_HEALTH ;
        patrolDirection = 1f;
        damageFlashTimer = 0f;
        hitStunTimer = 0f;
        alive = true;
    }

    public void update(float deltaTime) {
        if (!alive) {
            return;
        }

        damageFlashTimer =
                Math.max(0f, damageFlashTimer - deltaTime);

        hitStunTimer =
                Math.max(0f, hitStunTimer - deltaTime);

        // The droid temporarily stops moving after being hit.
        if (hitStunTimer > 0f) {
            return;
        }

        bounds.x += patrolDirection * PATROL_SPEED * deltaTime;

        if (bounds.x <= patrolMinimumX) {
            bounds.x = patrolMinimumX;
            patrolDirection = 1f;
        } else if (bounds.x >= patrolMaximumX) {
            bounds.x = patrolMaximumX;
            patrolDirection = -1f;
        }
    }

    public void takeDamage(
            int damage,
            float attackerCenterX) {

        if (!alive || damageFlashTimer > 0f) {
            return;
        }

        health -= damage;
        damageFlashTimer = DAMAGE_FLASH_DURATION;
        hitStunTimer = HIT_STUN_DURATION;

        float droidCenterX =
                bounds.x + bounds.width / 2f;

        float knockbackDirection;

        if (attackerCenterX < droidCenterX) {
            knockbackDirection = 1f;
        } else {
            knockbackDirection = -1f;
        }

        bounds.x += knockbackDirection * KNOCKBACK_DISTANCE;

        bounds.x = MathUtils.clamp(
                bounds.x,
                patrolMinimumX,
                patrolMaximumX
        );

        // After being hit, the droid begins moving away from Luke.
        patrolDirection = knockbackDirection;

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
            shapeRenderer.setColor(
                    1f,
                    0.20f,
                    0.20f,
                    1f
            );
        } else {
            shapeRenderer.setColor(
                    0.55f,
                    0.58f,
                    0.65f,
                    1f
            );
        }

        shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height - 12f
        );

        shapeRenderer.circle(
                bounds.x + bounds.width / 2f,
                bounds.y + bounds.height - 8f,
                12f
        );

        shapeRenderer.setColor(
                1f,
                0.05f,
                0.05f,
                1f
        );

        shapeRenderer.rect(
                bounds.x + 12f,
                bounds.y + bounds.height - 11f,
                16f,
                5f
        );

        shapeRenderer.setColor(
                0.20f,
                1f,
                0.35f,
                1f
        );

        for (int i = 0; i < health; i++) {
            shapeRenderer.rect(
                    bounds.x + i * 13f,
                    bounds.y + bounds.height + 12f,
                    10f,
                    5f
            );
        }
    }
    
    public void reset(float startingX, float startingY) {
        bounds.setPosition(startingX, startingY);

        health = MAX_HEALTH;
        patrolDirection = 1f;
        damageFlashTimer = 0f;
        hitStunTimer = 0f;
        alive = true;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return alive;
    }
}
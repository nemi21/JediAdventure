package com.nehemiah.jediadventure.entities;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    private static final float WIDTH = 32f;
    private static final float HEIGHT = 56f;

    private static final float RUN_SPEED = 300f;
    private static final float JUMP_SPEED = 700f;
    private static final float GRAVITY = -1800f;
    private static final float MAX_FALL_SPEED = -1000f;

    private static final float COYOTE_DURATION = 0.12f;
    private static final float JUMP_BUFFER_DURATION = 0.12f;

    private static final float ATTACK_DURATION = 0.18f;
    private static final float ATTACK_COOLDOWN = 0.32f;
    private static final float ATTACK_RANGE = 54f;
    private static final float ATTACK_HEIGHT = 30f;

    private static final int MAX_HEALTH = 5;
    private static final float INVULNERABILITY_DURATION = 1f;
    private static final float DAMAGE_KNOCKBACK = 60f;

    private final Rectangle bounds;
    private final Rectangle attackBounds;

    private float velocityY;
    private float coyoteTimer;
    private float jumpBufferTimer;
    private float attackTimer;
    private float attackCooldownTimer;
    private float invulnerabilityTimer;

    private int health;
    private boolean onGround;
    private boolean facingRight;

    public Player(float startingX, float startingY) {
        bounds = new Rectangle(startingX, startingY, WIDTH, HEIGHT);
        attackBounds = new Rectangle();

        health = MAX_HEALTH;
        facingRight = true;

        resetMovement();
    }

    public void update(
            float deltaTime,
            float worldWidth,
            List<Rectangle> platforms) {

        invulnerabilityTimer =
                Math.max(0f, invulnerabilityTimer - deltaTime);

        float moveX = readHorizontalMovement();

        if (moveX > 0f) {
            facingRight = true;
        } else if (moveX < 0f) {
            facingRight = false;
        }

        updateHorizontalMovement(
                moveX,
                deltaTime,
                worldWidth,
                platforms
        );

        updateJumpTimers(deltaTime);
        checkForJump();

        applyGravity(deltaTime);
        resolveVerticalCollisions(platforms);

        updateAttack(deltaTime);
    }

    private float readHorizontalMovement() {
        float moveX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += 1f;
        }

        return moveX;
    }

    private void updateHorizontalMovement(
            float moveX,
            float deltaTime,
            float worldWidth,
            List<Rectangle> platforms) {

        bounds.x += moveX * RUN_SPEED * deltaTime;

        bounds.x = MathUtils.clamp(
                bounds.x,
                0f,
                worldWidth - bounds.width
        );

        resolveHorizontalCollisions(platforms, moveX);
    }

    private void updateJumpTimers(float deltaTime) {
        if (onGround) {
            coyoteTimer = COYOTE_DURATION;
        } else {
            coyoteTimer = Math.max(0f, coyoteTimer - deltaTime);
        }

        boolean jumpPressed =
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        if (jumpPressed) {
            jumpBufferTimer = JUMP_BUFFER_DURATION;
        } else {
            jumpBufferTimer =
                    Math.max(0f, jumpBufferTimer - deltaTime);
        }
    }

    private void checkForJump() {
        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            velocityY = JUMP_SPEED;
            onGround = false;
            jumpBufferTimer = 0f;
            coyoteTimer = 0f;
        }
    }

    private void applyGravity(float deltaTime) {
        boolean jumpHeld =
                Gdx.input.isKeyPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.UP);

        float gravityMultiplier = 1f;

        if (!jumpHeld && velocityY > 0f) {
            gravityMultiplier = 2.5f;
        }

        velocityY += GRAVITY * gravityMultiplier * deltaTime;
        velocityY = Math.max(velocityY, MAX_FALL_SPEED);

        bounds.y += velocityY * deltaTime;
        onGround = false;
    }

    private void updateAttack(float deltaTime) {
        attackTimer = Math.max(0f, attackTimer - deltaTime);
        attackCooldownTimer =
                Math.max(0f, attackCooldownTimer - deltaTime);

        boolean attackPressed =
                Gdx.input.isKeyJustPressed(Input.Keys.J)
                || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        if (attackPressed && attackCooldownTimer <= 0f) {
            attackTimer = ATTACK_DURATION;
            attackCooldownTimer = ATTACK_COOLDOWN;
        }

        float attackX = facingRight
                ? bounds.x + bounds.width
                : bounds.x - ATTACK_RANGE;

        attackBounds.set(
                attackX,
                bounds.y + 13f,
                ATTACK_RANGE,
                ATTACK_HEIGHT
        );
    }

    public void takeDamage(
            int damage,
            float damageSourceCenterX,
            float worldWidth) {

        if (invulnerabilityTimer > 0f || !isAlive()) {
            return;
        }

        health -= damage;
        invulnerabilityTimer = INVULNERABILITY_DURATION;

        float playerCenterX = bounds.x + bounds.width / 2f;

        if (damageSourceCenterX < playerCenterX) {
            bounds.x += DAMAGE_KNOCKBACK;
        } else {
            bounds.x -= DAMAGE_KNOCKBACK;
        }

        bounds.x = MathUtils.clamp(
                bounds.x,
                0f,
                worldWidth - bounds.width
        );

        velocityY = 350f;
        onGround = false;
    }

    public void respawn(float respawnX, float respawnY) {
        bounds.setPosition(respawnX, respawnY);
        health = MAX_HEALTH;

        resetMovement();

        // Prevent immediate damage after respawning.
        invulnerabilityTimer = INVULNERABILITY_DURATION;
    }

    private void resetMovement() {
        velocityY = 0f;
        coyoteTimer = COYOTE_DURATION;
        jumpBufferTimer = 0f;
        attackTimer = 0f;
        attackCooldownTimer = 0f;
        invulnerabilityTimer = 0f;
        onGround = true;
    }

    private void resolveHorizontalCollisions(
            List<Rectangle> platforms,
            float moveX) {

        for (Rectangle platform : platforms) {
            if (!bounds.overlaps(platform)) {
                continue;
            }

            if (moveX > 0f) {
                bounds.x = platform.x - bounds.width;
            } else if (moveX < 0f) {
                bounds.x = platform.x + platform.width;
            }
        }
    }

    private void resolveVerticalCollisions(List<Rectangle> platforms) {
        for (Rectangle platform : platforms) {
            if (!bounds.overlaps(platform)) {
                continue;
            }

            if (velocityY > 0f) {
                bounds.y = platform.y - bounds.height;
                velocityY = 0f;
            } else if (velocityY < 0f) {
                bounds.y = platform.y + platform.height;
                velocityY = 0f;
                onGround = true;
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        boolean damageFlash =
                invulnerabilityTimer > 0f
                && ((int) (invulnerabilityTimer * 16f)) % 2 == 0;

        if (damageFlash) {
            shapeRenderer.setColor(0.70f, 0.12f, 0.12f, 1f);
        } else {
            shapeRenderer.setColor(0.04f, 0.04f, 0.06f, 1f);
        }

        shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height - 12f
        );

        shapeRenderer.setColor(0.82f, 0.64f, 0.48f, 1f);
        shapeRenderer.circle(
                bounds.x + bounds.width / 2f,
                bounds.y + bounds.height - 10f,
                10f
        );

        shapeRenderer.setColor(0.30f, 0.20f, 0.10f, 1f);
        shapeRenderer.rect(
                bounds.x + 6f,
                bounds.y + bounds.height - 6f,
                20f,
                5f
        );

        shapeRenderer.setColor(0.35f, 0.35f, 0.38f, 1f);
        shapeRenderer.rect(
                bounds.x,
                bounds.y + 20f,
                bounds.width,
                5f
        );

        if (isAttacking()) {
            drawLightsaber(shapeRenderer);
        }
    }

    private void drawLightsaber(ShapeRenderer shapeRenderer) {
        float bladeY = attackBounds.y + attackBounds.height / 2f;

        shapeRenderer.setColor(0.65f, 0.68f, 0.72f, 1f);

        float hiltX = facingRight
                ? bounds.x + bounds.width - 2f
                : bounds.x - 8f;

        shapeRenderer.rect(hiltX, bladeY - 4f, 10f, 8f);

        shapeRenderer.setColor(0.20f, 1f, 0.35f, 1f);
        shapeRenderer.rect(
                attackBounds.x,
                bladeY - 3f,
                attackBounds.width,
                6f
        );
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Rectangle getAttackBounds() {
        return isAttacking() ? attackBounds : null;
    }

    public int getHealth() {
        return health;
    }

    public int getMaximumHealth() {
        return MAX_HEALTH;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isAttacking() {
        return attackTimer > 0f;
    }
}
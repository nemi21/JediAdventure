package com.nehemiah.jediadventure.entities;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    private static final float WIDTH = 32f;
    private static final float HEIGHT = 56f;

    private static final float DRAW_WIDTH = 82f;
    private static final float DRAW_HEIGHT = 96f;
    private static final float DRAW_Y_OFFSET = -3f;

    private static final int SPRITE_FRAME_WIDTH = 544;
    private static final int SPRITE_FRAME_HEIGHT = 640;

    private static final float IDLE_FRAME_DURATION = 0.18f;
    private static final float RUN_FRAME_DURATION = 0.09f;

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

    private final Texture idleTexture;
    private final Texture runTexture;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runAnimation;

    private float velocityY;
    private float coyoteTimer;
    private float jumpBufferTimer;
    private float attackTimer;
    private float attackCooldownTimer;
    private float invulnerabilityTimer;
    private float animationTime;

    private int health;

    private boolean onGround;
    private boolean facingRight;
    private boolean movingHorizontally;

    public Player(float startingX, float startingY) {
        bounds = new Rectangle(startingX, startingY, WIDTH, HEIGHT);
        attackBounds = new Rectangle();

        idleTexture = new Texture(
                Gdx.files.internal(
                        "characters/luke/luke_idle_4frame.png"
                )
        );

        runTexture = new Texture(
                Gdx.files.internal(
                        "characters/luke/luke_run_6frame.png"
                )
        );

        idleTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
        );

        runTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
        );

        TextureRegion[][] idleFrames = TextureRegion.split(
                idleTexture,
                SPRITE_FRAME_WIDTH,
                SPRITE_FRAME_HEIGHT
        );

        TextureRegion[][] runFrames = TextureRegion.split(
                runTexture,
                SPRITE_FRAME_WIDTH,
                SPRITE_FRAME_HEIGHT
        );

        idleAnimation = new Animation<>(
                IDLE_FRAME_DURATION,
                idleFrames[0]
        );

        runAnimation = new Animation<>(
                RUN_FRAME_DURATION,
                runFrames[0]
        );

        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        runAnimation.setPlayMode(Animation.PlayMode.LOOP);

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

        float previousX = bounds.x;

        updateHorizontalMovement(
                moveX,
                deltaTime,
                worldWidth,
                platforms
        );

        boolean currentlyMoving =
                Math.abs(bounds.x - previousX) > 0.01f;

        updateAnimation(deltaTime, currentlyMoving);

        updateJumpTimers(deltaTime);
        checkForJump();

        applyGravity(deltaTime);
        resolveVerticalCollisions(platforms);

        updateAttack(deltaTime);
    }

    private void updateAnimation(
            float deltaTime,
            boolean currentlyMoving) {

        if (movingHorizontally != currentlyMoving) {
            animationTime = 0f;
        } else {
            animationTime += deltaTime;
        }

        movingHorizontally = currentlyMoving;
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

        invulnerabilityTimer = INVULNERABILITY_DURATION;
    }

    private void resetMovement() {
        velocityY = 0f;
        coyoteTimer = COYOTE_DURATION;
        jumpBufferTimer = 0f;
        attackTimer = 0f;
        attackCooldownTimer = 0f;
        invulnerabilityTimer = 0f;
        animationTime = 0f;

        movingHorizontally = false;
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

    public void render(SpriteBatch spriteBatch) {
        TextureRegion currentFrame;

        if (!onGround) {
            // Temporary airborne pose until we add a jump sheet.
            currentFrame = runAnimation.getKeyFrames()[0];
        } else if (movingHorizontally) {
            currentFrame = runAnimation.getKeyFrame(
                    animationTime,
                    true
            );
        } else {
            currentFrame = idleAnimation.getKeyFrame(
                    animationTime,
                    true
            );
        }

        boolean damageFlash =
                invulnerabilityTimer > 0f
                && ((int) (invulnerabilityTimer * 16f)) % 2 == 0;

        if (damageFlash) {
            spriteBatch.setColor(1f, 0.35f, 0.35f, 1f);
        } else {
            spriteBatch.setColor(Color.WHITE);
        }

        float drawX =
                bounds.x + bounds.width / 2f - DRAW_WIDTH / 2f;

        float drawY = bounds.y + DRAW_Y_OFFSET;

        float horizontalScale = facingRight ? 1f : -1f;

        spriteBatch.draw(
                currentFrame,
                drawX,
                drawY,
                DRAW_WIDTH / 2f,
                0f,
                DRAW_WIDTH,
                DRAW_HEIGHT,
                horizontalScale,
                1f,
                0f
        );

        spriteBatch.setColor(Color.WHITE);
    }

    public void renderAttack(ShapeRenderer shapeRenderer) {
        if (isAttacking()) {
            drawLightsaber(shapeRenderer);
        }
    }

    private void drawLightsaber(ShapeRenderer shapeRenderer) {
        float bladeY =
                attackBounds.y + attackBounds.height / 2f;

        float hiltX = facingRight
                ? bounds.x + bounds.width - 2f
                : bounds.x - 8f;

        shapeRenderer.setColor(0.65f, 0.68f, 0.72f, 1f);
        shapeRenderer.rect(
                hiltX,
                bladeY - 4f,
                10f,
                8f
        );

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

    public void dispose() {
        idleTexture.dispose();
        runTexture.dispose();
    }
}
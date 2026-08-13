package com.nehemiah.jediadventure.entities;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {

	private static final float WIDTH = 32f;
	private static final float HEIGHT = 56f;

	private static final float DRAW_WIDTH = 82f;
	private static final float DRAW_HEIGHT = 96f;
	private static final float DRAW_Y_OFFSET = -3f;

	private static final float ATTACK_DRAW_WIDTH = 116f;

	private static final int SPRITE_FRAME_WIDTH = 544;
	private static final int SPRITE_FRAME_HEIGHT = 640;

	private static final int ATTACK_FRAME_WIDTH = 768;
	private static final int ATTACK_FRAME_HEIGHT = 640;

	private static final int AIR_FRAME_WIDTH = 544;
	private static final int AIR_FRAME_HEIGHT = 640;

	private static final float IDLE_FRAME_DURATION = 0.18f;
	private static final float RUN_FRAME_DURATION = 0.09f;
	private static final float ATTACK_FRAME_DURATION = 0.055f;
	private static final float JUMP_FRAME_DURATION = 0.08f;
	private static final float FALL_FRAME_DURATION = 0.09f;

	private static final float RUN_SPEED = 300f;
	private static final float JUMP_SPEED = 700f;
	private static final float GRAVITY = -1800f;
	private static final float MAX_FALL_SPEED = -1000f;
	private static final float DROP_THROUGH_DURATION = 0.20f;
	
	private static final float WALL_SLIDE_SPEED = -180f;
	private static final float WALL_JUMP_HORIZONTAL_SPEED = 420f;
	private static final float WALL_JUMP_VERTICAL_SPEED = 680f;
	private static final float WALL_JUMP_CONTROL_DURATION = 0.13f;

	private static final float COYOTE_DURATION = 0.12f;
	private static final float JUMP_BUFFER_DURATION = 0.12f;

	private static final float ATTACK_DURATION = 0.22f;
	private static final float ATTACK_COOLDOWN = 0.34f;
	private static final float ATTACK_RANGE = 54f;
	private static final float ATTACK_HEIGHT = 30f;

	private static final int MAX_HEALTH = 5;
	private static final float INVULNERABILITY_DURATION = 1f;
	private static final float DAMAGE_KNOCKBACK = 60f;

    private final Rectangle bounds;
    private final Rectangle attackBounds;

    private final Texture idleTexture;
    private final Texture runTexture;
    private final Texture attackTexture;
    private final Animation<TextureRegion> attackAnimation;
    private final Texture airTexture;

    private float attackAnimationTime;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runAnimation;
    private final Animation<TextureRegion> jumpAnimation;
    private final Animation<TextureRegion> fallAnimation;

    private float velocityY;
    private float coyoteTimer;
    private float jumpBufferTimer;
    private float attackTimer;
    private float attackCooldownTimer;
    private float invulnerabilityTimer;
    private float animationTime;
    private float jumpAnimationTime;
    private float fallAnimationTime;
    private float dropThroughTimer;
    private float wallJumpControlTimer;
    private float wallJumpDirection;
    private boolean standingOnOneWayPlatform;

    private int health;

    private boolean onGround;
    private boolean facingRight;
    private boolean movingHorizontally;
    private boolean attackHasHit;
    
    private boolean touchingWallLeft;
    private boolean touchingWallRight;
    private boolean wallSliding;

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
        
        attackTexture = new Texture(
                Gdx.files.internal(
                        "characters/luke/luke_attack_quick_4frame.png"
                )
        );
        
        airTexture = new Texture(
                Gdx.files.internal(
                        "characters/luke/luke_air_6frame.png"
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
        
        attackTexture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
        );
        
        airTexture.setFilter(
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
        
        TextureRegion[][] attackGrid = TextureRegion.split(
                attackTexture,
                ATTACK_FRAME_WIDTH,
                ATTACK_FRAME_HEIGHT
        );

        TextureRegion[] attackFrames = {
                attackGrid[0][0],
                attackGrid[0][1],
                attackGrid[1][0],
                attackGrid[1][1]
        };

        attackAnimation = new Animation<>(
                ATTACK_FRAME_DURATION,
                attackFrames
        );

        attackAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[][] airGrid = TextureRegion.split(
                airTexture,
                AIR_FRAME_WIDTH,
                AIR_FRAME_HEIGHT
        );

        TextureRegion[] jumpFrames = {
                airGrid[0][0],
                airGrid[0][1],
                airGrid[0][2]
        };

        TextureRegion[] fallFrames = {
                airGrid[1][0],
                airGrid[1][1],
                airGrid[1][2]
        };

        jumpAnimation = new Animation<>(
                JUMP_FRAME_DURATION,
                jumpFrames
        );

        fallAnimation = new Animation<>(
                FALL_FRAME_DURATION,
                fallFrames
        );

        jumpAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        fallAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        runAnimation.setPlayMode(Animation.PlayMode.LOOP);

        health = MAX_HEALTH;
        facingRight = true;

        resetMovement();
    }
    
    public void update(
            float deltaTime,
            float worldWidth,
            List<Rectangle> platforms,
            List<Rectangle> oneWayPlatforms) {

        dropThroughTimer =
                Math.max(
                        0f,
                        dropThroughTimer - deltaTime
                );

        wallJumpControlTimer =
                Math.max(
                        0f,
                        wallJumpControlTimer - deltaTime
                );

        invulnerabilityTimer =
                Math.max(
                        0f,
                        invulnerabilityTimer - deltaTime
                );

        float moveX = readHorizontalMovement();

        // During the brief wall-jump movement, Luke continues
        // facing away from the wall.
        if (wallJumpControlTimer <= 0f) {
            if (moveX > 0f) {
                facingRight = true;
            } else if (moveX < 0f) {
                facingRight = false;
            }
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

        updateAnimation(
                deltaTime,
                currentlyMoving
        );

        updateJumpTimers(deltaTime);
        checkForJump();

        float previousY = bounds.y;

        applyGravity(
                deltaTime,
                moveX
        );

        standingOnOneWayPlatform = false;

        resolveVerticalCollisions(platforms);

        resolveOneWayPlatformCollisions(
                oneWayPlatforms,
                previousY
        );

        if (onGround) {
            wallSliding = false;
        }

        updateAirAnimation(deltaTime);
        updateAttack(deltaTime);
    }
    
    private void updateAirAnimation(float deltaTime) {
        if (onGround) {
            jumpAnimationTime = 0f;
            fallAnimationTime = 0f;
            return;
        }

        if (velocityY > 0f) {
            jumpAnimationTime += deltaTime;
            fallAnimationTime = 0f;
        } else {
            fallAnimationTime += deltaTime;
        }
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

        touchingWallLeft = false;
        touchingWallRight = false;

        float horizontalSpeed =
                moveX * RUN_SPEED;

        // Briefly push Luke away from the wall after a wall jump.
        if (wallJumpControlTimer > 0f) {
            horizontalSpeed =
                    wallJumpDirection
                    * WALL_JUMP_HORIZONTAL_SPEED;
        }

        bounds.x += horizontalSpeed * deltaTime;

        bounds.x = MathUtils.clamp(
                bounds.x,
                0f,
                worldWidth - bounds.width
        );

        float movementDirection =
                Math.signum(horizontalSpeed);

        resolveHorizontalCollisions(
                platforms,
                movementDirection
        );
    }

    private void updateJumpTimers(float deltaTime) {
        if (onGround) {
            coyoteTimer = COYOTE_DURATION;
        } else {
            coyoteTimer =
                    Math.max(0f, coyoteTimer - deltaTime);
        }

        boolean jumpPressed =
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        boolean downHeld =
                Gdx.input.isKeyPressed(Input.Keys.S)
                || Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if (jumpPressed
                && downHeld
                && standingOnOneWayPlatform) {

            dropThroughTimer = DROP_THROUGH_DURATION;

            jumpBufferTimer = 0f;
            coyoteTimer = 0f;

            onGround = false;
            standingOnOneWayPlatform = false;

            // Move Luke slightly below the platform.
            bounds.y -= 6f;

        } else if (jumpPressed) {
            jumpBufferTimer = JUMP_BUFFER_DURATION;
        } else {
            jumpBufferTimer =
                    Math.max(0f, jumpBufferTimer - deltaTime);
        }
    }

    private void checkForJump() {
        if (jumpBufferTimer <= 0f) {
            return;
        }

        boolean canWallJump =
                !onGround
                && (touchingWallLeft || touchingWallRight);

        if (canWallJump) {
            if (touchingWallLeft) {
                wallJumpDirection = 1f;
            } else {
                wallJumpDirection = -1f;
            }

            velocityY = WALL_JUMP_VERTICAL_SPEED;
            wallJumpControlTimer =
                    WALL_JUMP_CONTROL_DURATION;

            facingRight = wallJumpDirection > 0f;

            jumpBufferTimer = 0f;
            coyoteTimer = 0f;

            onGround = false;
            wallSliding = false;

            return;
        }

        if (coyoteTimer > 0f) {
            velocityY = JUMP_SPEED;

            onGround = false;

            jumpBufferTimer = 0f;
            coyoteTimer = 0f;
        }
    }

    private void applyGravity(
            float deltaTime,
            float moveX) {

        boolean jumpHeld =
                Gdx.input.isKeyPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.UP);

        float gravityMultiplier = 1f;

        if (!jumpHeld && velocityY > 0f) {
            gravityMultiplier = 2.5f;
        }

        velocityY +=
                GRAVITY
                * gravityMultiplier
                * deltaTime;

        velocityY =
                Math.max(
                        velocityY,
                        MAX_FALL_SPEED
                );

        boolean pressingTowardWall =
                (touchingWallLeft && moveX < 0f)
                || (touchingWallRight && moveX > 0f);

        wallSliding =
                wallJumpControlTimer <= 0f
                && !onGround
                && velocityY < 0f
                && pressingTowardWall;

        if (wallSliding) {
            velocityY =
                    Math.max(
                            velocityY,
                            WALL_SLIDE_SPEED
                    );
        }

        bounds.y += velocityY * deltaTime;
        onGround = false;
    }

    private void updateAttack(float deltaTime) {
        if (attackTimer > 0f) {
            attackAnimationTime += deltaTime;
        }

        attackTimer = Math.max(0f, attackTimer - deltaTime);

        attackCooldownTimer =
                Math.max(0f, attackCooldownTimer - deltaTime);

        boolean attackPressed =
                Gdx.input.isKeyJustPressed(Input.Keys.J)
                || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        if (attackPressed && attackCooldownTimer <= 0f) {
            attackTimer = ATTACK_DURATION;
            attackCooldownTimer = ATTACK_COOLDOWN;
            attackAnimationTime = 0f;
            attackHasHit = false;
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
        attackAnimationTime = 0f;
        jumpAnimationTime = 0f;
        fallAnimationTime = 0f;

        movingHorizontally = false;
        onGround = true;
        attackHasHit = false;
        
        dropThroughTimer = 0f;
        standingOnOneWayPlatform = false;
        
        wallJumpControlTimer = 0f;
        wallJumpDirection = 0f;

        touchingWallLeft = false;
        touchingWallRight = false;
        wallSliding = false;
    }

    private void resolveHorizontalCollisions(
            List<Rectangle> platforms,
            float movementDirection) {

        for (Rectangle platform : platforms) {
            if (!bounds.overlaps(platform)) {
                continue;
            }

            if (movementDirection > 0f) {
                bounds.x =
                        platform.x - bounds.width;

                touchingWallRight = true;

            } else if (movementDirection < 0f) {
                bounds.x =
                        platform.x + platform.width;

                touchingWallLeft = true;
            }
        }
    }
    
    private void resolveOneWayPlatformCollisions(
            List<Rectangle> oneWayPlatforms,
            float previousY) {

        if (velocityY > 0f || dropThroughTimer > 0f) {
            return;
        }

        float previousBottom = previousY;
        float currentBottom = bounds.y;

        for (Rectangle platform : oneWayPlatforms) {
            float platformTop =
                    platform.y + platform.height;

            boolean horizontallyOverlapping =
                    bounds.x + bounds.width > platform.x
                    && bounds.x
                    < platform.x + platform.width;

            boolean crossedPlatformTop =
                    previousBottom >= platformTop
                    && currentBottom <= platformTop;

            if (horizontallyOverlapping
                    && crossedPlatformTop) {

                bounds.y = platformTop;
                velocityY = 0f;

                onGround = true;
                standingOnOneWayPlatform = true;

                return;
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
        float currentDrawWidth;

        if (isAttacking()) {
            currentFrame = attackAnimation.getKeyFrame(
                    attackAnimationTime,
                    false
            );

            currentDrawWidth = ATTACK_DRAW_WIDTH;
        }  else if (!onGround && velocityY > 0f) {
            currentFrame = jumpAnimation.getKeyFrame(
                    jumpAnimationTime,
                    false
            );

            currentDrawWidth = DRAW_WIDTH;
        } else if (!onGround) {
            currentFrame = fallAnimation.getKeyFrame(
                    fallAnimationTime,
                    false
            );

            currentDrawWidth = DRAW_WIDTH;
        } else if (movingHorizontally) {
            currentFrame = runAnimation.getKeyFrame(
                    animationTime,
                    true
            );

            currentDrawWidth = DRAW_WIDTH;
        } else {
            currentFrame = idleAnimation.getKeyFrame(
                    animationTime,
                    true
            );

            currentDrawWidth = DRAW_WIDTH;
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
                bounds.x + bounds.width / 2f
                - currentDrawWidth / 2f;

        float drawY = bounds.y + DRAW_Y_OFFSET;

        float horizontalScale = facingRight ? 1f : -1f;

        spriteBatch.draw(
                currentFrame,
                drawX,
                drawY,
                currentDrawWidth / 2f,
                0f,
                currentDrawWidth,
                DRAW_HEIGHT,
                horizontalScale,
                1f,
                0f
        );

        spriteBatch.setColor(Color.WHITE);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Rectangle getAttackBounds() {
        if (!isAttacking() || attackHasHit) {
            return null;
        }

        return attackBounds;
    }
    
    public void markAttackHit() {
        attackHasHit = true;
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
        attackTexture.dispose();
        airTexture.dispose();
    }
    
}
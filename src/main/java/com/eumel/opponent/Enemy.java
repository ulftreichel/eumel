package com.eumel.opponent;

import com.eumel.Projectile;
import com.eumel.level.design.BreakablePlatform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.List;

import static com.eumel.JumpAndRun.debugProjektil;

public class Enemy extends Rectangle {
    private double initialX;
    private double initialY;
    private double velocityX;
    private double velocityY;
    private double range;
    private int type;
    private ImageView imageView;
    private static Image[] rightImages;
    private static Image[] leftImages;
    private boolean isFloating;
    private final double GRAVITY = 0.5;
    private int health;
    private boolean isActive = true;
    private boolean canShoot;
    private int damage;
    private double projectileRange;
    private double sightRange;
    private static final double SIGHT_HEIGHT_RANGE = 100.0;
    private long lastShotTime = 0;
    private static final long SHOOT_COOLDOWN = 1_000_000_000;

    static {
        rightImages = new Image[6];
        leftImages = new Image[6];
        for (int i = 1; i <= 6; i++) {
            try {
                rightImages[i - 1] = new Image(Enemy.class.getResource("/images/enemys/enemy" + i + "right.png").toExternalForm());
                leftImages[i - 1] = new Image(Enemy.class.getResource("/images/enemys/enemy" + i + "left.png").toExternalForm());
            } catch (Exception e) {
                System.out.println("Fehler beim Laden des Gegnerbildes enemy" + i + ": " + e.getMessage());
                rightImages[i - 1] = null;
                leftImages[i - 1] = null;
            }
        }
    }

    public Enemy(double x, double y, double width, double height, double velocityX, double range, int type, boolean isFloating, int health, boolean canShoot, int damage, double projectileRange, double sightRange) {
        super(x, y, width, height);
        this.initialX = x;
        this.initialY = y;
        this.velocityX = velocityX;
        this.velocityY = 0;
        this.range = range;
        this.type = type;
        this.isFloating = isFloating;
        this.health = health;
        this.isActive = true;
        this.canShoot = canShoot;
        this.damage = damage > 0 ? damage : 0;
        this.projectileRange = projectileRange > 0 ? projectileRange : 180;
        this.sightRange = sightRange > 0 ? sightRange : 200;
        if (type < 1 || type > 6) {
            throw new IllegalArgumentException("Gegnertyp muss zwischen 1 und 6 liegen, ist aber: " + type);
        }

        this.imageView = new ImageView();
        this.imageView.setFitWidth(width);
        this.imageView.setFitHeight(height);
        this.imageView.setX(x);
        this.imageView.setY(y);
        updateImage();
    }

    public void updatePosition(List<BreakablePlatform> platforms, Pane gamePane, List<Projectile> enemyProjectiles, double playerX, double playerY) {
        if (isActive) {
            setX(getX() + velocityX);
            imageView.setX(getX());
            if (getX() <= initialX || getX() >= initialX + range) {
                velocityX = -velocityX;
            }

            if (!isFloating) {
                velocityY += GRAVITY;
            }

            double oldY = getY();
            setY(getY() + velocityY);
            imageView.setY(getY());

            if (!isFloating) {
                for (BreakablePlatform platform : platforms) {
                    if (platform.isDisable()) continue;
                    double enemyBottom = getY() + getHeight();
                    double enemyTop = getY();
                    double enemyLeft = getX();
                    double enemyRight = getX() + getWidth();
                    double platformTop = platform.getY();
                    double platformBottom = platform.getY() + platform.getHeight();
                    double platformLeft = platform.getX();
                    double platformRight = platform.getX() + platform.getWidth();

                    if (enemyBottom >= platformTop && oldY + getHeight() <= platformTop &&
                            enemyRight > platformLeft && enemyLeft < platformRight &&
                            velocityY > 0) {
                        setY(platformTop - getHeight());
                        velocityY = 0;
                        imageView.setY(getY());
                        break;
                    }
                }
            } else {
                if (getY() < initialY) {
                    setY(initialY);
                    velocityY = 0;
                    imageView.setY(initialY);
                } else if (getY() > initialY + 10) {
                    velocityY = -0.5;
                }
            }

            if (canShoot && System.nanoTime() - lastShotTime >= SHOOT_COOLDOWN) {
                shootAtPlayer(gamePane, enemyProjectiles, playerX, playerY);
                lastShotTime = System.nanoTime();
            }

            updateImage();
        }
    }

    private void shootAtPlayer(Pane gamePane, List<Projectile> enemyProjectiles, double playerX, double playerY) {
        double enemyCenterX = getX() + getWidth() / 2;
        double enemyCenterY = getY() + getHeight() / 2;
        boolean playerIsRight = playerX > enemyCenterX;
        boolean facingRight = velocityX >= 0;

        double horizontalDistance = Math.abs(playerX - enemyCenterX);
        double verticalDistance = Math.abs(playerY - enemyCenterY);
        if (horizontalDistance <= sightRange && verticalDistance <= SIGHT_HEIGHT_RANGE) {
            // Schieße nur, wenn die Blickrichtung mit der Spielerposition übereinstimmt
            if ((facingRight && playerIsRight) || (!facingRight && !playerIsRight)) {
                Projectile enemyProjectile = new Projectile(enemyCenterX, enemyCenterY, playerIsRight, null, this.damage, true, 0);
                enemyProjectiles.add(enemyProjectile);
                gamePane.getChildren().add(enemyProjectile.getImageView());
                if (debugProjektil) {
                    System.out.println("Gegner schießt bei x=" + enemyCenterX + ", y=" + enemyCenterY + ", Richtung: " + (playerIsRight ? "rechts" : "links") + ", Schaden: " + damage + ", Reichweite: " + projectileRange + ", Sichtbereich: " + sightRange + ", Höhenbereich: " + SIGHT_HEIGHT_RANGE + ", Spielerposition: X" + playerX + " , Y" + playerY);
                }
            } else {
                if (debugProjektil) {
                    System.out.println("Gegner schießt nicht: Blickrichtung (" + (facingRight ? "rechts" : "links") + ") passt nicht zur Spielerposition (" + (playerIsRight ? "rechts" : "links") + ")");
                }
            }
        } else {
            if (debugProjektil) {
                System.out.println("Gegner schießt nicht: Spieler außerhalb des Sichtbereichs (horizontal: " + horizontalDistance + " > " + sightRange + " oder vertikal: " + verticalDistance + " > " + SIGHT_HEIGHT_RANGE + ")");
            }
        }
    }

    private void updateImage() {
        if (isActive && velocityX >= 0) {
            imageView.setImage(rightImages[type - 1]);
        } else if (isActive) {
            imageView.setImage(leftImages[type - 1]);
        } else {
            imageView.setImage(null);
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        if (isActive) {
            this.health -= damage;
            if (health <= 0) {
                isActive = false;
                imageView.setVisible(false);
            }
            if (debugProjektil) {
                System.out.println("Gegner getroffen! Neue Lebenspunkte: " + health);
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public int getDamage() {
        return damage;
    }
}
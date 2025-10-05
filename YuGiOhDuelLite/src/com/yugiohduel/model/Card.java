package com.yugiohduel.model;

/**
 * Representa una carta de Yu-Gi-Oh! con sus atributos básicos.
 */
public class Card {
    private String name;
    private int atk;
    private int def;
    private String imageUrl;
    private String type;

    /**
     * Constructor completo de la carta
     * @param name Nombre de la carta
     * @param atk Puntos de ataque
     * @param def Puntos de defensa
     * @param imageUrl URL de la imagen de la carta
     * @param type Tipo de carta (Monster, Spell, Trap)
     */
    public Card(String name, int atk, int def, String imageUrl, String type) {
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.imageUrl = imageUrl;
        this.type = type;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getType() {
        return type;
    }

    /**
     * Verifica si la carta es un monstruo
     * @return true si es un monstruo, false en caso contrario
     */
    public boolean isMonster() {
        return type != null && type.toLowerCase().contains("monster");
    }

    @Override
    public String toString() {
        return String.format("%s [ATK:%d / DEF:%d]", name, atk, def);
    }
}
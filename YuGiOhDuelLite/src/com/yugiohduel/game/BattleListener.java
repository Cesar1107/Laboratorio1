package com.yugiohduel.game;

/**
 * Interfaz para notificar eventos del duelo.
 * Permite desacoplar la lógica del juego de la interfaz gráfica.
 */
public interface BattleListener {

    /**
     * Se ejecuta cuando ocurre un turno de batalla
     * @param playerCard Nombre de la carta del jugador
     * @param aiCard Nombre de la carta de la IA
     * @param winner Nombre del ganador del turno ("Player", "AI", o "Draw")
     */
    void onTurn(String playerCard, String aiCard, String winner);

    /**
     * Se ejecuta cuando cambia el puntaje
     * @param playerScore Puntos del jugador
     * @param aiScore Puntos de la IA
     */
    void onScoreChanged(int playerScore, int aiScore);

    /**
     * Se ejecuta cuando el duelo termina
     * @param winner Ganador del duelo ("Player" o "AI")
     */
    void onDuelEnded(String winner);
}
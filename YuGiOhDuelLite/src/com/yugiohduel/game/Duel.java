package com.yugiohduel.game;

import com.yugiohduel.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestiona la lógica completa del duelo entre el jugador y la IA
 */
public class Duel {
    private List<Card> playerCards;
    private List<Card> aiCards;
    private int playerScore;
    private int aiScore;
    private boolean playerTurn;
    private BattleListener listener;
    private Random random;
    private boolean duelEnded;

    private static final int WINNING_SCORE = 2;

    public Duel() {
        this.playerCards = new ArrayList<>();
        this.aiCards = new ArrayList<>();
        this.playerScore = 0;
        this.aiScore = 0;
        this.random = new Random();
        this.duelEnded = false;

        // Determinar turno inicial aleatoriamente
        this.playerTurn = random.nextBoolean();
    }

    /**
     * Establece el listener para eventos de batalla
     */
    public void setBattleListener(BattleListener listener) {
        this.listener = listener;
    }

    /**
     * Agrega una carta al mazo del jugador
     */
    public void addPlayerCard(Card card) {
        playerCards.add(card);
    }

    /**
     * Agrega una carta al mazo de la IA
     */
    public void addAiCard(Card card) {
        aiCards.add(card);
    }

    /**
     * Verifica si el duelo está listo para comenzar
     */
    public boolean isReady() {
        return playerCards.size() == 3 && aiCards.size() == 3;
    }

    /**
     * Ejecuta un turno de batalla
     * @param playerCardIndex Índice de la carta elegida por el jugador
     */
    public void playTurn(int playerCardIndex) {
        if (duelEnded) {
            return;
        }

        if (playerCardIndex < 0 || playerCardIndex >= playerCards.size()) {
            return;
        }

        // Jugador elige su carta
        Card playerCard = playerCards.get(playerCardIndex);

        // IA elige carta aleatoria
        int aiCardIndex = random.nextInt(aiCards.size());
        Card aiCard = aiCards.get(aiCardIndex);

        // Determinar ganador del turno
        String winner = determineWinner(playerCard, aiCard);

        // Actualizar puntajes
        if (winner.equals("Player")) {
            playerScore++;
        } else if (winner.equals("AI")) {
            aiScore++;
        }

        // Notificar eventos
        if (listener != null) {
            listener.onTurn(playerCard.toString(), aiCard.toString(), winner);
            listener.onScoreChanged(playerScore, aiScore);
        }

        // Remover cartas usadas
        playerCards.remove(playerCardIndex);
        aiCards.remove(aiCardIndex);

        // Verificar si el duelo terminó
        if (playerScore >= WINNING_SCORE || aiScore >= WINNING_SCORE) {
            duelEnded = true;
            String duelWinner = playerScore >= WINNING_SCORE ? "Player" : "AI";
            if (listener != null) {
                listener.onDuelEnded(duelWinner);
            }
        }
    }

    /**
     * Determina el ganador comparando ATK vs DEF
     * Reglas simplificadas: ambos atacan, gana el mayor ATK
     */
    private String determineWinner(Card playerCard, Card aiCard) {
        int playerPower = playerCard.getAtk();
        int aiPower = aiCard.getAtk();

        if (playerPower > aiPower) {
            return "Player";
        } else if (aiPower > playerPower) {
            return "AI";
        } else {
            return "Draw";
        }
    }

    // Getters
    public List<Card> getPlayerCards() {
        return new ArrayList<>(playerCards);
    }

    public List<Card> getAiCards() {
        return new ArrayList<>(aiCards);
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getAiScore() {
        return aiScore;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public boolean isDuelEnded() {
        return duelEnded;
    }
}
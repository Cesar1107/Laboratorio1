package com.yugiohduel.ui;

import com.yugiohduel.api.YgoApiClient;
import com.yugiohduel.game.BattleListener;
import com.yugiohduel.game.Duel;
import com.yugiohduel.model.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Interfaz gráfica principal del juego Yu-Gi-Oh! Duel Lite
 */
public class DuelUI extends JFrame implements BattleListener {

    // Componentes principales
    private JPanel playerCardsPanel;
    private JPanel aiCardsPanel;
    private JTextArea battleLog;
    private JLabel playerScoreLabel;
    private JLabel aiScoreLabel;
    private JButton startButton;
    private JLabel statusLabel;

    // Lógica del juego
    private YgoApiClient apiClient;
    private Duel duel;
    private List<JButton> cardButtons;

    public DuelUI() {
        apiClient = new YgoApiClient();
        cardButtons = new ArrayList<>();

        initializeUI();
    }

    /**
     * Inicializa todos los componentes de la interfaz
     */
    private void initializeUI() {
        setTitle("Yu-Gi-Oh! Duel Lite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(1000, 700);

        // Panel superior: Cartas de la IA
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("IA Cards"));
        aiCardsPanel = new JPanel(new FlowLayout());
        topPanel.add(aiCardsPanel, BorderLayout.CENTER);

        aiScoreLabel = new JLabel("Score: 0");
        aiScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(aiScoreLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Panel central: Log de batalla
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Battle Log"));

        battleLog = new JTextArea(15, 40);
        battleLog.setEditable(false);
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(battleLog);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior: Cartas del jugador
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Your Cards"));
        playerCardsPanel = new JPanel(new FlowLayout());
        bottomPanel.add(playerCardsPanel, BorderLayout.CENTER);

        playerScoreLabel = new JLabel("Score: 0");
        playerScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(playerScoreLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Panel de control
        JPanel controlPanel = new JPanel();
        startButton = new JButton("Start Duel");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(e -> startDuel());

        statusLabel = new JLabel("Press 'Start Duel' to begin");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        controlPanel.add(startButton);
        controlPanel.add(statusLabel);

        add(controlPanel, BorderLayout.WEST);

        setLocationRelativeTo(null);
    }

    /**
     * Inicia el duelo cargando cartas desde la API
     */
    private void startDuel() {
        startButton.setEnabled(false);
        statusLabel.setText("Loading cards...");
        battleLog.setText("=== NEW DUEL STARTED ===\n");

        // Limpiar paneles
        playerCardsPanel.removeAll();
        aiCardsPanel.removeAll();
        cardButtons.clear();

        // Crear nuevo duelo
        duel = new Duel();
        duel.setBattleListener(this);

        // Cargar cartas en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                loadCards();

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Select a card to play!");
                    appendLog("All cards loaded. Choose your card!\n");
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error loading cards");
                    appendLog("ERROR: " + ex.getMessage() + "\n");
                    startButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                            "Failed to load cards: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    /**
     * Carga 3 cartas para cada jugador desde la API
     */
    private void loadCards() throws Exception {
        appendLog("Loading player cards...\n");
        for (int i = 0; i < 3; i++) {
            Card card = apiClient.getRandomMonsterCard();
            duel.addPlayerCard(card);

            SwingUtilities.invokeLater(() -> {
                addCardToPanel(card, playerCardsPanel, true);
            });

            appendLog("Player card " + (i + 1) + ": " + card.toString() + "\n");
        }

        appendLog("\nLoading AI cards...\n");
        for (int i = 0; i < 3; i++) {
            Card card = apiClient.getRandomMonsterCard();
            duel.addAiCard(card);

            SwingUtilities.invokeLater(() -> {
                addCardToPanel(card, aiCardsPanel, false);
            });

            appendLog("AI card " + (i + 1) + ": " + card.toString() + "\n");
        }

        appendLog("\n=== DUEL IS READY! ===\n\n");
    }

    /**
     * Agrega una carta visual al panel especificado
     */
    private void addCardToPanel(Card card, JPanel panel, boolean isPlayer) {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        cardPanel.setPreferredSize(new Dimension(150, 250));

        // Cargar imagen en segundo plano
        JLabel imageLabel = new JLabel("Loading...", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(140, 140));

        new Thread(() -> {
            try {
                URL url = new URL(card.getImageUrl());
                BufferedImage img = ImageIO.read(url);
                Image scaledImg = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> {
                    imageLabel.setIcon(new ImageIcon(scaledImg));
                    imageLabel.setText("");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    imageLabel.setText("Image error");
                });
            }
        }).start();

        cardPanel.add(imageLabel);

        // Información de la carta
        JLabel nameLabel = new JLabel("<html><center>" + card.getName() + "</center></html>");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(nameLabel);

        JLabel statsLabel = new JLabel("ATK:" + card.getAtk() + " / DEF:" + card.getDef());
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(statsLabel);

        // Si es carta del jugador, agregar botón de selección
        if (isPlayer) {
            JButton selectButton = new JButton("Play");
            selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            int cardIndex = cardButtons.size();
            selectButton.addActionListener(e -> playCard(cardIndex));

            cardButtons.add(selectButton);
            cardPanel.add(selectButton);
        }

        panel.add(cardPanel);
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Ejecuta el turno con la carta seleccionada
     */
    private void playCard(int cardIndex) {
        if (duel == null || duel.isDuelEnded()) {
            return;
        }

        // Deshabilitar todos los botones
        for (JButton btn : cardButtons) {
            btn.setEnabled(false);
        }

        duel.playTurn(cardIndex);
    }

    /**
     * Agrega texto al log de batalla
     */
    private void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            battleLog.append(text);
            battleLog.setCaretPosition(battleLog.getDocument().getLength());
        });
    }

    // Implementación de BattleListener

    @Override
    public void onTurn(String playerCard, String aiCard, String winner) {
        appendLog("--- TURN ---\n");
        appendLog("Player: " + playerCard + "\n");
        appendLog("AI: " + aiCard + "\n");
        appendLog("Winner: " + winner + "\n\n");
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        SwingUtilities.invokeLater(() -> {
            playerScoreLabel.setText("Score: " + playerScore);
            aiScoreLabel.setText("Score: " + aiScore);
        });
    }

    @Override
    public void onDuelEnded(String winner) {
        appendLog("===================\n");
        appendLog("DUEL ENDED!\n");
        appendLog("WINNER: " + winner + "\n");
        appendLog("===================\n");

        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Duel ended! Winner: " + winner);
            startButton.setEnabled(true);

            JOptionPane.showMessageDialog(this,
                    "The duel is over!\nWinner: " + winner,
                    "Duel Ended",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * Método main para iniciar la aplicación
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DuelUI ui = new DuelUI();
            ui.setVisible(true);
        });
    }
}
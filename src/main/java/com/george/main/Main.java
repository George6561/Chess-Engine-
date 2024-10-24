package com.george.main;

import com.george.stockfish.StockfishConnector;
import com.george.window.ChessWindow;
import javafx.application.Application;
import javafx.application.Platform;

import java.io.IOException;

/**
 * Main class to play a full chess game with Stockfish moving pieces on the
 * board.
 */
public class Main {

    private static ChessWindow chessWindow;
    private static StockfishConnector stockfish;

    public static void main(String[] args) {
        // Launch the ChessWindow in the JavaFX Application Thread
        Platform.startup(() -> {
            try {
                chessWindow = new ChessWindow();
                chessWindow.start(new javafx.stage.Stage());

                // Create and start a background thread for Stockfish
                Thread stockfishThread = new Thread(() -> {
                    try {
                        playGameWithStockfish();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                stockfishThread.setDaemon(true);  // Ensure it exits when the application closes
                stockfishThread.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void playGameWithStockfish() throws IOException, InterruptedException {
        stockfish = new StockfishConnector();
        StringBuilder moveHistory = new StringBuilder();  // Store the move history

        // Start Stockfish engine
        if (stockfish.startEngine()) {
            try {
                // Initialize UCI mode
                stockfish.sendCommand("uci");
                stockfish.getResponse();  // Wait for UCI initialization

                // Check if Stockfish is ready
                stockfish.sendCommand("isready");
                stockfish.getResponse();

                // Set the initial chess position to the standard starting position
                stockfish.sendCommand("position startpos");

                // This must be done on the JavaFX thread!
                Platform.runLater(() -> {
                    try {
                        chessWindow.displayChessPieces(-1, -1);  // Show the initial board
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Play moves until the game ends
                boolean gameOver = false;
                boolean isWhiteToMove = true;  // White starts the game

                while (!gameOver) {
                    // Send command to Stockfish to calculate the best move
                    stockfish.sendCommand("go movetime 1000");
                    String bestMove = stockfish.getBestMove();

                    if (bestMove == null || bestMove.isEmpty()) {
                        break;  // Game is over if no valid move is returned
                    }

                    System.out.println("Best Move: " + bestMove);

                    // Append the best move to the move history
                    if (moveHistory.length() > 0) {
                        moveHistory.append(" ");  // Separate moves with a space
                    }
                    moveHistory.append(bestMove);  // Add the current move to history

                    // Move the piece on the board (this must be done on the JavaFX thread)
                    Platform.runLater(() -> {
                        try {
                            chessWindow.movePiece(bestMove);
                            chessWindow.displayChessPieces(-1, -1);  // Update the visual board
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    // Check if the game is over (e.g., checkmate or draw)
                    if (isGameOver(bestMove)) {
                        System.out.println("Game over: " + getGameOverReason(bestMove));
                        gameOver = true;
                    } else {
                        // Update the position with the entire move history for Stockfish
                        stockfish.sendCommand("position startpos moves " + moveHistory.toString());
                    }

                    // Alternate between white and black
                    isWhiteToMove = !isWhiteToMove;

                    // Give some delay to make the game more visible
                    Thread.sleep(500);  // 0.5 second delay for better visualization
                }
            } finally {
                // Stop the Stockfish engine
                stockfish.stopEngine();
            }
        } else {
            System.out.println("Failed to start Stockfish engine.");
        }
    }

    private static boolean isGameOver(String bestMove) {
        // Simple condition to determine if the game is over (can be improved)
        return bestMove.equals("resign") || bestMove.contains("mate") || bestMove.equals("1/2-1/2");
    }

    private static String getGameOverReason(String bestMove) {
        if (bestMove.equals("resign")) {
            return "Resignation";
        } else if (bestMove.contains("mate")) {
            return "Checkmate";
        } else if (bestMove.equals("1/2-1/2")) {
            return "Draw";
        } else {
            return "Unknown end";
        }
    }
}

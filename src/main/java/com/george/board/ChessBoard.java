/*
 * Copyright (c) 2024 
 * George Miller
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * ----------------------------------------------------------------------------
 *
 * Class: ChessBoard
 *
 * This class represents a chessboard and provides methods to manage its state.
 * The chessboard is stored as an 8x8 integer array, where positive numbers represent 
 * white pieces and negative numbers represent black pieces.
 *
 * Key functionalities include:
 * - Retrieving the board state as a 2D array or a 1D array.
 * - Moving pieces on the board, handling special moves such as castling.
 * - Adding and removing pieces from specific positions on the board.
 * - Managing player turns and determining the current player.
 */

package com.george.board;

public class ChessBoard {

    // The chessboard is represented as an 8x8 2D array.
    private static final int[][] board = new int[][]{
        {-2, -3, -4, -5, -6, -4, -3, -2}, // Row 0: Black's major pieces
        {-1, -1, -1, -1, -1, -1, -1, -1}, // Row 1: Black's pawns
        {0, 0, 0, 0, 0, 0, 0, 0},         // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0},         // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0},         // Empty squares
        {0, 0, 0, 0, 0, 0, 0, 0},         // Empty squares
        {1, 1, 1, 1, 1, 1, 1, 1},         // White's pawns
        {2, 3, 4, 5, 6, 4, 3, 2}          // Row 7: White's major pieces
    };

    // Enum to represent the player's turn
    public enum Player {
        WHITE, BLACK
    }

    // The current player (whose turn it is)
    private static Player move = Player.WHITE;

    /**
     * Returns a copy of the current chessboard as a 2D array.
     * 
     * This method returns a deep copy of the 2D array to prevent direct modification
     * of the internal board state by other classes.
     *
     * @return A copy of the chessboard as a 2D integer array.
     */
    public int[][] getBoard() {
        int[][] boardCopy = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            boardCopy[i] = board[i].clone();  // Clone each row for deep copy
        }
        return boardCopy;
    }

    /**
     * Returns the chessboard as a one-dimensional array with the rating of the position 
     * as the last element. This can be used for machine learning models.
     *
     * @return The board in elements 0-63 and the move score in element 64.
     */
    public int[] getBoardArray() {
        int[] oneDimensionalBoard = new int[65];
        // TODO: Put the rating of the move in element 64 (e.g., based on Stockfish evaluation)

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                oneDimensionalBoard[(8 * y) + x] = board[y][x];
            }
        }

        return oneDimensionalBoard;
    }

    /**
     * Moves a piece from one square to another on the chessboard.
     * 
     * Special moves like castling are handled separately.
     *
     * @param fromRow The starting row of the piece.
     * @param fromCol The starting column of the piece.
     * @param toRow The destination row of the piece.
     * @param toCol The destination column of the piece.
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow >= 0 && fromRow < 8 && fromCol >= 0 && fromCol < 8
                && toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {

            int piece = board[fromRow][fromCol];
            if (piece == 0) {
                System.out.println("No piece found at the source.");
                return;  // No piece to move
            }

            // Handle castling (king moving two squares to either side)
            if (piece == 6 || piece == -6) {  // King
                if (fromCol == 4 && (toCol == 6 || toCol == 2)) {
                    if (toCol == 6) {  // Kingside castling
                        board[fromRow][4] = 0;  // Move king
                        board[fromRow][6] = piece;
                        board[fromRow][7] = 0;  // Move rook
                        board[fromRow][5] = (piece > 0) ? 2 : -2;  // Add rook
                        System.out.println("Kingside castling executed.");
                    } else if (toCol == 2) {  // Queenside castling
                        board[fromRow][4] = 0;  // Move king
                        board[fromRow][2] = piece;
                        board[fromRow][0] = 0;  // Move rook
                        board[fromRow][3] = (piece > 0) ? 2 : -2;  // Add rook
                        System.out.println("Queenside castling executed.");
                    }
                    return;
                }
            }

            // Regular move
            board[fromRow][fromCol] = 0;  // Clear original square
            board[toRow][toCol] = piece;  // Move piece to destination
        } else {
            System.out.println("Invalid move or out-of-bounds coordinates.");
        }
    }

    /**
     * Removes a piece from the specified square on the chessboard.
     *
     * @param row The row of the piece to remove.
     * @param col The column of the piece to remove.
     */
    public void removePiece(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = 0;  // Set the square to empty (0)
        } else {
            System.out.println("Invalid coordinates for removing piece.");
        }
    }

    /**
     * Adds a piece to the specified square on the chessboard.
     *
     * @param row The row where the piece will be placed.
     * @param col The column where the piece will be placed.
     * @param piece The piece to add (use positive values for white, negative for black).
     */
    public void addPiece(int row, int col, int piece) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = piece;  // Place the piece on the board
        } else {
            System.out.println("Invalid coordinates for adding piece.");
        }
    }

    /**
     * Switches the turn to the next player (White to Black or Black to White).
     */
    public void nextMove() {
        if (move == Player.WHITE) {
            move = Player.BLACK;
        } else {
            move = Player.WHITE;
        }
    }

    /**
     * Determines which player is currently allowed to move.
     *
     * @return The current player (WHITE or BLACK).
     */
    public Player currentPlayer() {
        return move;
    }

    /**
     * Returns a string representation of the chessboard.
     *
     * @return A string representing the current state of the chessboard.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];

                // Convert piece value to a character
                switch (piece) {
                    case -1: sb.append('p'); break;  // Black Pawn
                    case -2: sb.append('r'); break;  // Black Rook
                    case -3: sb.append('n'); break;  // Black Knight
                    case -4: sb.append('b'); break;  // Black Bishop
                    case -5: sb.append('q'); break;  // Black Queen
                    case -6: sb.append('k'); break;  // Black King
                    case 1: sb.append('P'); break;   // White Pawn
                    case 2: sb.append('R'); break;   // White Rook
                    case 3: sb.append('N'); break;   // White Knight
                    case 4: sb.append('B'); break;   // White Bishop
                    case 5: sb.append('Q'); break;   // White Queen
                    case 6: sb.append('K'); break;   // White King
                    default: sb.append('*'); break;  // Empty square
                }
            }
            sb.append('\n');  // New line after each row
        }

        return sb.toString();
    }
}

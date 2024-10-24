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
 * Class: StockfishConnector
 *
 * This class manages communication with the Stockfish chess engine. It provides methods
 * to start the engine, send commands, receive responses, and stop the engine. This is 
 * particularly useful for integrating Stockfish as an AI chess engine in other applications.
 *
 * Key functionalities include:
 * - Starting and stopping the Stockfish engine.
 * - Sending UCI (Universal Chess Interface) commands to the engine.
 * - Receiving responses and best move suggestions from Stockfish.
 */
package com.george.stockfish;

import java.io.*;

public class StockfishConnector {

    private Process stockfish;
    private BufferedReader input;
    private BufferedWriter output;

    /**
     * Starts the Stockfish chess engine by launching a process for the engine
     * executable.
     *
     * - It attempts to start the Stockfish engine using the specified path. -
     * Initializes the input and output streams for communicating with
     * Stockfish.
     *
     * @return true if the engine starts successfully, false otherwise.
     */
    public boolean startEngine() {
        try {
            // Provide the relative path to the Stockfish executable
            String pathToStockfish = "stockfish/stockfish-windows-x86-64-avx2";
            stockfish = new ProcessBuilder(pathToStockfish).start();
            input = new BufferedReader(new InputStreamReader(stockfish.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(stockfish.getOutputStream()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a UCI (Universal Chess Interface) command to the Stockfish engine.
     *
     * - This method writes the given command to the Stockfish input stream. -
     * Commands should follow the UCI protocol (e.g., "uci", "isready",
     * "position", "go").
     *
     * @param command The UCI command to send to Stockfish.
     * @throws IOException If an error occurs while writing to Stockfish.
     */
    public void sendCommand(String command) throws IOException {
        output.write(command + "\n");
        output.flush();  // Ensure the command is sent immediately
    }

    /**
     * Reads the response from the Stockfish engine after sending a command.
     *
     * - This method captures the output of Stockfish line-by-line. - It listens
     * for key phrases such as "uciok", "readyok", or "bestmove" to determine
     * the end of the response.
     *
     * @return The response from Stockfish as a String.
     * @throws IOException If an error occurs while reading from Stockfish.
     */
    public String getResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        // Read all output from Stockfish until we get an indication that the command is complete
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
            // End the response based on specific keywords
            if (line.equals("uciok") || line.startsWith("bestmove") || line.equals("readyok")) {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves the best move suggested by Stockfish after evaluating a
     * position.
     *
     * - This method reads Stockfish's output until it finds the line starting
     * with "bestmove". - The best move is extracted from the response (e.g.,
     * "bestmove e2e4").
     *
     * @return The best move suggested by Stockfish as a string (e.g., "e2e4").
     * @throws IOException If an error occurs while reading from Stockfish.
     */
    public String getBestMove() throws IOException {
        String bestMove = null;
        StringBuilder sb = new StringBuilder();
        String line;

        // Read the output from Stockfish and look for the "bestmove" line
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");

            // If the output contains "bestmove", extract the move
            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");  // Split the line by spaces
                bestMove = parts[1];  // The best move is the second element (e.g., e2e4)
                break;  // Stop once the best move is found
            }
        }

        return bestMove;
    }

    /**
     * Stops the Stockfish engine by sending the "quit" command and terminating
     * the process.
     *
     * - Sends the "quit" command to Stockfish, which gracefully shuts it down.
     * - Destroys the process associated with the engine.
     */
    public void stopEngine() {
        try {
            sendCommand("quit");  // Tell Stockfish to quit
            stockfish.destroy();  // Terminate the Stockfish process
        } catch (IOException e) {
            e.printStackTrace();  // Handle any IO exceptions during the shutdown process
        }
    }

}

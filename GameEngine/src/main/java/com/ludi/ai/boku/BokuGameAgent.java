package com.ludi.ai.boku;

import game.Game;
import other.AI;

import other.context.Context;
import other.move.Move;

import com.ludi.ai.boku.search.*;
import com.ludi.ai.boku.heuristics.*;

public class BokuGameAgent extends AI implements IGameAgent {

    private int player = -1;
    private Game currentGame = null;
    private ISearch searchTechnique = null;
    private long stopTime = 0;// System.currentTimeMillis();
    private final static int MAXDEPTH=100;

    public BokuGameAgent() {
        this.friendlyName = "Boku Game Agent";
    }

    public Game game() {
        return currentGame;
    }

    @Override
    public Move selectAction(
            final Game game,
            final Context context,
            final double maxSeconds,
            final int maxIterations,
            final int maxDepth) {
        this.currentGame = game;
        stopTime = maxSeconds > 0 ? (System.currentTimeMillis() + (long)(maxSeconds * 1000)) : -1;
        int maxdepth = maxDepth < 0 ? MAXDEPTH : maxDepth;
        final Context copycontext = copyContext(context);
        return this.searchTechnique.searchBestMove(copycontext, maxSeconds, maxIterations, maxdepth);
    }

    @Override
    public void initAI(final Game game, final int playerID) {
        this.player = playerID;
        /* this.searchTechnique = new AlphaBeta(this, new MoveManager(this), new LineCompletionHeuristicManager(),
                new ZobristTranspositionTable()); */

        /* this.searchTechnique = new NegaMax(this, new MoveManager(this), new LineCompletionHeuristicManagerNegaMax(),
                new ZobristTranspositionTable()); */

        this.searchTechnique = new NegaMaxPV(this, new MoveManager(this), new LineCompletionHeuristicManagerNegaMax(),
                new ZobristTranspositionTable());

        this.searchTechnique.initialize(playerID);
    }

    @Override
    public boolean supportsGame(final Game game) {
        if (game.isStochasticGame())
            return false;

        if (!game.isAlternatingMoveGame())
            return false;

        return true;
    }

    @Override
    public void closeAI() {
        // TODO: [LOW] Send reset to Search and Heuristics for cleanup instead.
        this.searchTechnique = null;
    }

    @Override
    public boolean isSearchTimeElapsed() {
        if (stopTime == -1)
            return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime >= stopTime)
            return true;

        return false;

    }
}
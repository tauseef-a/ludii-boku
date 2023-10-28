package com.ludi.ai.boku;

import game.Game;
import other.AI;

import other.context.Context;
import other.move.Move;

import com.ludi.ai.boku.search.*;
import com.ludi.ai.boku.heuristics.*;


public class BokuGameAgent extends AI {

    protected int player = -1;
    Game currentGame = null;
    ISearch searchTechnique = null;

    public BokuGameAgent() {
        this.friendlyName = "Boku Game Agent";
    }

    public Game game()
    {
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
        final Context copycontext = copyContext(context);
        return this.searchTechnique.searchBestMove( copycontext, maxSeconds, maxIterations, maxDepth);
    }

    @Override
    public void initAI(final Game game, final int playerID) {
        this.player = playerID;
        this.searchTechnique = new AlphaBeta(new MoveManager(this), new LineCompletionHeuristicManager(), 
                new ZobristTranspositionTable());
        /* this.searchTechnique = new NegaMax(new MoveManager(this), new LineCompletionHeuristicManagerNegaMax(),
                new ZobristTranspositionTable()); */
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
        this.searchTechnique = null;
    }
}
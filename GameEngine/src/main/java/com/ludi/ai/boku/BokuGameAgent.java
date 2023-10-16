package com.ludi.ai.boku;

import game.Game;
import main.collections.FastArrayList;
import other.AI;

import other.context.Context;
import other.move.Move;


import com.ludi.ai.boku.search.Search;
import com.ludi.ai.boku.search.AlphaBeta;

public class BokuGameAgent extends AI implements MoveManager {

    // -------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;
    Game currentGame = null;
    Search searchTechnique = null;

    // -------------------------------------------------------------------------

    /**
     * Constructor
     */
    public BokuGameAgent() {
        this.friendlyName = "Boku Game Agent";
    }

    @Override
    public final Context setMoveAsCurrent(final Context currentContext, final Move move) {
        final Context copycontext = copyContext(currentContext);
        this.currentGame.apply(copycontext, move);
        return copycontext;
    }

    @Override
    public FastArrayList<Move> getCurrentMoves(final Context currentContext) {
        return this.currentGame.moves(currentContext).moves();
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
        return this.searchTechnique.searchBestMove(this, copycontext, maxSeconds, maxIterations, maxDepth);
    }

    @Override
    public void initAI(final Game game, final int playerID) {
        this.player = playerID;
        this.searchTechnique = new AlphaBeta(new LineCompletionHeuristicManager());
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
}
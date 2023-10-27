package com.ludi.ai.boku;

import game.Game;
import main.collections.FastArrayList;
import other.AI;

import other.context.Context;
import other.move.Move;

import com.ludi.ai.boku.search.ISearch;
import com.ludi.ai.boku.search.AlphaBeta;
import com.ludi.ai.boku.search.NegaMax;

import app.utils.TrialUtil;

public class BokuGameAgent extends AI implements IMoveManager {

    protected int player = -1;
    Game currentGame = null;
    ISearch searchTechnique = null;

    public BokuGameAgent() {
        this.friendlyName = "Boku Game Agent";
    }

    @Override
    public final Context setMoveAsCurrent(final Context currentContext, final Move move) {
        Context copycontext = copyContext(currentContext);
        Move m = this.currentGame.apply(copycontext, move);
        if (m == null)
            copycontext = null;
        return copycontext;
    }

    @Override
    public FastArrayList<Move> getCurrentMoves(final Context currentContext) {
        return this.currentGame.moves(currentContext).moves();
    }

    @Override
    public int getCurrentPly(final Context currentContext)
    {
        //This seems to work for now considering Capture and Undo State of Game
        return currentContext.trial().previousState().size();
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
        //this.searchTechnique = new AlphaBeta(new LineCompletionHeuristicManager());
        this.searchTechnique = new AlphaBeta(new LineCompletionHeuristicManager(), 
                new ZobristTranspositionTable());
        /* this.searchTechnique = new NegaMax(new LineCompletionHeuristicManagerNegaMax(),
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
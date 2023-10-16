package com.ludi.ai.boku.search;

import java.io.BufferedWriter;

import com.ludi.ai.boku.HeuristicsManager;
import com.ludi.ai.boku.MoveManager;

import game.Game;
import main.collections.FastArrayList;

import other.context.Context;
import other.move.Move;

public class AlphaBeta implements Search {

    // -------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;
    protected HeuristicsManager heuristicsEngine = null;

    BufferedWriter fileout = null;
    Game currentGame = null;

    // -------------------------------------------------------------------------

    /**
     * Constructor
     */
    public AlphaBeta(HeuristicsManager heuristicsEngine) {

        this.heuristicsEngine = heuristicsEngine;

    }

    public void initialize(final int playerID) {
        this.player = playerID;

    }

    public void reset() {

    }

    private float alphabetaSearch(
            final MoveManager moveEngine,
            final Context context,
            int depth,
            float alpha,
            float beta,
            boolean isMaximizing) {
        FastArrayList<Move> legalmoves = moveEngine.getCurrentMoves(context);
        if (depth == 0 || legalmoves.size() == 0) {
            float hrsvalue = this.heuristicsEngine.evaluateMove(context, this.player);
            return hrsvalue;
        }

        if (isMaximizing) {
            float maxvalue = 0.00f;
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveEngine.setMoveAsCurrent(context, m);
                float score = alphabetaSearch(moveEngine, copycontext, depth - 1, alpha, beta, false);
                if (score > alpha) {
                    alpha = score;
                }
                if (score > beta) {
                    return alpha;
                }

            }
            return alpha;
        } else {
            float minvalue = 1000.00f;
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveEngine.setMoveAsCurrent(context, m);
                float score = alphabetaSearch(moveEngine, copycontext, depth - 1, alpha, beta, true);

                if (score < beta)
                    beta = score;
                if (score < alpha) {
                    return beta;
                }
            }
            return beta;
        }
    }

    private Move iterativeDeepening(
            final MoveManager moveEngine,
            final Context context) {
        FastArrayList<Move> legalmoves = moveEngine.getCurrentMoves(context);
        Move bestmove = legalmoves.get(0);
        // final Context copycontext = copyContext(context);
        int initialdepth = 1;
        int finaldepth = 2;//TODO: Increase this
        float alpha = -10000.00f;
        // float beta = 10000.00f;
        while (initialdepth < finaldepth) {
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                // float alpha = -10000.00f;
                float beta = 10000.00f;
                final Context copycontext = moveEngine.setMoveAsCurrent(context, m);
                float score = alphabetaSearch(moveEngine, copycontext, initialdepth, alpha, beta, true);
                if (score > alpha) {
                    alpha = score;
                    bestmove = legalmoves.get(i);
                }
                if (alpha > beta) {
                    return bestmove;
                }
            }

            initialdepth++;
        }

        return bestmove;

    }

    public Move searchBestMove(final MoveManager moveEngine, final Context context, final double maxSeconds,
            final int maxIterations, final int maxDepth)

    {
        return iterativeDeepening(moveEngine, context);

    }
}
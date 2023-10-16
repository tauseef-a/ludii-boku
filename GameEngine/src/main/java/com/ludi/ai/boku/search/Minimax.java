package com.ludi.ai.boku.search;

import java.io.BufferedWriter;

import com.ludi.ai.boku.MoveManager;

import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import metadata.ai.heuristics.Heuristics;


public class Minimax implements Search {

    // -------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;
    protected Heuristics heuristicValueFunction = null;
    public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.001f;
    BufferedWriter fileout = null;

    // -------------------------------------------------------------------------

    /**
     * Constructor
     */
    public Minimax() {

    }

    public void initialize(final int playerID) {

    }

    public void reset() {

    }

    @Override
    public Move searchBestMove(MoveManager moveEngine, final Context context, final double maxSeconds, final int maxIterations, final int maxDepth) {
        return iterativeDeepening(moveEngine, context);
    }

    private float minimaxSearch(
            final MoveManager moveEngine,
            final Context context,
            int depth,
            boolean isMaximizing) {
        FastArrayList<Move> legalmoves = moveEngine.getCurrentMoves(context);
        if (depth == 0 || legalmoves.size() == 0) {
            return this.heuristicValueFunction.computeValue(context, this.player, ABS_HEURISTIC_WEIGHT_THRESHOLD);
        }
        if (isMaximizing) {
            float maxvalue = 0.00f;
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveEngine.setMoveAsCurrent(context, m);

                float score = minimaxSearch(moveEngine, copycontext, depth - 1, false);
                if (score > maxvalue) {
                    maxvalue = score;
                }
            }
            return maxvalue;

        } else {
            float minvalue = 1000.00f;
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveEngine.setMoveAsCurrent(context, m);
                float score = minimaxSearch(moveEngine, copycontext, depth - 1, true);
                if (score < minvalue) {
                    minvalue = score;
                }
            }
            return minvalue;
        }
    }

    private Move iterativeDeepening(
            final MoveManager moveEngine,
            final Context context) {
        FastArrayList<Move> legalmoves = moveEngine.getCurrentMoves(context);
        Move bestmove = legalmoves.get(0);
        int initialdepth = 1;
        int finaldepth = 4;

        while (initialdepth < finaldepth) {
            for (int i = 0; i < legalmoves.size(); ++i) {
                float maxvalue = 0.00f;
                final Move m = legalmoves.get(i);
                final Context copycontext = moveEngine.setMoveAsCurrent(context, m);

                float score = minimaxSearch(moveEngine, copycontext, initialdepth, true);
                if (score > maxvalue) {
                    maxvalue = score;
                    bestmove = legalmoves.get(i);
                }
            }

            initialdepth++;
        }
        return bestmove;
    }

    private Move getBestMove(
            final MoveManager moveEngine,
            final Context context) {
        return iterativeDeepening(moveEngine, context);
    }

}
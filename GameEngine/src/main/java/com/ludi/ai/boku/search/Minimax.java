package com.ludi.ai.boku.search;

import java.io.BufferedWriter;

import com.ludi.ai.boku.IMoveManager;

import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import metadata.ai.heuristics.Heuristics;


public class Minimax implements ISearch {

    // -------------------------------------------------------------------------

    /** Our player index */
    private int player = -1;
    private Heuristics heuristicValueFunction = null;
    private IMoveManager moveManager = null;
    private static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.001f;
    //private BufferedWriter fileout = null;

    // -------------------------------------------------------------------------

    /**
     * Constructor
     */
    public Minimax(IMoveManager moveManager) {
        this.moveManager = moveManager;
    }

    public void initialize(final int playerID) {

    }

    public void reset() {

    }

    @Override
    public Move searchBestMove( final Context context, final double maxSeconds, final int maxIterations, final int maxDepth) {
        return iterativeDeepening( context);
    }

    private float minimaxSearch(
            final IMoveManager moveManager,
            final Context context,
            int depth,
            boolean isMaximizing) {
        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context);
        if (depth == 0 || legalmoves.size() == 0) {
            return this.heuristicValueFunction.computeValue(context, this.player, ABS_HEURISTIC_WEIGHT_THRESHOLD);
        }
        if (isMaximizing) {
            float maxvalue = 0.00f;
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveManager.setMoveAsCurrent(context, m);

                float score = minimaxSearch(moveManager, copycontext, depth - 1, false);
                if (score > maxvalue) {
                    maxvalue = score;
                }
            }
            return maxvalue;

        } else {
            float minvalue = 1000.00f;
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveManager.setMoveAsCurrent(context, m);
                float score = minimaxSearch(moveManager, copycontext, depth - 1, true);
                if (score < minvalue) {
                    minvalue = score;
                }
            }
            return minvalue;
        }
    }

    private Move iterativeDeepening(
            final Context context) {
        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context);
        Move bestmove = legalmoves.get(0);
        int initialdepth = 1;
        int finaldepth = 4;

        while (initialdepth < finaldepth) {
            for (int i = 0; i < legalmoves.size(); ++i) {
                float maxvalue = 0.00f;
                final Move m = legalmoves.get(i);
                final Context copycontext = moveManager.setMoveAsCurrent(context, m);

                float score = minimaxSearch(moveManager, copycontext, initialdepth, true);
                if (score > maxvalue) {
                    maxvalue = score;
                    bestmove = legalmoves.get(i);
                }
            }

            initialdepth++;
        }
        return bestmove;
    }

}
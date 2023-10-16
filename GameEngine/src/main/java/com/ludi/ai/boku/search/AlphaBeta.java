package com.ludi.ai.boku.search;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.ludi.ai.boku.HeuristicsManager;
import com.ludi.ai.boku.MoveManager;

import main.collections.FastArrayList;

import other.context.Context;
import other.move.Move;

public class AlphaBeta implements Search {

    // -------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;
    protected HeuristicsManager heuristicsManager = null;
    private static final boolean BENCHMARK = true;

    private static final int SEARCH = 0;
    private static final int HEURISTICS = 1;
    private static ArrayList<Long> benchmarkdata = new ArrayList<Long>(2);

    private BufferedWriter benchmarkFile = null;

    // -------------------------------------------------------------------------

    /**
     * Constructor
     */
    public AlphaBeta(HeuristicsManager heuristicsManager) {

        this.heuristicsManager = heuristicsManager;
        if (BENCHMARK) {
            initializeBenchmark();
        }

    }

    private void initializeBenchmark()
    {
        try {
            for(int i=0;i<2;i++) benchmarkdata.add(0L);
            FileWriter fstream = new FileWriter("C:\\Tauseef_A\\Workspace_Coding\\ISG\\out.txt", true);
            benchmarkFile = new BufferedWriter(fstream);
            benchmarkFile.write("Start\n");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void publishBenchmark()
    {
        try {
            benchmarkFile.write(this.toString());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void saveCurrentBenchmark()
    {
        try {
            benchmarkFile.flush();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void initialize(final int playerID) {
        this.player = playerID;

    }

    public void reset() {

    }

    private float alphabetaSearch(
            final MoveManager moveManager,
            final Context context,
            int depth,
            float alpha,
            float beta,
            boolean isMaximizing) {
        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context);
        if (depth == 0 || legalmoves.size() == 0) {
            if (BENCHMARK) {
                long heuristicTime = System.currentTimeMillis();
                float hrsvalue = this.heuristicsManager.evaluateMove(context, this.player);
                heuristicTime = System.currentTimeMillis() - heuristicTime;
                benchmarkdata.set(HEURISTICS, benchmarkdata.get(HEURISTICS) + heuristicTime);
                return hrsvalue;

            } else {
                float hrsvalue = this.heuristicsManager.evaluateMove(context, this.player);
                return hrsvalue;
            }
        }

        if (isMaximizing) {
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveManager.setMoveAsCurrent(context, m);
                float score = alphabetaSearch(moveManager, copycontext, depth - 1, alpha, beta, false);
                if (score > alpha) {
                    alpha = score;
                }
                if (score > beta) {
                    return alpha;
                }

            }
            return alpha;
        } else {
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                final Context copycontext = moveManager.setMoveAsCurrent(context, m);
                float score = alphabetaSearch(moveManager, copycontext, depth - 1, alpha, beta, true);

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
            final MoveManager moveManager,
            final Context context) {
        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context);
        Move bestmove = legalmoves.get(0);
        // final Context copycontext = copyContext(context);
        int initialdepth = 1;
        int finaldepth = 2;//TODO: Increase this
        float alpha = -10000.00f;
        // float beta = 10000.00f;
        while (initialdepth < finaldepth) {
            long searchTime = System.currentTimeMillis();
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                // float alpha = -10000.00f;
                float beta = 10000.00f;
                final Context copycontext = moveManager.setMoveAsCurrent(context, m);
                float score = alphabetaSearch(moveManager, copycontext, initialdepth, alpha, beta, true);
                if (score > alpha) {
                    alpha = score;
                    bestmove = legalmoves.get(i);
                }
                if (alpha > beta) {
                    return bestmove;
                }
            }
            searchTime = System.currentTimeMillis() - searchTime;
            benchmarkdata.set(SEARCH, benchmarkdata.get(SEARCH) + searchTime);
            publishBenchmark();

            initialdepth++;
        }
        saveCurrentBenchmark();
        return bestmove;

    }

    public Move searchBestMove(final MoveManager moveManager, final Context context, final double maxSeconds,
            final int maxIterations, final int maxDepth)

    {
        return iterativeDeepening(moveManager, context);

    }

    @Override
    public String toString()
    {
        final StringBuilder objectString = new StringBuilder();
        objectString.append("(Alphabeta ");
        objectString.append("Search: " + benchmarkdata.get(SEARCH).toString());
        objectString.append("Heuristics: " + benchmarkdata.get(HEURISTICS).toString());
        objectString.append(") ");
        objectString.append(System.lineSeparator());

        return objectString.toString();
    }
}
package com.ludi.ai.boku.search;

/* import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List; */

import com.ludi.ai.boku.heuristics.IHeuristicsManager;
import com.ludi.ai.boku.IMoveManager;
import com.ludi.ai.boku.ITranspositionTable;
import com.ludi.ai.boku.ITranspositionTable.TranspositionTableEntry;

import main.collections.FastArrayList;

import other.context.Context;
import other.move.Move;

public class AlphaBeta implements ISearch {

    protected int player = -1;
    protected IHeuristicsManager heuristicsManager = null;
    private IMoveManager moveManager = null;
    private static final float ALPHA = -10000.00f;
    private static final float BETA = 10000.00f;
    private int maximizingPlayer;
    private ITranspositionTable tTable;
    

    /* private static final boolean BENCHMARK = true;
    private static final int SEARCH = 0;
    private static final int HEURISTICS = 1;
    private static ArrayList<Long> benchmarkdata = new ArrayList<Long>(2);
    private BufferedWriter benchmarkFile = null; */

    public AlphaBeta(IMoveManager movemanager, IHeuristicsManager heuristicsManager, ITranspositionTable ttable) {

        this.heuristicsManager = heuristicsManager;
        this.moveManager = movemanager;
        this.tTable = ttable;
        /*
         * if (BENCHMARK) {
         * initializeBenchmark();
         * }
         */

    }

    public void initialize(final int playerID) {
        this.player = playerID;

    }

    public void reset() {

    }

    private float alphabetaSearch(
            final Context context,
            int depth,
            float alpha,
            float beta,
            boolean isMaximizing,
            final int currentPly) {
        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context,currentPly);

        float old_alpha = alpha;
        float old_beta = beta;
        this.heuristicsManager.setContext(context);
        byte[][] ownedPieces = this.heuristicsManager.getBoardPieceStatus();
        TranspositionTableEntry tEntry = tTable.Retrieve(ownedPieces);
        if (tEntry != null) {
            if (tEntry.depth >= depth) {
                if (tEntry.flag == ITranspositionTable.FLAG_VALID)
                    return tEntry.score;
                else if (tEntry.flag == ITranspositionTable.FLAG_UBOUND)
                    beta = Math.min(beta, tEntry.score);
                else if (tEntry.flag == ITranspositionTable.FLAG_LBOUND)
                    alpha = Math.max(alpha, tEntry.score);

                if (alpha >= beta)
                    return tEntry.score;
            }
        }

        if (depth == 0 || legalmoves.size() == 0) {
            /* if (BENCHMARK) {
                long heuristicTime = System.currentTimeMillis();
                float hrsvalue = this.heuristicsManager.evaluateMove(context, maximizingPlayer);
                heuristicTime = System.currentTimeMillis() - heuristicTime;
                benchmarkdata.set(HEURISTICS, benchmarkdata.get(HEURISTICS) + heuristicTime);
                return hrsvalue;

            } else  */{
                float hrsvalue = this.heuristicsManager.evaluateMove(context, maximizingPlayer);
                return hrsvalue;
            }
        }

        // Examine TT Move First
        boolean isTTMoveGood = false;
        Move bestMove = null;
        float bestScore = 0;
        if (tEntry != null && tEntry.depth >= 0 && tEntry.move != null) {
            final Context copycontext = moveManager.setMoveAsCurrent(context, tEntry.move);
            if (copycontext != null) {
                float value = alphabetaSearch( copycontext, depth - 1, alpha, beta, !isMaximizing,currentPly+1);
                if(isMaximizing)
                {
                    if (value >= beta) {
                        bestScore = value;
                        isTTMoveGood = true;
                        bestMove = tEntry.move;
                        moveManager.storeKillerMove(bestMove, currentPly);
                    }

                }
                else //if Minimizing Player
                {
                    if (value <= alpha) {
                        bestScore = value;
                        isTTMoveGood = true;
                        bestMove = tEntry.move;
                        moveManager.storeKillerMove(bestMove, currentPly);
                    }

                }

            }
        }

        if(!isTTMoveGood)
        {
            boolean moveFound = false;
            boolean tMoveInvalid = tEntry != null ? (tEntry.move == null) : true;
            if (isMaximizing) {
                bestScore = ALPHA;
                for (int i = 0; i < legalmoves.size(); ++i) {
                    final Move m = legalmoves.get(i);
                    if (!moveFound && !tMoveInvalid && m.equals(tEntry.move)) {
                        moveFound = true;
                        continue;
                    }
                    final Context copycontext = moveManager.setMoveAsCurrent(context, m);
                    if (copycontext == null)
                        continue;
                    float score = alphabetaSearch(copycontext, depth - 1, alpha, beta, !isMaximizing/* true */,currentPly+1);
                    if(score > bestScore)
                    {
                        bestScore = score;
                        bestMove = m;
                    }
                    if (score > alpha) {
                        alpha = score;
                    }
                    if (score >= beta) {
                        moveManager.storeKillerMove(bestMove, currentPly);
                        break;//return alpha;
                    }

                }
                //return alpha;
            } else {
                bestScore = BETA;
                for (int i = 0; i < legalmoves.size(); ++i) {
                    final Move m = legalmoves.get(i);
                    if (!moveFound && !tMoveInvalid && m.equals(tEntry.move)) {
                        moveFound = true;
                        continue;
                    }
                    final Context copycontext = moveManager.setMoveAsCurrent(context, m);
                    if (copycontext == null)
                        continue;
                    float score = alphabetaSearch( copycontext, depth - 1, alpha, beta, !isMaximizing/* false */,currentPly+1);
                    if(score < bestScore)
                    {
                        bestScore = score;
                        bestMove = m;
                    }
                    if (score < beta)
                        beta = score;
                    if (score <= alpha) {
                        moveManager.storeKillerMove(bestMove, currentPly);
                        break;//return beta;
                    }
                }
                //return beta;
            }
        }

        {
            byte flag;
            if (bestScore <= old_alpha)
                flag = ITranspositionTable.FLAG_UBOUND;
            else if (bestScore >= old_beta)
                flag = ITranspositionTable.FLAG_LBOUND;
            else
                flag = ITranspositionTable.FLAG_VALID;
            if (tEntry != null)
                tTable.Save(tEntry.key, bestMove, bestScore, flag, (byte) depth);
            else
                tTable.Save(ownedPieces, bestMove, bestScore, flag, (byte) depth);
        }
        return bestScore;
    }

    private Move alphaBetaRootSearch(
            final Context context,
            final int depth,
            final int CurrentPly) {
        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context,CurrentPly);
        Move bestmove = legalmoves.get(0);
        float alpha = ALPHA;
        float beta = BETA;

        for (int i = 0; i < legalmoves.size(); ++i) {
            final Move m = legalmoves.get(i);
            final Context copycontext = moveManager.setMoveAsCurrent(context, m);
            if (copycontext == null)
                continue;

            float score = alphabetaSearch(copycontext, depth, alpha, beta, false,CurrentPly+1);
            if (score > alpha) {
                alpha = score;
                bestmove = legalmoves.get(i);
            }
            if (alpha >= beta) {
                //Storing this may be useful only in case of Undo Move???
                moveManager.storeKillerMove(bestmove, CurrentPly);
                return bestmove;
            }
        }

        // saveCurrentBenchmark();
        return bestmove;

    }

    private Move doIterativeDeepening(
            final Context context) {
        Move bestmove = null;
        int initialdepth = 0;
        int finaldepth = 3;// TODO: Increase this
        maximizingPlayer = context.state().playerToAgent(context.state().mover());
        int currentPly = moveManager.getCurrentPly(context);
        
        while (initialdepth < finaldepth) {
            bestmove = alphaBetaRootSearch(context, initialdepth,currentPly);
            initialdepth++;
        }
        // saveCurrentBenchmark();
        return bestmove;
    }

    public Move searchBestMove(final Context context, final double maxSeconds,
            final int maxIterations, final int maxDepth)

    {
        return doIterativeDeepening(context);

    }

    /* private void initializeBenchmark() {
        try {
            for (int i = 0; i < 2; i++)
                benchmarkdata.add(0L);
            FileWriter fstream = new FileWriter("C:\\Tauseef_A\\Workspace_Coding\\ISG\\out.txt", true);
            benchmarkFile = new BufferedWriter(fstream);
            benchmarkFile.write("Start\n");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void publishBenchmark() {
        try {
            benchmarkFile.write(getBenchmarkData());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void saveCurrentBenchmark() {
        try {
            benchmarkFile.flush();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public String getBenchmarkData() {
        final StringBuilder objectString = new StringBuilder();
        objectString.append("Search: " + benchmarkdata.get(SEARCH).toString());
        objectString.append(" ,Heuristics: " + benchmarkdata.get(HEURISTICS).toString());
        objectString.append(System.lineSeparator());

        return objectString.toString();
    } */

}
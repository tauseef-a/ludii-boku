package com.ludi.ai.boku.search;

/* import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList; */

import com.ludi.ai.boku.heuristics.IHeuristicsManager;
import com.ludi.ai.boku.IGameAgent;
import com.ludi.ai.boku.IMoveManager;
import com.ludi.ai.boku.ITranspositionTable;
import com.ludi.ai.boku.ITranspositionTable.TranspositionTableEntry;

import main.collections.FastArrayList;

import other.context.Context;
import other.move.Move;

public class NegaMax implements ISearch {

    // -------------------------------------------------------------------------

    private IHeuristicsManager heuristicsManager = null;
    private IMoveManager moveManager = null;
    private IGameAgent gameAgent = null;
    //private static final boolean BENCHMARK = true;
    private static final float ALPHA = -1000.00f;
    private static final float BETA = 1000.00f;
    private int maximizingPlayer = 0;;
    private ITranspositionTable tTable;

    //Benchmark Data
    /* private static final int SEARCH = 0;
    private static final int HEURISTICS = 1;
    private static ArrayList<Long> benchmarkdata = new ArrayList<Long>(2);
    private BufferedWriter benchmarkFile = null; */

    // -------------------------------------------------------------------------

    /**
     * Constructor
     */
    public NegaMax(IGameAgent agent, IMoveManager movemanager, IHeuristicsManager heuristicsManager, ITranspositionTable ttable) {

        this.gameAgent = agent;
        this.moveManager = movemanager;
        this.heuristicsManager = heuristicsManager;
        this.tTable = ttable;
        /* if (BENCHMARK) {
            initializeBenchmark();
        } */

    }

    public void initialize(final int playerID) {

    }

    public void reset() {
    }

    /*
     * To handle capture scenarios, where there are two moves handled by same player.
     * This API tries to correct the alpha,beta values in such case.
     */
    private float negamaxSearchABOrder(
            final Context context,
            int depth,
            float alpha,
            float beta,
            final int currentPly,
            final int previousPlayer) {

        int currentPlayer = context.state().playerToAgent(context.state().mover());
        if(currentPlayer != previousPlayer)
            return negamaxSearch(context, depth, alpha, beta, currentPly);
        else
            return negamaxSearch(context, depth+1, -beta, -alpha, currentPly);

    }

    private float negamaxSearch(
            final Context context,
            int depth,
            float alpha,
            float beta,
            final int currentPly) {

        /*
         * First check the TT, to extract possible details regarding current
         * move, like alpha,beta, or the exact game heuristic value of the move.
         * If exact value found, same can be returned, else improve alpha or beta
         * based on available information
         */
        this.heuristicsManager.setContext(context);
        float old_alpha = alpha;
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
        int currentPlayer = context.state().playerToAgent(context.state().mover());
        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context,currentPly);
        if (depth == 0 || legalmoves.size() == 0) {
            /* if (BENCHMARK) {
                long heuristicTime = System.currentTimeMillis();
                float hrsvalue = this.heuristicsManager.evaluateMove(context);
                heuristicTime = System.currentTimeMillis() - heuristicTime;
                benchmarkdata.set(HEURISTICS, benchmarkdata.get(HEURISTICS) + heuristicTime);
                return hrsvalue;

            } else  */{
                float hrsvalue = this.heuristicsManager.evaluateMove(context);
                return hrsvalue;
            }
        }

        // Examine TT Move First
        boolean isTTMoveGood = false;
        float bestScore = ALPHA;
        Move bestMove = null;
        if (tEntry != null && tEntry.depth >= 0 && tEntry.move != null) {
            final Context copycontext = moveManager.setMoveAsCurrent(context, tEntry.move);
            if (copycontext != null && !gameAgent.isSearchTimeElapsed()) {
                float value = -negamaxSearchABOrder( copycontext, depth - 1, -beta, -alpha,currentPly+1,currentPlayer);
                if (value >= beta) {
                    bestScore = value;
                    isTTMoveGood = true;
                    bestMove = tEntry.move;
                    moveManager.storeKillerMove(bestMove, currentPly);
                }
            }
        }

        /*
         * Move from TT was not found to be good, so we need to run typical
         *  Negamax algorithm now
         */
        if (!isTTMoveGood) {
            bestScore = ALPHA;
            boolean moveFound = false;
            boolean tMoveInvalid = tEntry != null ? (tEntry.move == null) : true;
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                if (!moveFound && !tMoveInvalid && m.equals(tEntry.move)) {
                    moveFound = true;
                    continue;
                }
                final Context copycontext = moveManager.setMoveAsCurrent(context, m);
                if (copycontext == null)
                    continue;
                float value = -negamaxSearchABOrder( copycontext, depth - 1, -beta, -alpha,currentPly+1,currentPlayer);
                if(gameAgent.isSearchTimeElapsed()) break;
                if (value > bestScore) {
                    bestScore = value;
                    bestMove = m;
                }
                if (bestScore > alpha) {
                    alpha = bestScore;
                }
                if (bestScore >= beta) {
                    moveManager.storeKillerMove(bestMove, currentPly);
                    break;// return score; // Fail-Soft Value
                }
            }
        }

        if(!gameAgent.isSearchTimeElapsed()){
            byte flag;
            if (bestScore <= old_alpha)
                flag = ITranspositionTable.FLAG_UBOUND;
            else if (bestScore > beta)
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

    private Move negamaxSearchMove(
            final IMoveManager moveManager,
            final Context context,
            final int depth,
            final int currentPly) {

        FastArrayList<Move> legalmoves = moveManager.getCurrentMoves(context,currentPly);
        Move bestmove = legalmoves.get(0);
        
        float alpha = ALPHA;
        float beta = BETA;
        //long searchTime = System.currentTimeMillis();
        for (int i = 0; i < legalmoves.size(); ++i) {
            final Move m = legalmoves.get(i);
            final Context copycontext = moveManager.setMoveAsCurrent(context, m);

            float score = -negamaxSearch( copycontext, depth, alpha, beta,currentPly+1);
            if(gameAgent.isSearchTimeElapsed()) break;
            if (score > alpha) {
                alpha = score;
                bestmove = legalmoves.get(i);
            }
            if (alpha >= beta) {
                moveManager.storeKillerMove(bestmove, currentPly);
                break;
            }
        }
        /* searchTime = System.currentTimeMillis() - searchTime;
        benchmarkdata.set(SEARCH, benchmarkdata.get(SEARCH) + searchTime);
        publishBenchmark();

        saveCurrentBenchmark(); */
        return bestmove;

    }

    private Move doIterativeDeepening(
            final Context context,
            final int maxDepth) {

        Move bestmove = moveManager.getCurrentMoves(context).get(0);
        int currentPly = moveManager.getCurrentPly(context);
        //maximizingPlayer = context.state().playerToAgent(context.state().mover());
        Move nextBestmove = bestmove;
        int initialdepth = 0;
        while (initialdepth <= maxDepth) {
            nextBestmove = negamaxSearchMove(moveManager, context, initialdepth,currentPly);
            if(gameAgent.isSearchTimeElapsed()) break;
            bestmove = nextBestmove;
            initialdepth++;
        }

        return bestmove;

    }

    public Move searchBestMove( final Context context, final double maxSeconds,
            final int maxIterations, final int maxDepth)

    {
        return doIterativeDeepening(context, maxDepth);
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
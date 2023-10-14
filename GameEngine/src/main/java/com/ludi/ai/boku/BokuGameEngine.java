package com.ludi.ai.boku;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.Action;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.MobilitySimple;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import other.context.Context;



public class BokuGameEngine extends AI
{
    
    //-------------------------------------------------------------------------
    
    /** Our player index */
    protected int player = -1;
    protected Heuristics heuristicValueFunction = null;
    public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.001f;
    BufferedWriter fileout = null;
    
    //-------------------------------------------------------------------------
    
    /**
     * Constructor
     */
    public BokuGameEngine()
    {
        this.friendlyName = "TicTacToe Example";
        try {
            FileWriter fstream = new FileWriter("C:\\Tauseef_A\\Workspace_Coding\\ISG\\out.txt", true);
            fileout = new BufferedWriter(fstream);
            fileout.write("Start\n");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
    }

    private float minimax
    (
        final Game game,
        final Context context,
        int depth,
        boolean isMaximizing
    )
    {
        FastArrayList<Move> legalmoves = game.moves(context).moves();
        if(depth == 0 || legalmoves.size() == 0)
        {
           return this.heuristicValueFunction.computeValue(context, this.player, ABS_HEURISTIC_WEIGHT_THRESHOLD);
        }
        if(isMaximizing)
        {
            float maxvalue = 0.00f;
            for (int i = 0; i < legalmoves.size(); ++i)
            {
                final Context copycontext = copyContext(context);
                final Move m = legalmoves.get(i);
                game.apply(copycontext, m);
                float score = minimax(game, context, depth-1, false);
                if(score > maxvalue)
                {
                    maxvalue = score;
                }
            }
            return maxvalue;

        }
        else
        {
            float minvalue = 1000.00f;
            for (int i = 0; i < legalmoves.size(); ++i)
            {
                final Context copycontext = copyContext(context);
                final Move m = legalmoves.get(i);
                game.apply(copycontext, m);
                float score = minimax(game, context, depth-1, true);
                if(score < minvalue)
                {
                    minvalue = score;
                }
            }
            return minvalue;
        }
    }
    private float optimizeheuristics(float hrsvalue,boolean opp)
    {
        if (hrsvalue >= 1.2f) {
                hrsvalue = 10000.00f;
                if(opp) hrsvalue-=5;
            }
            else if (hrsvalue >= 1.1f) {
                hrsvalue = 9990.00f;
            }
            else if (hrsvalue >= 1.0f) {
                hrsvalue = 9980.00f;
            }
            else if (hrsvalue >= 0.90f) {
                hrsvalue = 9970.00f;
            }
            else if (hrsvalue >= 0.80f) {
                hrsvalue = 9960.00f;
            }
            else if (hrsvalue >= 0.70f) {
                hrsvalue = 9950.00f;
            }
            else if (hrsvalue >= 0.60f) {
                hrsvalue = 9940.00f;
            }
             else if (hrsvalue >= 0.50f) {
                hrsvalue = 9930.00f;
            }
            if(opp) hrsvalue+=5;
            
            return hrsvalue;
    }

    private float alphabeta
    (
        final Game game,
        final Context context,
        int depth,
        float alpha,
        float beta,
        boolean isMaximizing
    )
    {
        FastArrayList<Move> legalmoves = game.moves(context).moves();
        if(depth == 0 || legalmoves.size() == 0)
        {
            float hrsvalue = this.heuristicValueFunction.computeValue(context, this.player,
                    ABS_HEURISTIC_WEIGHT_THRESHOLD);
            
            if (hrsvalue >= 0.50f) {
                hrsvalue = optimizeheuristics(hrsvalue,false);
            }

            float oppval = this.heuristicValueFunction.computeValue(context, 1, ABS_HEURISTIC_WEIGHT_THRESHOLD);
            if (oppval >= 0.50f) {
                oppval = optimizeheuristics(oppval,true);
            }
            hrsvalue -= oppval;
            int[] scores = context.scores();
            for(int j=0;j< scores.length;j++)
            {
                if(scores[j] != 0)
                {
                    int p = scores[j];
                }
            }
            try {
                fileout.write(Float.toString(hrsvalue) + "||");
                fileout.flush();
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            return hrsvalue;
        }
        try {
        fileout.write("\t");
        }catch(IOException exp)
        {

        }
    
        if(isMaximizing)
        {
            float maxvalue = 0.00f;
            for (int i = 0; i < legalmoves.size(); ++i)
            {
                final Context copycontext = copyContext(context);
                final Move m = legalmoves.get(i);
                game.apply(copycontext, m);
                float score = alphabeta(game, copycontext, depth-1,alpha,beta, false);
                if(score > alpha)
                {
                    alpha = score;
                }
                if(score > beta)
                {
                    return alpha;
                }
                
            }
            return alpha;

        }
        else
        {
            float minvalue = 1000.00f;
            for (int i = 0; i < legalmoves.size(); ++i)
            {
                final Context copycontext = copyContext(context);
                final Move m = legalmoves.get(i);
                game.apply(copycontext, m);
                float score = alphabeta(game, copycontext, depth-1,alpha,beta, true);
                
                if(score < beta) beta = score;
                if(score < alpha)
                {
                    return beta;
                }
            }
            return beta;
        }
    }

    private Move iterativeDeepening(
            final Game game,
            final Context context) {
        FastArrayList<Move> legalmoves = game.moves(context).moves();
        Move bestmove = legalmoves.get(0);
        //final Context copycontext = copyContext(context);
        int initialdepth = 1;
        int finaldepth = 4;
        float alpha = -10000.00f;
        //float beta = 10000.00f;
        while (initialdepth < finaldepth) {
            for (int i = 0; i < legalmoves.size(); ++i) {
                final Move m = legalmoves.get(i);
                //float alpha = -10000.00f;
                float beta = 10000.00f;
                final Context copycontext = copyContext(context);
                game.apply(copycontext, m);
                float hrsvalue = this.heuristicValueFunction.computeValue(copycontext, this.player,
                    ABS_HEURISTIC_WEIGHT_THRESHOLD);
                
                float score = alphabeta(game, copycontext, 1, alpha, beta, true);
                if (score > alpha) {
                    alpha = score;
                    bestmove = legalmoves.get(i);
                }
                if (alpha > beta) {
                    return bestmove;
                }
            }
            try {
                fileout.write("\n");
                fileout.flush();
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            
            initialdepth++;
        }
        final Context copycontext = copyContext(context);
        game.apply(copycontext, bestmove);
                float hrsvalue = this.heuristicValueFunction.computeValue(copycontext, this.player,
                    ABS_HEURISTIC_WEIGHT_THRESHOLD);
        return bestmove;

    }

    private Move AdvSearchAlgo
    (
        final Game game,
        final Context context
    )
    {
        return iterativeDeepening(game,context);
    }
    
    //-------------------------------------------------------------------------

    @Override
    public Move selectAction
    (
        final Game game,
        final Context context, 
        final double maxSeconds, 
        final int maxIterations, 
        final int maxDepth
    )
    {
        final Context copycontext = copyContext(context);
        return AdvSearchAlgo(game,copycontext);
        //FastArrayList<Move> legalmoves = game.moves(context).moves();

        //return legalmoves.get(0);

    }
    
    
    
    @Override
    public void initAI(final Game game, final int playerID)
    {
        this.player = playerID;
        this.heuristicValueFunction = new Heuristics(new HeuristicTerm[]{
           // new MobilitySimple(null, Float.valueOf(0.2f)),
            new LineCompletionHeuristic(null,Float.valueOf(1.00f),5)
        });
    }
    
    @Override
    public boolean supportsGame(final Game game)
    {
        if (game.isStochasticGame())
            return false;
        
        if (!game.isAlternatingMoveGame())
            return false;
        
        return true;
    }
    
    //-------------------------------------------------------------------------

    
    //-------------------------------------------------------------------------

}
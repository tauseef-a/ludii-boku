package com.ludi.ai.boku;

import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import other.context.Context;

public class LineCompletionHeuristicEngine implements HeuristicsEngine{

    protected Heuristics heuristicEvaluator = null;
    public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.001f;

    public LineCompletionHeuristicEngine()
    {
        this. heuristicEvaluator = new Heuristics(new HeuristicTerm[]{
            // new MobilitySimple(null, Float.valueOf(0.2f)),
             new LineCompletionHeuristic(null,Float.valueOf(1.00f),5)
         });

    }

    public float evaluateMove(final Context currentContext, final int playerID)
    {
        float hrsvalue = this.heuristicEvaluator.computeValue(currentContext, playerID,
                ABS_HEURISTIC_WEIGHT_THRESHOLD);

        if (hrsvalue >= 0.50f) {
            hrsvalue = optimizeheuristics(hrsvalue, false);
        }

        float oppval = this.heuristicEvaluator.computeValue(currentContext, 1, ABS_HEURISTIC_WEIGHT_THRESHOLD);
        if (oppval >= 0.50f) {
            oppval = optimizeheuristics(oppval, true);
        }
        hrsvalue -= oppval;
        int[] scores = currentContext.scores();
        for (int j = 0; j < scores.length; j++) {
            if (scores[j] != 0) {
                int p = scores[j];
            }
        }
        
        return hrsvalue;

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
}
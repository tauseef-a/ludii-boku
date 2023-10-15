package com.ludi.ai.boku.search;

import com.ludi.ai.boku.MoveEngine;

import other.context.Context;
import other.move.Move;

public interface Search {
    public void initialize(final int playerID);

    public void reset();

    public Move searchBestMove(final MoveEngine moveEngine, final Context context, final double maxSeconds, final int maxIterations, final int maxDepth);
}

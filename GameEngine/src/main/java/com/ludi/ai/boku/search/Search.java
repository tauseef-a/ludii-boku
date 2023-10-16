package com.ludi.ai.boku.search;

import com.ludi.ai.boku.MoveManager;

import other.context.Context;
import other.move.Move;

public interface Search {
    public void initialize(final int playerID);

    public void reset();

    public Move searchBestMove(final MoveManager moveEngine, final Context context, final double maxSeconds, final int maxIterations, final int maxDepth);
}

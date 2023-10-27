package com.ludi.ai.boku.search;

import com.ludi.ai.boku.IMoveManager;

import other.context.Context;
import other.move.Move;

public interface ISearch {
    public void initialize(final int playerID);

    public void reset();

    public Move searchBestMove(final IMoveManager moveManager, final Context context, final double maxSeconds, final int maxIterations, final int maxDepth);
}

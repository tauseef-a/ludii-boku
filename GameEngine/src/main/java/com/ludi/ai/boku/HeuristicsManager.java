package com.ludi.ai.boku;

import other.context.Context;

public interface HeuristicsManager {
    public float evaluateMove(final Context currentContext, final int playerID);
}
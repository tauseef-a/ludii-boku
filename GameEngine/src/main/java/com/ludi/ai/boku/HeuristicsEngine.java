package com.ludi.ai.boku;

import other.context.Context;

public interface HeuristicsEngine {
    public float evaluateMove(final Context currentContext, final int playerID);
}
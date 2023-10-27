package com.ludi.ai.boku;

import other.context.Context;

public interface IHeuristicsManager {
    public float evaluateMove(final Context currentContext, final int playerID);

    public void setContext(final Context currentContext);

    public float evaluateMove(final Context currentContext);

    public byte[][] getBoardPieceStatus();
    // public void updateContext(final Context currentContext);
}
package com.ludi.ai.boku;

import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;

public interface IMoveManager {

    public Context setMoveAsCurrent(final Context currentContext, final Move move);

    public FastArrayList<Move> getCurrentMoves(final Context currentContext);

    public FastArrayList<Move> getCurrentMoves(final Context currentContext,int currentPly);

    public int getCurrentPly(final Context currentContext);

    public void storeKillerMove(Move m,int currentPly);

}
package com.ludi.ai.boku;

import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;

public class MoveManager  implements IMoveManager{

    private BokuGameAgent ai;
    //We assume a max depth of 100 include capture Moves.
    private Move[][] killerMoves = new Move[100][2];

    MoveManager(BokuGameAgent ai)
    {
        this.ai=ai;
    }

    @Override
    public final Context setMoveAsCurrent(final Context currentContext, final Move move) {
        Context copycontext = ai.copyContext(currentContext);
        Move m = ai.game().apply(copycontext, move);
        if (m == null)
            copycontext = null;
        return copycontext;
    }

    @Override
    public FastArrayList<Move> getCurrentMoves(final Context currentContext) {
        return ai.game().moves(currentContext).moves();
    }

    @Override
    public FastArrayList<Move> getCurrentMoves(final Context currentContext,int currentPly) {
        //TODO: [LOW] Once History Heuristics is added switch to priority Queue

        FastArrayList<Move> moves = new FastArrayList<>();
        FastArrayList<Move> fromMoves = ai.game().moves(currentContext).moves();
        int found = 2;
        if(killerMoves[currentPly][0] != null) {moves.add(killerMoves[currentPly][0]);found--;};
        if(killerMoves[currentPly][1] != null) {moves.add(killerMoves[currentPly][1]);found--;};
        
        for(int i=0;i<fromMoves.size();i++)
        {
            Move m = fromMoves.get(i);
            if(found <= 2 && (m.equals(killerMoves[currentPly][0]) || m.equals(killerMoves[currentPly][1])) )
            {
                found++;
                continue;

            }
            moves.add(m);

        }
        return moves;
    }

    @Override
    public int getCurrentPly(final Context currentContext)
    {
        //This seems to work for now considering Capture and Undo State of Game
        return currentContext.trial().previousState().size();
    }

    @Override
    public void storeKillerMove(Move m,int currentPly)
    {
        if(m != null)
        {
            if(!m.equals(killerMoves[currentPly][0]) && !m.equals(killerMoves[currentPly][1]))
            {
                killerMoves[currentPly][1] = killerMoves[currentPly][0];
                killerMoves[currentPly][0] = m;

            }
        }
    }

}
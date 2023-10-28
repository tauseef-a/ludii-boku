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

        FastArrayList<Move> fromMoves = ai.game().moves(currentContext).moves();
        boolean kMove1Found = false;
        boolean kMove2Found = false;
        int kMove1Index = -1;
        int kMove2Index = -1;

        if(killerMoves[currentPly][0] != null)
        {
            FastArrayList<Move> moves = new FastArrayList<>();
            for (int i = 0; i < fromMoves.size() && (!kMove1Found || !kMove2Found); i++) {
                Move m = fromMoves.get(i);
                if (!kMove1Found && (m.equals(killerMoves[currentPly][0]))) {
                    kMove1Found = true;
                    kMove1Index = i;
                }
                else if (!kMove2Found && (m.equals(killerMoves[currentPly][1]))) {
                    kMove2Found = true;
                    kMove2Index = i;
                }
            }
            if (!kMove1Found && !kMove2Found)
                return fromMoves;
            
            if(kMove1Found) {moves.add(killerMoves[currentPly][0]);};
            if(kMove2Found) {moves.add(killerMoves[currentPly][1]);};
            
            for(int i=0;i<fromMoves.size();i++)
            {
                if(i==kMove1Index || i==kMove2Index ) continue;
                Move m = fromMoves.get(i);
                moves.add(m);
    
            }
        
            return moves;
        }
        else return fromMoves;
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
package com.ludi.ai.boku;

import other.move.Move;

public interface ITranspositionTable {
    public final static byte FLAG_VALID = 1;
    public final static byte FLAG_LBOUND = 2;
    public final static byte FLAG_UBOUND = 3;

    public class TranspositionTableEntry {
        final public long key;
        final public Move move;
        final public float score;
        final public byte flag;
        final public byte depth;

        TranspositionTableEntry(long k, Move m, float s, byte f, byte d) {
            key = k;
            move = m;
            score = s;
            flag = f;
            depth = d;
        }
    }

    public void Save(long hash, Move move, float score, byte flag, byte depth);

    public void Save(byte[][] peices, Move move, float score, byte flag, byte depth);

    public TranspositionTableEntry Retrieve(byte[][] ownedPieces);

}
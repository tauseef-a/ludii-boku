package com.ludi.ai.boku;

import other.move.Move;
import java.util.Random;

public class ZobristTranspositionTable implements ITranspositionTable {
    private final static int MAXZOBRISTKEYS = 80;
    private final static int TTSIZE = 256;// 2^8;
    private final static int MASK = 0xFF;
    private TranspositionTableEntry[] ttEntryList;

    private long[][] zobristKey = new long[2][MAXZOBRISTKEYS];

    ZobristTranspositionTable() {
        ttEntryList = new TranspositionTableEntry[TTSIZE];
        updateZobristHashEntry();
    }

    /*
     * Here we use Zobrist Hashing technique.
     * Hash is uniquely assigned to each players piece on the board.
     * We consider all pieces on the board currently, and xor their individual hashes.
     */
    private long getHashFromPieces(byte[][] ownedPieces) {
        long hash = 0;
        for (int playerID = 0; playerID < ownedPieces.length; playerID++) {
            for (int i = 0; i < ownedPieces[playerID].length && ownedPieces[playerID][i] != -1; i++) {
                hash = hash ^ zobristKey[playerID][ownedPieces[playerID][i]];
            }
        }
        return hash;
    }

    /*
     * Retrieve the entry from TT for the move, given the owned pieces from current board.
     * If the entry is not found, or the hash is found to mismatch, we return null.
     */
    @Override
    public TranspositionTableEntry Retrieve(byte[][] ownedPieces) {
        long hash = getHashFromPieces(ownedPieces);
        long index = hash & MASK;
        TranspositionTableEntry ttEntry = ttEntryList[(int) index];
        if (ttEntry != null && ttEntry.key != hash) {
            ttEntry = null;
        }
        return ttEntry;
    }

    /*
     * Save the move, score, flag, and depth in the transposition table.
     * Here we are only saving the "One Deep" replacement scheme.
     */
    @Override
    public void Save(long hash, Move move, float score, byte flag, byte depth) {
        // long hash = getHashFromPieces(ownedPieces);
        long index = hash & MASK;
        TranspositionTableEntry ttEntry = ttEntryList[(int) index];
        if (ttEntry != null) {
            // Same Move with Better Depth. Lets discard same move with lesser depth
            if (ttEntry.key == hash && ttEntry.depth < depth) {
                ttEntryList[(int) index] = new TranspositionTableEntry(hash, move, score, flag, depth);
            }
            // Different Move with better Search depth
            else if (ttEntry.key != hash && ttEntry.depth < depth) {
                ttEntryList[(int) index] = new TranspositionTableEntry(hash, move, score, flag, depth);
            }
        } else {
            ttEntryList[(int) index] = new TranspositionTableEntry(hash, move, score, flag, depth);
        }
    }

    @Override
    public void Save(byte[][] ownedPieces, Move move, float score, byte flag, byte depth) {
        long hash = getHashFromPieces(ownedPieces);
        Save(hash, move, score, flag, depth);
    }

    /*
     * Hashes are generated for every player, and every position that can be held by the player.
     */
    private void updateZobristHashEntry() {
        Random rand = new Random(System.currentTimeMillis());
        for (int playerID = 0; playerID < 2; playerID++) {
            for (int i = 0; i < MAXZOBRISTKEYS; i++) {
                zobristKey[playerID][i] = rand.nextLong();
            }
        }
    }

}
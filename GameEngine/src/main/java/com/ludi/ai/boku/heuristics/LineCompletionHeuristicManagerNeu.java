package com.ludi.ai.boku.heuristics;

import other.context.Context;

import java.util.Arrays;

import java.util.List;
import java.util.ListIterator;

import game.Game;

import other.location.Location;
import other.state.owned.Owned;


public class LineCompletionHeuristicManagerNeu implements IHeuristicsManager {

    public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.001f;
    private int targetLength;
    private static final float MAXVALUE = 1000.00f;
    boolean[] ignoreList = new boolean[80];
    private static final byte[] boardLineLengths = { 5, 6, 7, 8, 9, 10, 10, 9, 8, 7, 6 };
    private static final byte[] boardLineLowerPos = { 0, 5, 11, 18, 26, 35, 45, 54, 62, 69, 75 };
    private static final byte[] boardLineUpperPos = { 4, 10, 17, 25, 34, 44, 53, 61, 68, 74, 79 };
    private int[] siteOwner = new int[80];
    private byte[][] ownedSiteList = new byte[2][60];
    private static final byte[][][] boardRadialList = new byte[80][6][4];
    private final static byte[] staticSiteWeights = new byte[80];
    private byte[] dynamicSiteWeights = new byte[80];
    private boolean useDynamicSiteWeights = false;
    private float boardvalue;

    public LineCompletionHeuristicManagerNeu() {
        this.targetLength = 5;
        generateBoardRadialList();
        updateStaticSiteWeights();

    }

    public float evaluateMove(final Context currentContext, final int playerID, float hrsvalue) {
        byte[] SiteWeights = null;
        if (useDynamicSiteWeights) {
            SiteWeights = dynamicSiteWeights;
        } else {
            SiteWeights = staticSiteWeights;
        }
        
        final Owned owned = currentContext.state().owned();
        final List<? extends Location>[] pieces = owned.positions(playerID);
        byte opponentID =  (byte)(playerID == 1? 2:1);
        if (pieces.length != 1)
            assert true;
        ListIterator<? extends Location> iterator = pieces[0].listIterator(pieces[0].size());
        if (iterator.hasPrevious()) {
            byte site = (byte) iterator.previous().site();
            if (siteOwner[site] == 0) {//Site is newly added. WIll have to change after capture TODO
                if (useDynamicSiteWeights) {
                    if ((playerID == 1 && SiteWeights[site] > 0) // TODO agent
                            || (playerID == 2 && SiteWeights[site] < 0)) {
                        hrsvalue -= SiteWeights[site];
                        for (int radialDir = 0; radialDir < 6; radialDir++) {
                            for (int nborSiteId = 0; nborSiteId < 4
                                    && boardRadialList[site][radialDir][nborSiteId] != (byte) -1; nborSiteId++) {
                                if (siteOwner[boardRadialList[site][radialDir][nborSiteId]] == opponentID)
                                    hrsvalue -= SiteWeights[boardRadialList[site][radialDir][nborSiteId]];
                            }

                        }

                    } else {
                        hrsvalue += SiteWeights[site];
                    }
                }
                else
                {
                    hrsvalue += SiteWeights[site];
                }
            }
        }

        return boardvalue;

    }

    public float evaluateContext(final Context currentContext) {
        updateContext(currentContext);
        boardvalue = 0;
        byte[] SiteWeights = null;
        if (useDynamicSiteWeights) {
            SiteWeights = dynamicSiteWeights;
        } else {
            SiteWeights = staticSiteWeights;
        }

        // for (int playerId = 0; playerId < 2; playerId++) {//TODO Determine AI and non
        // AI player and change below hardcoding
        for (int siteId = 0; siteId < 60 && ownedSiteList[1][siteId] != -1; siteId++) {
            boardvalue += SiteWeights[ownedSiteList[1][siteId]];
        }
        for (int siteId = 0; siteId < 60 && ownedSiteList[0][siteId] != -1; siteId++) {
            boardvalue += SiteWeights[ownedSiteList[0][siteId]];
        }
        // }

        return boardvalue;

    }

    public void updateContext(final Context currentContext) {
        updateSiteStatus(currentContext);
        generateDynamicWeights();

    }

    public void updateSiteStatus(final Context context) {
        Arrays.fill(siteOwner, 0);
        Arrays.fill(ownedSiteList[0], (byte) -1);
        Arrays.fill(ownedSiteList[1], (byte) -1);
        final Game game = context.game();
        final Owned owned = context.state().owned();
        final int playerCount = game.players().count();
        useDynamicSiteWeights = false;
        boolean canGenerateDynamicWeight = true;
        for (int i = 1; i <= playerCount; i++) {
            final List<? extends Location>[] pieces = owned.positions(i);
            if (pieces.length != 1)
                assert true;
            ListIterator<? extends Location> iterator = pieces[0].listIterator(pieces[0].size());
            if (iterator.hasPrevious())
                ownedSiteList[i - 1][0] = (byte) iterator.previous().site();
            else
                canGenerateDynamicWeight &= false;

            byte k = 1;
            while (iterator.hasPrevious()) {
                final byte site = (byte) iterator.previous().site();
                siteOwner[site] = i;
                ownedSiteList[i - 1][k++] = site;
            }
        }
        useDynamicSiteWeights = canGenerateDynamicWeight;
    }

    private void generateDynamicWeights() {
        Arrays.fill(dynamicSiteWeights, (byte) 0);
        useDynamicSiteWeights = true;
        
        byte factor = (byte)-1;
        byte oppPlayerID=2;
        for (int playerId = 0; playerId < 2; playerId++) {
            if(playerId == 1) {factor =1;oppPlayerID = 1;}
            boolean useDynamicSiteWeightsPlayer = false;
            for (int siteId = 0; siteId < 60 && ownedSiteList[playerId][siteId] != -1; siteId++) {
                dynamicSiteWeights[ownedSiteList[playerId][siteId]] += factor*5;
                int countEmptyRadial = 0;
                for (int radialDir = 0; radialDir < 6; radialDir++) {
                    byte weight = 4;
                    boolean oppFound = false;
                    // if (boardRadialList[ownedSiteList[playerId][siteId]][radialDir][3] != -1) {
                    for (int nborSiteId = 0; nborSiteId < 4
                            && boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId] != (byte) -1; nborSiteId++) {

                        if ((dynamicSiteWeights[boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId]]
                                - (factor * weight)) > 0)
                            dynamicSiteWeights[boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId]] = (byte)(factor
                                    * weight);
                        if(siteOwner[boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId]] == oppPlayerID)
                        {
                            oppFound = true;
                            break;
                        }
                        weight--;
                    }
                    if(oppFound)
                    {
                        //weight = 4;
                        for (int nborSiteId = 0; nborSiteId < 4
                                && boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId] != (byte) -1; nborSiteId++) {

                            //dynamicSiteWeights[boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId]] -= weight--;
                            dynamicSiteWeights[boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId]] = 0;
                            if (siteOwner[boardRadialList[ownedSiteList[playerId][siteId]][radialDir][nborSiteId]] == playerId
                                    + 1) {
                                break;
                            }
                        }
                        dynamicSiteWeights[ownedSiteList[playerId][siteId]] -=factor;
                    }
                    // }
                    if (weight == 4)
                        countEmptyRadial++;
                }
                if (countEmptyRadial >= 6)
                    useDynamicSiteWeightsPlayer |= false;
                else
                    useDynamicSiteWeightsPlayer |= true;

            }
            useDynamicSiteWeights &= useDynamicSiteWeightsPlayer;
        }

    }

    private void updateStaticSiteWeights() {
        staticSiteWeights[39] = 5;

        staticSiteWeights[0] = 4;
        staticSiteWeights[4] = 4;
        staticSiteWeights[35] = 4;
        staticSiteWeights[44] = 4;
        staticSiteWeights[75] = 4;
        staticSiteWeights[79] = 4;

        staticSiteWeights[5] = 3;
        staticSiteWeights[6] = 3;
        staticSiteWeights[9] = 3;
        staticSiteWeights[10] = 3;
        staticSiteWeights[26] = 3;
        staticSiteWeights[34] = 3;
        staticSiteWeights[36] = 3;
        staticSiteWeights[43] = 3;
        staticSiteWeights[45] = 3;
        staticSiteWeights[53] = 3;
        staticSiteWeights[69] = 3;
        staticSiteWeights[70] = 3;
        staticSiteWeights[73] = 3;
        staticSiteWeights[74] = 3;

        staticSiteWeights[12] = 2;
        staticSiteWeights[13] = 2;
        staticSiteWeights[15] = 2;
        staticSiteWeights[16] = 2;
        staticSiteWeights[27] = 2;
        staticSiteWeights[33] = 2;
        staticSiteWeights[37] = 2;
        staticSiteWeights[42] = 2;
        staticSiteWeights[46] = 2;
        staticSiteWeights[52] = 2;
        staticSiteWeights[63] = 2;
        staticSiteWeights[64] = 2;
        staticSiteWeights[66] = 2;
        staticSiteWeights[67] = 2;

        staticSiteWeights[20] = 1;
        staticSiteWeights[21] = 1;
        staticSiteWeights[22] = 1;
        staticSiteWeights[23] = 1;
        staticSiteWeights[28] = 1;
        staticSiteWeights[32] = 1;
        staticSiteWeights[38] = 1;
        staticSiteWeights[41] = 1;
        staticSiteWeights[47] = 1;
        staticSiteWeights[51] = 1;
        staticSiteWeights[56] = 1;
        staticSiteWeights[57] = 1;
        staticSiteWeights[58] = 1;
        staticSiteWeights[59] = 1;

    }

    private void generateBoardRadialList() {
        for (byte i = 0; i < 80; i++) {
            generateBoardRadialListForSite(i);
        }
    }

    private void generateBoardRadialListForSite(byte site) {
        byte[][] boardSiteRadialList = boardRadialList[site];
        int line = 0;
        for (int i = 0; i < boardLineUpperPos.length; i++) {
            if (site <= boardLineUpperPos[i]) {
                line = i;
                break;
            }
        }
        Arrays.fill(boardSiteRadialList[0], (byte) -1);
        // boardSiteRadialList[0][0] = site;
        for (byte j = 1, newsite = (byte) (site + 1); j < targetLength
                && newsite <= boardLineUpperPos[line]; j++, newsite++) {
            boardSiteRadialList[0][j - 1] = newsite;
        }

        Arrays.fill(boardSiteRadialList[1], (byte) -1);
        // boardSiteRadialList[1][0] = site;
        for (byte j = 1, newsite = site; j < targetLength && (line + j) < boardLineLengths.length; j++) {
            newsite += boardLineLengths[line + j];
            if (newsite <= boardLineUpperPos[line + j])
                boardSiteRadialList[1][j - 1] = newsite;
            else
                break;
        }

        Arrays.fill(boardSiteRadialList[2], (byte) -1);
        // boardSiteRadialList[2][0] = site;
        for (byte j = 1, newsite = site; j < targetLength && (line + j) < boardLineLengths.length; j++) {
            newsite += boardLineLengths[line + j] - 1;
            if (newsite >= boardLineLowerPos[line + j])
                boardSiteRadialList[2][j - 1] = newsite;
            else
                break;
        }
        // opposite

        Arrays.fill(boardSiteRadialList[3], (byte) -1);
        // boardSiteRadialList[3][0] = site;
        for (byte j = 1, newsite = (byte) (site - 1); j < targetLength
                && newsite >= boardLineLowerPos[line]; j++, newsite--) {
            boardSiteRadialList[3][j - 1] = newsite;
        }

        Arrays.fill(boardSiteRadialList[4], (byte) -1);
        // boardSiteRadialList[4][0] = site;
        for (byte j = 1, k = 0, newsite = site; j < targetLength && (line - k - 1) >= 0; j++, k++) {
            newsite -= boardLineLengths[line - k];
            if (newsite >= boardLineLowerPos[line - k - 1])
                boardSiteRadialList[4][j - 1] = newsite;
            else
                break;
        }

        Arrays.fill(boardSiteRadialList[5], (byte) -1);
        // boardSiteRadialList[5][0] = site;
        for (byte j = 1, k = 0, newsite = site; j < targetLength && (line - k - 1) >= 0; j++, k++) {
            newsite -= boardLineLengths[line - k] - 1;
            if (newsite >= boardLineLowerPos[line - k - 1])
                boardSiteRadialList[5][j - 1] = newsite;
            else
                break;
        }

    }

    @Override
    public float evaluateMove(final Context currentContext, final int playerID) {
        return 0.0f;
    }

    @Override
    public void setContext(final Context currentContext) {
    }

    @Override
    public float evaluateMove(final Context currentContext) {
        return 0;
    }

    @Override
    public byte[][] getBoardPieceStatus() {
        return null;
    }

}
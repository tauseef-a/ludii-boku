package com.ludi.ai.boku;

import other.context.Context;

import java.util.Arrays;

import java.util.List;
import java.util.ListIterator;

import game.Game;

import gnu.trove.list.array.TFloatArrayList;

import main.collections.ListUtils;

import other.location.Location;
import other.state.owned.Owned;

public class LineCompletionHeuristicManager implements IHeuristicsManager {

    // protected Heuristics heuristicEvaluator = null;
    private static final int TARGETLENGTH = 5;
    private static final int CAPTURELENGTH = 2;
    private static final float MAXVALUE = 10000.00f;
    private static final int MAXOWNEDSITES = 60;
    private static final byte[] boardLineLengths = { 5, 6, 7, 8, 9, 10, 10, 9, 8, 7, 6 };
    private static final byte[] boardLineLowerPos = { 0, 5, 11, 18, 26, 35, 45, 54, 62, 69, 75 };
    private static final byte[] boardLineUpperPos = { 4, 10, 17, 25, 34, 44, 53, 61, 68, 74, 79 };
    private int[] siteOwner = new int[80];
    private byte[][] ownedSiteList = new byte[2][MAXOWNEDSITES];
    private static final byte[][][] boardRadialList = new byte[80][6][4];

    public LineCompletionHeuristicManager() {
        // this.TARGETLENGTH = 5;
        generateBoardRadialList();

    }

    public void updateSiteStatus(final Context context) {
        Arrays.fill(siteOwner, 0);
        Arrays.fill(ownedSiteList[0], (byte) -1);
        Arrays.fill(ownedSiteList[1], (byte) -1);
        final Game game = context.game();
        final Owned owned = context.state().owned();
        final int playerCount = game.players().count();

        for (int i = 1; i <= playerCount; i++) {
            final List<? extends Location>[] pieces = owned.positions(i);
            if (pieces.length != 1)
                assert true;
            ListIterator<? extends Location> iterator = pieces[0].listIterator(pieces[0].size());
            if (iterator.hasPrevious())
                ownedSiteList[i - 1][0] = (byte) iterator.previous().site();

            byte k = 1;
            while (iterator.hasPrevious()) {
                final byte site = (byte) iterator.previous().site();
                siteOwner[site] = i;
                ownedSiteList[i - 1][k++] = site;
            }
        }
    }

    /**
     * The functionality of this API is copied from https://github.com/Ludeme/Ludii
     * Core/src/metadata/ai/heuristics/terms/LineCompletionHeuristic.java file evaluate function.
     * All credits to its corresponding authors
     * Changes include mainly enhancements to precalculate the radial directions to speed up the execution time
     * And stopping further calculations once Target Length is Reached.
     * @param context
     * @param playerID
     * @return
     */
    public float evaluateBoard(final Context context, final int playerID) {
        final boolean[] ignore = new boolean[80];

        final TFloatArrayList lineValues = new TFloatArrayList();

        for (int siteID = 0; siteID < MAXOWNEDSITES && ownedSiteList[playerID - 1][siteID] != -1; siteID++) {
            final int pieceSite = ownedSiteList[playerID - 1][siteID];

            byte[][] radialList = boardRadialList[pieceSite];

            for (int radialDirection = 0; radialDirection < 3; radialDirection++) {

                final boolean[] endPathsBlocked = new boolean[TARGETLENGTH];
                final int[] potentialLineLengths = new int[TARGETLENGTH];
                final int[] realPieces = new int[TARGETLENGTH];

                // Fill all the counts up starting with 1, since we know
                // there's at least 1 piece (the one we're starting from)
                Arrays.fill(potentialLineLengths, 1);
                Arrays.fill(realPieces, 1);

                for (int indexPath = 1; indexPath < TARGETLENGTH
                        && radialList[radialDirection][indexPath - 1] != -1; ++indexPath) {
                    final int site = radialList[radialDirection][indexPath - 1];
                    final int siteowner = siteOwner[site];

                    if (ignore[site]) {
                        // We've already been here, skip this
                        break;
                    } else if (siteowner != 0 && siteowner != playerID) {
                        // An enemy piece
                        assert (endPathsBlocked[TARGETLENGTH - indexPath] == false);
                        endPathsBlocked[TARGETLENGTH - indexPath] = true;
                        break;
                    } else {
                        for (int j = 0; j < TARGETLENGTH - indexPath; ++j) {
                            potentialLineLengths[j] += 1;

                            if (siteowner == playerID)
                                realPieces[j] += 1;
                            if (realPieces[j] == TARGETLENGTH)
                                return MAXVALUE;
                        }
                    }
                }

                // At best there can be targetLength lines for this radial + opposite combo;
                // There's:
                // - one line starting in piece pos and following direction
                // - one line with one piece in opposite direction, and rest in direction
                // - one line with two pieces in opposite direction, and rest in direction
                // - etc.

                final boolean[] endOppositePathsBlocked = new boolean[TARGETLENGTH];

                // Now the same thing, but in opposite radial

                for (int indexPath = 1; indexPath < TARGETLENGTH
                        && radialList[radialDirection + 3][indexPath - 1] != -1; ++indexPath) {
                    final int site = radialList[radialDirection + 3][indexPath - 1];
                    final int who = siteOwner[site];

                    if (ignore[site]) {
                        // We've already been here, skip this
                        break;
                    } else if (who != 0 && who != playerID) {
                        // An enemy piece
                        assert (endOppositePathsBlocked[indexPath - 1] == false);
                        endOppositePathsBlocked[indexPath - 1] = true;
                        break;
                    } else {
                        for (int j = indexPath; j < TARGETLENGTH; ++j) {
                            potentialLineLengths[j] += 1;

                            if (who == playerID)
                                realPieces[j] += 1;
                            if (realPieces[j] == TARGETLENGTH)
                                return MAXVALUE;
                        }
                    }
                }

                // Compute values for all potential lines along this radial
                for (int j = 0; j < potentialLineLengths.length; ++j) {
                    if (potentialLineLengths[j] == TARGETLENGTH) {
                        // This is a potential line
                        float value = (float) realPieces[j] / (float) potentialLineLengths[j];

                        if (endPathsBlocked[j])
                            value *= 0.5f;
                        if (endOppositePathsBlocked[j])
                            value *= 0.5f;

                        lineValues.add(value);
                    }
                }

            }

            // From now on we should ignore any lines including this piece;
            // we've already counted all of them!
            ignore[pieceSite] = true;
        }

        // Union of probabilities takes way too long to compute, so we just
        // take average of the top 2 line values
        final int argMax = ListUtils.argMax(lineValues);
        final float maxVal = lineValues.getQuick(argMax);
        lineValues.setQuick(argMax, -1.f);
        final int secondArgMax = ListUtils.argMax(lineValues);
        final float secondMaxVal = lineValues.getQuick(secondArgMax);

        return maxVal + secondMaxVal / 2.f;

    }

    public float evaluateMove(final Context currentContext, final int playerID) {
        int opponent = playerID == 1? 2:1;
        updateSiteStatus(currentContext);
        float hrsvalue = this.evaluateBoard(currentContext, playerID);

        if(hrsvalue >=MAXVALUE)
          return MAXVALUE;

        if (hrsvalue >= 0.50f) {
            hrsvalue = optimizeheuristics(hrsvalue, false);
        }

        float oppval = this.evaluateBoard(currentContext, opponent);
        if(oppval >= MAXVALUE)
          return -MAXVALUE;
        if (oppval >= 0.50f) {
            oppval = optimizeheuristics(oppval, true);
        }
        hrsvalue -= oppval;

        return hrsvalue;
    }

    private float optimizeheuristics(float hrsvalue, boolean opp) {
        if (hrsvalue >= 1.2f) {
            hrsvalue = 7000.00f;
            if (opp)
                hrsvalue -= 5;
        } else if (hrsvalue >= 1.1f) {
            hrsvalue = 6990.00f;
        } else if (hrsvalue >= 1.0f) {
            hrsvalue = 6980.00f;
        } else if (hrsvalue >= 0.90f) {
            hrsvalue = 6970.00f;
        } else if (hrsvalue >= 0.80f) {
            hrsvalue = 6960.00f;
        } else if (hrsvalue >= 0.70f) {
            hrsvalue = 6950.00f;
        } else if (hrsvalue >= 0.60f) {
            hrsvalue = 6940.00f;
        } else if (hrsvalue >= 0.50f) {
            hrsvalue = 4000.00f;
        }
        if (opp)
            hrsvalue += 5;

        return hrsvalue;
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
        for (byte j = 1, newsite = (byte) (site + 1); j < TARGETLENGTH
                && newsite <= boardLineUpperPos[line]; j++, newsite++) {
            boardSiteRadialList[0][j - 1] = newsite;
        }

        Arrays.fill(boardSiteRadialList[1], (byte) -1);
        // boardSiteRadialList[1][0] = site;
        for (byte j = 1, newsite = site; j < TARGETLENGTH && (line + j) < boardLineLengths.length; j++) {
            newsite += boardLineLengths[line + j];
            if (newsite <= boardLineUpperPos[line + j])
                boardSiteRadialList[1][j - 1] = newsite;
            else
                break;
        }

        Arrays.fill(boardSiteRadialList[2], (byte) -1);
        // boardSiteRadialList[2][0] = site;
        for (byte j = 1, newsite = site; j < TARGETLENGTH && (line + j) < boardLineLengths.length; j++) {
            newsite += boardLineLengths[line + j] - 1;
            if (newsite >= boardLineLowerPos[line + j])
                boardSiteRadialList[2][j - 1] = newsite;
            else
                break;
        }
        // opposite

        Arrays.fill(boardSiteRadialList[3], (byte) -1);
        // boardSiteRadialList[3][0] = site;
        for (byte j = 1, newsite = (byte) (site - 1); j < TARGETLENGTH
                && newsite >= boardLineLowerPos[line]; j++, newsite--) {
            boardSiteRadialList[3][j - 1] = newsite;
        }

        Arrays.fill(boardSiteRadialList[4], (byte) -1);
        // boardSiteRadialList[4][0] = site;
        for (byte j = 1, k = 0, newsite = site; j < TARGETLENGTH && (line - k - 1) >= 0; j++, k++) {
            newsite -= boardLineLengths[line - k];
            if (newsite >= boardLineLowerPos[line - k - 1])
                boardSiteRadialList[4][j - 1] = newsite;
            else
                break;
        }

        Arrays.fill(boardSiteRadialList[5], (byte) -1);
        // boardSiteRadialList[5][0] = site;
        for (byte j = 1, k = 0, newsite = site; j < TARGETLENGTH && (line - k - 1) >= 0; j++, k++) {
            newsite -= boardLineLengths[line - k] - 1;
            if (newsite >= boardLineLowerPos[line - k - 1])
                boardSiteRadialList[5][j - 1] = newsite;
            else
                break;
        }

    }

    @Override
    public void setContext(final Context currentContext) {
        updateSiteStatus(currentContext);
    }

    @Override
    public float evaluateMove(final Context currentContext) {
        return 0.0f;
    }

    @Override
    public byte[][] getBoardPieceStatus() {
        return ownedSiteList;
    }
}
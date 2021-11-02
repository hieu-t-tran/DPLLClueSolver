/**
 * ClueReasoner.java - project skeleton for a propositional reasoner
 * for the game of Clue.  Unimplemented portions have the comment "TO
 * BE IMPLEMENTED AS AN EXERCISE".  The reasoner does not include
 * knowledge of how many cards each player holds.  See
 * http://cs.gettysburg.edu/~tneller/nsf/clue/ for details.
 *
 * @author Todd Neller
 * @version 1.0
 *

Copyright (C) 2005 Todd Neller

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Information about the GNU General Public License is available online at:
  http://www.gnu.org/licenses/
To receive a copy of the GNU General Public License, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
02111-1307, USA.

 */

import java.io.*;

public class ClueReasoner 
{
    private int numPlayers;
    private int numCards;
    private SATSolver solver;    
    private String caseFile = "cf";
    private String[] players = {"sc", "mu", "wh", "gr", "pe", "pl"};
    private String[] suspects = {"mu", "pl", "gr", "pe", "sc", "wh"};
    private String[] weapons = {"kn", "ca", "re", "ro", "pi", "wr"};
    private String[] rooms = {"ha", "lo", "di", "ki", "ba", "co", "bi", "li", "st"};
    private String[] cards;

    public ClueReasoner(SATSolver solver)
    {
        numPlayers = players.length;

        // Initialize card info
        cards = new String[suspects.length + weapons.length + rooms.length];
        int i = 0;
        for (String card : suspects)
            cards[i++] = card;
        for (String card : weapons)
            cards[i++] = card;
        for (String card : rooms)
            cards[i++] = card;
        numCards = i;

        // Initialize solver
        this.solver = solver;
        addInitialClauses();
    }

    private int getPlayerNum(String player) 
    {
        if (player.equals(caseFile))
            return numPlayers;
        for (int i = 0; i < numPlayers; i++)
            if (player.equals(players[i]))
                return i;
        System.out.println("Illegal player: " + player);
        return -1;
    }

    private int getCardNum(String card)
    {
        for (int i = 0; i < numCards; i++)
            if (card.equals(cards[i]))
                return i;
        System.out.println("Illegal card: " + card);
        return -1;
    }

    private int getPairNum(String player, String card) 
    {
        return getPairNum(getPlayerNum(player), getCardNum(card));
    }

    private int getPairNum(int playerNum, int cardNum)
    {
        return playerNum * numCards + cardNum + 1;
    }    

    public void addInitialClauses() 
    {
        // Each card is in at least one place (including case file).
        for (int c = 0; c < numCards; c++) {
            int[] clause = new int[numPlayers + 1];
            for (int p = 0; p <= numPlayers; p++)
                clause[p] = getPairNum(p, c);
            solver.addClause(clause);
        }    
        
        // If a card is one place, it cannot be in another place.
        for (int c=0; c<numCards; c++) {
        	for (int i=0; i<numPlayers; i++) {
            	for (int j=i+1; j<numPlayers+1; j++) {
            		int[] clause = new int[2];
            		clause[0] = -getPairNum(i, c);
            		clause[1] = -getPairNum(j, c);
            		solver.addClause(clause);
            	}
            }
        }
            
        // At least one card of each category is in the case file.
        int[] suspectClause = new int[suspects.length];
        for (int i=0; i<suspects.length; i++) {
        	suspectClause[i] = getPairNum(caseFile, suspects[i]);
        }
        solver.addClause(suspectClause);
        
        int[] weaponClause = new int[weapons.length];
        for (int i=0; i<weapons.length; i++) {
        	weaponClause[i] = getPairNum(caseFile, weapons[i]);
        }
        solver.addClause(weaponClause);
        
        int[] roomClause = new int[rooms.length];
        for (int i=0; i<rooms.length; i++) {
        	roomClause[i] = getPairNum(caseFile, rooms[i]);
        }
        solver.addClause(roomClause);
            
        // No two cards in each category can both be in the case file.
        for (int i=0; i<suspects.length-1; i++) {
        	for (int j=i+1; j<suspects.length; j++) {
        		int[] clause = new int[2];
        		clause[0] = -getPairNum(caseFile, suspects[i]);
        		clause[1] = -getPairNum(caseFile, suspects[j]);
        		solver.addClause(clause);
        	}
        }
        
        for (int i=0; i<weapons.length-1; i++) {
        	for (int j=i+1; j<weapons.length; j++) {
        		int[] clause = new int[2];
        		clause[0] = -getPairNum(caseFile, weapons[i]);
        		clause[1] = -getPairNum(caseFile, weapons[j]);
        		solver.addClause(clause);
        	}
        }
        
        for (int i=0; i<rooms.length-1; i++) {
        	for (int j=i+1; j<rooms.length; j++) {
        		int[] clause = new int[2];
        		clause[0] = -getPairNum(caseFile, rooms[i]);
        		clause[1] = -getPairNum(caseFile, rooms[j]);
        		solver.addClause(clause);
        	}
        }
    }
        
    public void hand(String player, String[] handCards) 
    {
        for (int i=0; i<cards.length; i++) {
        	boolean found = false;
        	for (int j=0; j<handCards.length; j++) {
        		if (cards[i].equals(handCards[j])) {
        			solver.addClause(new int[] { getPairNum(player, handCards[j]) });
        			found = true;
        			break;
        		}
        	}
        	if (!found) {
        		solver.addClause(new int[] { -getPairNum(player, cards[i]) });
        	}
        }
    }

    public void suggest(String suggester, String card1, String card2, 
                        String card3, String refuter, String cardShown) 
    {
        if (refuter != null) {
        	addNegativeClausesAfterSuggestion(suggester, card1, card2, card3, refuter);
        	if (cardShown != null) {
        		solver.addClause(new int[] { getPairNum(refuter, cardShown) });
        	} else {
        		int[] clause = new int[3];
        		clause[0] = getPairNum(refuter, card1);
        		clause[1] = getPairNum(refuter, card2);
        		clause[2] = getPairNum(refuter, card3);
        		solver.addClause(clause);
        	}
        } else {
        	for (int i=0; i<numPlayers; i++) {
        		if (!players[i].equals(suggester)) {
        			addNegativeClausesAfterSuggestion(suggester, card1, card2, card3, suggester);
        		}
        	}
        }
    }
    
    /*
     * Helper method to add clauses about players not having cards after a suggestion
     */
    private void addNegativeClausesAfterSuggestion(String suggester, String card1, String card2, 
                        String card3, String refuter) {
    	int i = getPlayerNum(suggester)+1;
    	while (i != getPlayerNum(refuter)) {
    		if (i < numPlayers) {
    			solver.addClause(new int[] { -getPairNum(players[i], card1) });
    			solver.addClause(new int[] { -getPairNum(players[i], card2) });
    			solver.addClause(new int[] { -getPairNum(players[i], card3) });
    			i++;
    		} else {
    			i = 0;
    		}
    	}
    }

    public void accuse(String accuser, String card1, String card2, 
                       String card3, boolean isCorrect)
    {
        if (isCorrect) {
        	solver.addClause(new int[] { getPairNum(caseFile, card1) });
        	solver.addClause(new int[] { getPairNum(caseFile, card2) });
        	solver.addClause(new int[] { getPairNum(caseFile, card3) });
        } else {
        	int[] clause = new int[3];
        	clause[0] = -getPairNum(caseFile, card1);
        	clause[1] = -getPairNum(caseFile, card2);
        	clause[2] = -getPairNum(caseFile, card3);
        	solver.addClause(clause);
        }
    }

    public int query(String player, String card) 
    {
        return solver.testLiteral(getPairNum(player, card));
    }

    public String queryString(int returnCode) 
    {
        if (returnCode == SATSolver.TRUE)
            return "Y";
        else if (returnCode == SATSolver.FALSE)
            return "n";
        else
            return "-";
    }
        
    public void printNotepad() 
    {
        PrintStream out = System.out;
        for (String player : players) {
            out.print("\t" + player);
        }
        out.println("\t" + caseFile);
        for (String card : cards) {
            out.print(card + "\t");
            for (String player : players) {
                out.print(queryString(query(player, card)) + "\t");
            }
            out.println(queryString(query(caseFile, card)));
        }
    }
    
    /**
     * FINAL PROJECT
     * 
     * Let a solver play a Clue game. There are 3 games to choose from, each of which ends when it is
     * logically possible to know all the cards in the case file. This method is used for comparing the
     * performance of different SAT solvers in the experiment.
     * 
     * Statistics of each game:
     * Game 1: 722 clauses and 1413 literals, counting cards does not matter
     * Game 2: 673 clauses and 1362 literals, counting cards does matter
     * Game 3: 776 clauses and 1465 literals, counting cards does matter
     * 
     * @param gameNumber	the number of the game chosen, from 1 to 3
     */
    public void playGame(int gameNumber) {
    	if (gameNumber < 1 || gameNumber > 3) {
    		System.err.println("Invalid game number!");
    	} else {
        	changePlayerOrder();
        	if (gameNumber == 3) {
        		String[] myCards = {"pe", "ca", "st"};
                hand("pl", myCards);
                suggest("sc", "wh", "ro", "co", "pe", null);
                suggest("wh", "wh", "ro", "co", "pe", null);
                suggest("mu", "wh", "wr", "di", "pe", null);
                suggest("pe", "wh", "ro", "li", "sc", null);
                suggest("pl", "gr", "kn", "lo", "wh", "gr");
                suggest("sc", "wh", "ro", "bi", "wh", null);
                suggest("wh", "wh", "ro", "li", "sc", null);
                suggest("gr", "wh", "ro", "di", "sc", null);
                suggest("mu", "wh", "ro", "ki", "sc", null);
                suggest("pl", "wh", "ro", "ha", "sc", "wh");
                suggest("sc", "wh", "ro", "li", null, null);
                suggest("wh", "wh", "wr", "st", "pe", null);
                suggest("gr", "wh", "ro", "lo", "sc", null);
                suggest("mu", "sc", "ro", "di", "sc", null);
                suggest("pe", "sc", "ro", "st", "pl", "st");
                suggest("pl", "mu", "ro", "li", "sc", "ro");
                suggest("sc", "sc", "wr", "ha", "gr", null);
                suggest("wh", "wh", "kn", "ha", "gr", null);
                suggest("gr", "pe", "wr", "co", "pe", null);
                suggest("mu", "pe", "kn", "co", "pe", null);
                suggest("pe", "pl", "ro", "ba", "sc", null);
                suggest("pl", "mu", "kn", "ki", "wh", "ki");
                suggest("wh", "gr", "kn", "di", "sc", null);
                suggest("gr", "sc", "ro", "ki", "sc", null);
                suggest("mu", "sc", "re", "st", "pl", "st");
                suggest("pe", "sc", "wr", "bi", "wh", null);
                suggest("pl", "sc", "wr", "st", "pe", "wr");
        	} else if (gameNumber == 1) {
        		String[] myCards = {"sc", "pi", "di"};
                hand("sc", myCards);
                suggest("gr", "gr", "ro", "li", "mu", null);
                suggest("mu", "pl", "kn", "ba", "pl", null);
                suggest("pe", "pl", "wr", "ba", "pl", null);
                suggest("pl", "pl", "wr", "ki", "wh", null);
                suggest("sc", "gr", "ca", "ki", "gr", "gr");
                suggest("wh", "sc", "kn", "ba", "mu", null);
                suggest("gr", "sc", "pi", "ba", "pl", null);
                suggest("mu", "wh", "ro", "co", "pe", null);
                suggest("pe", "wh", "re", "co", "wh", null);
                suggest("pl", "wh", "wr", "di", "sc", "di");
                suggest("sc", "wh", "re", "co", "wh", "co");
                suggest("wh", "sc", "pi", "bi", "sc", "sc");
                suggest("gr", "sc", "ro", "bi", "pe", null);
                suggest("mu", "pl", "wr", "lo", "pe", null);
                suggest("pe", "pl", "re", "bi", "wh", null);
                suggest("pl", "wh", "re", "ki", "gr", null);
                suggest("sc", "mu", "wr", "bi", "pe", "wr");
                suggest("wh", "mu", "wr", "ba", "pe", null);
                suggest("gr", "sc", "pi", "co", "sc", "sc");
                suggest("mu", "pl", "re", "ki", "wh", null);
                suggest("pe", "pe", "ro", "li", "mu", null);
                suggest("pl", "wh", "pi", "lo", "sc", "pi");
                suggest("sc", "mu", "re", "bi", "gr", "re");
                suggest("wh", "mu", "ro", "di", "pe", null);
                suggest("gr", "pl", "re", "lo", "wh", null);
                suggest("mu", "wh", "re", "ha", "pe", null);
                suggest("pe", "sc", "re", "st", "pl", null);
                suggest("pl", "wh", "ro", "ha", "pe", null);
                suggest("sc", "wh", "pi", "st", "pl", "st");
        	} else if (gameNumber == 2) {
        		String[] myCards = {"kn", "wr", "lo"};
                hand("wh", myCards);
                suggest("pe", "sc", "ca", "ba", "pl", null);
                suggest("pl", "sc", "re", "ki", "sc", null);
                suggest("sc", "mu", "pi", "di", "gr", null);
                suggest("wh", "gr", "ca", "ki", "mu", "ca");
                suggest("gr", "pl", "pi", "st", "mu", null);
                suggest("mu", "sc", "pi", "lo", "sc", null);
                suggest("pe", "sc", "re", "di", "pl", null);
                suggest("pl", "pe", "wr", "ki", "sc", null);
                suggest("sc", "sc", "wr", "lo", "wh", "lo");
                suggest("wh", "gr", "re", "st", "mu", "st");
                suggest("gr", "wh", "re", "ha", "pe", null);
                suggest("mu", "pl", "ca", "ha", "pe", null);
                suggest("pe", "sc", "ro", "ki", "sc", null);
                suggest("pl", "pl", "wr", "li", "wh", "wr");
                suggest("sc", "wh", "kn", "ba", "wh", "kn");
                suggest("wh", "gr", "re", "ki", "pl", "re");
                suggest("gr", "pe", "re", "di", "mu", null);
                suggest("mu", "wh", "ro", "ki", "pe", null);
                suggest("pe", "pl", "ro", "st", "gr", null);
                suggest("pl", "pe", "ro", "li", "gr", null);
                suggest("sc", "wh", "wr", "bi", "wh", "wr");
                suggest("wh", "gr", "pi", "co", "sc", "gr");
                suggest("gr", "sc", "wr", "bi", "pl", null);
                suggest("mu", "pe", "re", "ba", "pl", null);
                suggest("pe", "wh", "wr", "ba", "pl", null);
                suggest("pl", "pl", "pi", "di", "mu", null);
                suggest("sc", "wh", "re", "ba", "pe", null);
                suggest("wh", "pl", "pi", "co", "pe", "pl");
        	}
    	}
    }
    
    /**
     * FINAL PROJECT
     * 
     * Compare the performance of ZChaffSolver, DPLLSolver without value ordering strategy and DPLLSolver
     * with value ordering strategy. Each solver will play 3 Clue games, where each game will be played a
     * number of times. The performance of a solver in a game will be the average time it takes to print out
     * the notepad at the end of the game, over all times played. The result is then printed out.
     */
    public static void main(String[] args) {
    	int numberOfTimesToPlay = 20;
        double[][] result = new double[3][3];
        for (int i=0; i<3; i++) {
        	for (int j=0; j<3; j++) {
        		long sumTime = 0;
        		for (int k=0; k<numberOfTimesToPlay; k++) {
        			ClueReasoner cr;
        			if (i == 0) {
        				cr = new ClueReasoner(new ZChaffSolver());
        			} else if (i == 1) {
        				cr = new ClueReasoner(new DPLLSolver(false));
        			} else {
        				cr = new ClueReasoner(new DPLLSolver(true));
        			}
        			cr.playGame(j+1);
        			long time = System.currentTimeMillis();
        			cr.printNotepad();
        			sumTime += System.currentTimeMillis() - time;
        		}
        		result[i][j] = sumTime / ((double) numberOfTimesToPlay);
        	}
        }
        System.out.println();
        System.out.println("\t\tGame 1\tGame 2\tGame 3");
        System.out.println("ZChaff" + "\t\t" + result[0][0] + "\t" + result[0][1] + "\t" + result[0][2]);
        System.out.println("DPLL w/o strat" + "\t" + result[1][0] + "\t" + result[1][1] + "\t" + result[1][2]);
        System.out.println("DPLL with strat" + "\t" + result[2][0] + "\t" + result[2][1] + "\t" + result[2][2]);
    }
    
    /* Change the player order from default to a customized order */
    private void changePlayerOrder() {
    	players = new String[]{"sc", "wh", "gr", "mu", "pe", "pl"};
    }
}

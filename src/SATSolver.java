/**
 * SATSolver.java - a simple Java interface to the zchaff SAT solver.
 * See http://cs.gettysburg.edu/~tneller/nsf/clue/ for details.
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

import java.util.*;

public abstract class SATSolver 
{
    public static final int FALSE = -1;
    public static final int UNKNOWN = 0;
    public static final int TRUE = 1;

    public ArrayList<int[]> clauses = new ArrayList<int[]>();
    public ArrayList<int[]> queryClauses = new ArrayList<int[]>();

    public void addClause(int[] clause) {
        clauses.add((int[]) clause.clone());
    }
        
    public void clearClauses() {
        clauses.clear();
    }

    public void addQueryClause(int[] clause) {
        queryClauses.add((int[]) clause.clone());
    }
        
    public void clearQueryClauses() {
        queryClauses.clear();
    }

    public abstract boolean makeQuery();

    public int testLiteral(int literal) {
        int result = UNKNOWN;
        clearQueryClauses();   
        int[] clauseT = {literal};
        addQueryClause(clauseT);
        if (!makeQuery())
            result = FALSE;
        else {
            clearQueryClauses();
            int[] clauseF = {-literal};
            addQueryClause(clauseF);
            if (!makeQuery())
                result = TRUE;
        }
        clearQueryClauses();
        return result;
    }       

    public static void main(String[] args) 
    {
        // Liar and truth-teller example test code:
        int[][] clauses = {{1, 2, 3}, {-1, -2}, {-1, -3}, {-2, -3}, {-4, 2}, {-2, 4}, {-5, 2}, {-2, 5}, {-6, -3}, {3, 6}, {4, 5, 6}, {-4, -5, -6}};
        SATSolver s = new ZChaffSolver();
        for (int i = 0; i < clauses.length; i++)
            s.addClause(clauses[i]);
        System.out.println("Knowledge base is satisfiable: " + s.makeQuery());
        System.out.print("Is this statement true? ");
        int result = s.testLiteral(-1);
        if (result == FALSE)
            System.out.println("No.");
        else if (result == TRUE)
            System.out.println("Yes.");
        else
            System.out.println("Unknown."); 
    }
}

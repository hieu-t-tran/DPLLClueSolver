/**
 * This class was originally the makeQuery() method of SATSolver.java, but was moved into
 * this separate class so that SATSolver could become an abstract class.
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
import java.util.*;

public class ZChaffSolver extends SATSolver {
	
	public boolean makeQuery() {
        try {
            int maxVar = 0;
            ArrayList<int[]> allClauses = new ArrayList<int[]>(clauses);
            allClauses.addAll(queryClauses);
            for (int[] clause: allClauses)
                for (int literal: clause)
                    maxVar = Math.max(Math.abs(literal), maxVar);
            PrintStream out = new PrintStream(new File("query.cnf"));
            //out.println("c This DIMACS format CNF file was generated by SatSolver.java");
            //out.println("c Do not edit.");
            out.println("p cnf " + maxVar + " " + allClauses.size());
            for (int[] clause: allClauses) {
                for (int literal: clause)
                    out.print(literal + " ");
                out.println("0");
            }
            out.close();
            Process process = Runtime.getRuntime().exec("./zchaff query.cnf");
            Scanner sc = new Scanner(process.getInputStream());
            sc.findWithinHorizon("RESULT:", 0);
            String result = sc.next();
            sc.close();
            process.waitFor();
            return result.equals("SAT");
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }
}

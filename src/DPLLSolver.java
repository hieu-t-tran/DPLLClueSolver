/**
 * Institution: Dickinson College
 * Course: COMP 364: Artificial Intelligence
 * Semester: Fall 2020
 * Instructor: Professor Michael Skalak
 * 
 * FINAL PROJECT
 * 
 * Basic implementation of the DPLL algorithm to solve SAT problems. This implementation can use the value
 * ordering strategy to improve performance.
 * 
 * @author Hieu Tran
 * @version December 2, 2020
 * 

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

public class DPLLSolver extends SATSolver {
	
	boolean useValueOrdering; // whether the value ordering strategy is used or not
	
	public DPLLSolver(boolean useValueOrdering) {
		super();
		this.useValueOrdering = useValueOrdering;
	}

	/**
	 * Decide if the query clauses can make the knowledge base satisfiable or not. This method is based on
	 * Figure 7.17 of Russell and Norvig (third edition).
	 * 
	 * @return true if the set of clauses is satisfiable, false otherwise
	 */
	public boolean makeQuery() {
		ArrayList<int[]> allClauses = new ArrayList<int[]>(clauses);
        allClauses.addAll(queryClauses);
        HashSet<Integer> symbols = new HashSet<Integer>();
        for (int[] clause : allClauses) {
            for (int literal : clause) {
                symbols.add(Math.abs(literal));
            }
        }
        HashMap<Integer, Boolean> model = new HashMap<Integer, Boolean>();
        return dpllAlgorithm(allClauses, symbols, model);
	}
	
	/**
	 * Implementation of the DPLL algorithm to decide if a set of clauses is satisfiable or not. This method
	 * is based on Figure 7.17 of Russell and Norvig (third edition).
	 * 
	 * @param clauses	the set of clauses
	 * @param symbols	the set of symbols
	 * @param model		the model that the algorithm generates
	 * 
	 * @return true if the set of clauses is satisfiable, false otherwise
	 */
	private boolean dpllAlgorithm(ArrayList<int[]> clauses, HashSet<Integer> symbols,
			HashMap<Integer, Boolean> model) {
		
		int checkResult = checkClauses(clauses, model);
		if (checkResult == TRUE) {
			return true;
		}
		if (checkResult == FALSE) {
			return false;
		}
		
		SymbolValuePair pureSymbol = findPureSymbol(clauses, symbols, model);
		if (pureSymbol != null) {
			HashSet<Integer> newSymbols = new HashSet<Integer>(symbols);
			newSymbols.remove(pureSymbol.symbol);
			HashMap<Integer, Boolean> newModel = new HashMap<Integer, Boolean>(model);
			newModel.put(pureSymbol.symbol, pureSymbol.value);
			return dpllAlgorithm(clauses, newSymbols, newModel);
		}
		
		SymbolValuePair unitClause = findUnitClause(clauses, model);
		if (unitClause != null) {
			HashSet<Integer> newSymbols = new HashSet<Integer>(symbols);
			newSymbols.remove(unitClause.symbol);
			HashMap<Integer, Boolean> newModel = new HashMap<Integer, Boolean>(model);
			newModel.put(unitClause.symbol, unitClause.value);
			return dpllAlgorithm(clauses, newSymbols, newModel);
		}
		
		int chosenSymbol = symbols.iterator().next();
		HashSet<Integer> newSymbols = new HashSet<Integer>(symbols);
		newSymbols.remove(chosenSymbol);
		if (!useValueOrdering) {
			HashMap<Integer, Boolean> modelWithChosenTrue = new HashMap<Integer, Boolean>(model);
			modelWithChosenTrue.put(chosenSymbol, true);
			if (dpllAlgorithm(clauses, newSymbols, modelWithChosenTrue)) {
				return true;
			} else {
				newSymbols = new HashSet<Integer>(symbols);
				HashMap<Integer, Boolean> modelWithChosenFalse = new HashMap<Integer, Boolean>(model);
				modelWithChosenFalse.put(chosenSymbol, false);
				return dpllAlgorithm(clauses, newSymbols, modelWithChosenFalse);
			}
		} else {
			if (trueValueIsMoreFrequent(chosenSymbol, clauses, model)) {
				HashMap<Integer, Boolean> modelWithChosenTrue = new HashMap<Integer, Boolean>(model);
				modelWithChosenTrue.put(chosenSymbol, true);
				if (dpllAlgorithm(clauses, newSymbols, modelWithChosenTrue)) {
					return true;
				} else {
					newSymbols = new HashSet<Integer>(symbols);
					HashMap<Integer, Boolean> modelWithChosenFalse = new HashMap<Integer, Boolean>(model);
					modelWithChosenFalse.put(chosenSymbol, false);
					return dpllAlgorithm(clauses, newSymbols, modelWithChosenFalse);
				}
			} else {
				HashMap<Integer, Boolean> modelWithChosenFalse = new HashMap<Integer, Boolean>(model);
				modelWithChosenFalse.put(chosenSymbol, false);
				if (dpllAlgorithm(clauses, newSymbols, modelWithChosenFalse)) {
					return true;
				} else {
					newSymbols = new HashSet<Integer>(symbols);
					HashMap<Integer, Boolean> modelWithChosenTrue = new HashMap<Integer, Boolean>(model);
					modelWithChosenTrue.put(chosenSymbol, true);
					return dpllAlgorithm(clauses, newSymbols, modelWithChosenTrue);
				}
			}
		}
	}
	
	/**
	 * Check if the set of clauses can be true in the model.
	 * 
	 * @param clauses	the set of clauses
	 * @param model		the model
	 * 
	 * @return TRUE (1) if all clauses are true, FALSE (-1) if any clause is false, UNKNOWN (0) otherwise
	 */
	private int checkClauses(ArrayList<int[]> clauses, HashMap<Integer, Boolean> model) {
		boolean allClausesAreTrue = true;
		for (int[] clause : clauses) {
			int value = checkOneClause(clause, model);
			if (value == FALSE) {
				return FALSE;
			} else if (value == UNKNOWN) {
				allClausesAreTrue = false;
			}
		}
		if (allClausesAreTrue) {
			return TRUE;
		} else {
			return UNKNOWN;
		}
	}
	
	/**
	 * Check if the clause can be true in the model.
	 * 
	 * @param clause	the clause
	 * @param model		the model
	 * 
	 * @return TRUE (1) if any literal is true, FALSE (-1) if all literals are false, UNKNOWN (0) otherwise
	 */
	private int checkOneClause(int[] clause, HashMap<Integer, Boolean> model) {
		boolean allLiteralsAreFalse = true;
		for (int literal : clause) {
			int value = checkLiteral(literal, model);
			if (value == TRUE) {
				return TRUE;
			} else if (value == UNKNOWN) {
				allLiteralsAreFalse = false;
			}
		}
		if (allLiteralsAreFalse) {
			return FALSE;
		} else {
			return UNKNOWN;
		}
	}
	
	/**
	 * Check if the literal is true in the model.
	 * 
	 * @param literal	the literal
	 * @param model		the model
	 * 
	 * @return TRUE (1) if the literal is true, FALSE (-1) if the literal is false, UNKNOWN (0) otherwise
	 */
	private int checkLiteral(int literal, HashMap<Integer, Boolean> model) {
		Boolean value = model.get(Math.abs(literal));
		if (value == null) {
			return UNKNOWN;
		} else if (literal > 0) {
			if (value) {
				return TRUE;
			} else {
				return FALSE;
			}
		} else {
			if (value) {
				return FALSE;
			} else {
				return TRUE;
			}
		}
	}
	
	/**
	 * Find the list of clauses that are either FALSE or UNKNOWN. This method is used for finding pure
	 * symbols and unit clauses.
	 * 
	 * @param clauses	the set of clauses
	 * @param model		the model
	 * 
	 * @return the list of clauses that are either FALSE or UNKNOWN
	 */
	private ArrayList<int[]> findNonTrueClauses(ArrayList<int[]> clauses, HashMap<Integer, Boolean> model) {
		ArrayList<int[]> nonTrueClauses = new ArrayList<int[]>();
		for (int[] clause : clauses) {
			if (checkOneClause(clause, model) != TRUE) {
				nonTrueClauses.add(clause);
			}
		}
		return nonTrueClauses;
	}
	
	/**
	 * Find a pure symbol in the set of clauses.
	 * 
	 * @param clauses	the set of clauses
	 * @param symbols	the set of symbols
	 * @param model		the model
	 * 
	 * @return a pure symbol with its value, or null if there is no pure symbol
	 */
	private SymbolValuePair findPureSymbol(ArrayList<int[]> clauses, HashSet<Integer> symbols,
			HashMap<Integer, Boolean> model) {
		
		ArrayList<int[]> nonTrueClauses = findNonTrueClauses(clauses, model);
		HashSet<Integer> pureTrueSymbols = new HashSet<Integer>();
		HashSet<Integer> pureFalseSymbols = new HashSet<Integer>();
		
		for (int[] clause : nonTrueClauses) {
			if (checkOneClause(clause, model) == UNKNOWN) {
				for (int literal : clause) {
					if (symbols.contains(Math.abs(literal))) {
						if (literal > 0) {
							pureTrueSymbols.add(literal);
						} else {
							pureFalseSymbols.add(-literal);
						}
					}
				}
			}
		}
		
		for (Integer symbol : symbols) {
			if (pureTrueSymbols.contains(symbol) && pureFalseSymbols.contains(symbol)) {
				pureTrueSymbols.remove(symbol);
				pureFalseSymbols.remove(symbol);
			}
		}
		
		if (pureTrueSymbols.size() > 0) {
			return new SymbolValuePair(pureTrueSymbols.iterator().next(), true);
		} else if (pureFalseSymbols.size() > 0) {
			return new SymbolValuePair(pureFalseSymbols.iterator().next(), false);
		} else {
			return null;
		}
	}
	
	/**
	 * Find a unit clause in the set of clauses. 
	 * 
	 * @param clauses	the set of clauses
	 * @param model		the model
	 * 
	 * @return a symbol with its value corresponding to the unit clause found, or null if there is no unit clause
	 */
	private SymbolValuePair findUnitClause(ArrayList<int[]> clauses, HashMap<Integer, Boolean> model) {
		for (int[] clause : clauses) {
			if (checkOneClause(clause, model) == UNKNOWN) {
				int countUnknown = 0;
				int lastUnknownLiteral = 0;
				for (int literal : clause) {
					if (checkLiteral(literal, model) == UNKNOWN) {
						countUnknown++;
						lastUnknownLiteral = literal;
					}
				}
				if (countUnknown == 1) {
					if (lastUnknownLiteral > 0) {
						return new SymbolValuePair(lastUnknownLiteral, true);
					} else {
						return new SymbolValuePair(-lastUnknownLiteral, false);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Determine if the true literal of a symbol appears more frequently than the false literal among the
	 * clauses that are UNKNOWN in the model. This method is used when the value ordering strategy is used.
	 * 
	 * @param symbol	the symbol
	 * @param clauses	the set of clauses
	 * @param model		the model
	 * 
	 * @return true if the true literal of the symbol appears more frequently, false otherwise
	 */
	private boolean trueValueIsMoreFrequent(int symbol, ArrayList<int[]> clauses,
			HashMap<Integer, Boolean> model) {
		
		int countTrue = 0;
		int countFalse = 0;
		for (int[] clause : clauses) {
			if (checkOneClause(clause, model) != TRUE) {
				for (int literal : clause) {
					if (literal == symbol) {
						countTrue++;
					} else if (literal == -symbol) {
						countFalse++;
					}
				}
			}
		}
		return (countTrue >= countFalse);
	}
	
	// Print all clauses for debugging purposes.
	private void printClauses(ArrayList<int[]> clauses) {
		for (int[] clause : clauses) {
			printOneClause(clause);
		}
	}
	
	// Print one clause for debugging purposes.
	private void printOneClause(int[] clause) {
		for (int literal : clause) {
			System.out.print(literal + "\t");
		}
		System.out.println();
	}
	
	/*
	 * Represent a pair of a symbol and its value. This is used for finding pure symbols and unit clauses.
	 */
	private class SymbolValuePair {
		int symbol;
		boolean value;
		
		public SymbolValuePair(int symbol, boolean value) {
			this.symbol = symbol;
			this.value = value;
		}
	}
}

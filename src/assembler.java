import java.util.*;

class assembler {
	public List<String> tinyTable;
	public List<String> varTable;
	public List<String> localTable;
	public List<Integer> assemblerBBIndex;
	public String t1, t2, t3, t4;
	private boolean use;
	private int linkCount;
	public int space = 0;
	public int parameter = 0;

	public assembler() {
		tinyTable = new Vector<String>();
		varTable = new Vector<String>();
		localTable = new Vector<String>();
		assemblerBBIndex = new Vector<Integer>();
		use = false;
	}

	public void init(List<msTable> mTable) {
		for (msTable t: mTable) {
			if (t.scope.equals("__global")) {
				
				/*Iterator sti = t.symbolTable.iterator();

				while (sti.hasNext()) {
					mSymbol ese = (mSymbol) sti.next();
					tinyTable.add("var " + ese.getName());
				}*/

				int prefill = 0;
				for (mSymbol sti: t.symbolTable) {
					if (sti.getType().equals("STRING")) {
						tinyTable.add("str " + sti.getName() + " " + sti.getValue());
					}
					else {
						tinyTable.add("var " + sti.getName());
					}
					assemblerBBIndex.add(0);
				}
				tinyTable.add("push");
				tinyTable.add("push r0");
				tinyTable.add("push r1");
				tinyTable.add("push r2");
				tinyTable.add("push r3");

				tinyTable.add("jsr main");
				tinyTable.add("sys halt");

				for (int i = 0; i < 7; i++) {
					assemblerBBIndex.add(0);
				}
			}
			else { break; }
		}
	}

	public void contextSwitch(String target) {
		tinyTable.add("push r0");
		tinyTable.add("push r1");
		tinyTable.add("push r2");
		tinyTable.add("push r3");
		tinyTable.add("jsr " + target.trim());
		tinyTable.add("pop r3");
		tinyTable.add("pop r2");
		tinyTable.add("pop r1");
		tinyTable.add("pop r0");
	}

	public void fin() {
		tinyTable.add("unlnk");
		tinyTable.add("ret");
	}

	public List<Integer> getBB() {
		return assemblerBBIndex;
	}

	public List<String> process(List<String> irTable, List<Integer> bbIndex, boolean debug) {
		int registerCounter = 0;
		int i = 0;
		int irIndex = 0;
		int currentBB;
		int previousSize;
		//List<String> strDeclaration = new LinkedList<String>();
		for (String ir : irTable) {
			if (debug) { tinyTable.add("----- " + ir); }
			currentBB = bbIndex.get(irIndex++);
			previousSize = tinyTable.size();
			//System.out.println(previousSize + " : " + ir);
			if (ir.startsWith("STORES")) {
				//String[] mString = ir.split("\"");
				//strDeclaration.add("str " + mString[2].trim() + " \"" + mString[1] + "\"");
				i++;
				irIndex++;
				continue;
			}
			String[] tiny = ir.split("\\s");
			tinyTable.add(";Parsing: " + ir);
			switch(tiny.length) {
				case 1:
					if (tiny[0].equals("RET")) {
						fin();
					}
					else if (tiny[0].equals("POP")) {
						tinyTable.add("pop");
					}
					else if (tiny[0].equals("PUSH")) {
						tinyTable.add("push");
					}
				break;
				case 2:
					if (tiny[1].startsWith("$T")) {
						registerCounter = varTable.indexOf(tiny[1]);
						if (registerCounter == -1) {
							varTable.add(tiny[1]);
							registerCounter = varTable.size() - 1;
            }
						t1 = "r" + registerCounter;
					}
					else if (tiny[1].startsWith("$L")) {
						registerCounter = localTable.indexOf(tiny[1]) + 1;
						if (localTable.indexOf(tiny[1]) == -1) {
							localTable.add(tiny[1]);
							registerCounter = localTable.size();
						}
						t1 = "$-" + registerCounter;
					}
					else if (tiny[1].startsWith("$P")) {
						t1 = "$" + (5 + 0 + parameter - Integer.parseInt(tiny[1].substring(2)));
					}
					else {
						t1 = tiny[1];
					}

					if (tiny[0].equals("MAKESPACE")) {
						space = Integer.parseInt(tiny[1]);
						localTable = new Vector<String>();
					}
					else if (tiny[0].equals("MAKEPARAM")) {
						parameter = Integer.parseInt(tiny[1]);
					}
					else if (tiny[0].equals("JUMP")) {
						tinyTable.add("jmp " + tiny[1].trim());
					}
					else if (tiny[0].equals("LABEL")) {
						tinyTable.add("label " + tiny[1].trim());
						if (!tiny[1].startsWith("LABEL")) {
							tinyTable.add("link " + space);
						}
					}
					else if (tiny[0].equals("READI")) {
						tinyTable.add("sys readi " + t1);
					}
					else if (tiny[0].equals("READF")) {
						tinyTable.add("sys readr " + t1);
					}
					else if (tiny[0].equals("WRITEI")) {
						tinyTable.add("sys writei " + t1);
					}
					else if (tiny[0].equals("WRITEF")) {
						tinyTable.add("sys writer " + t1);
					}
					else if (tiny[0].equals("WRITES")) {
						tinyTable.add("sys writes " + t1);
					}
					else if (tiny[0].equals("JSR")) {
						linkCount = 0;
						contextSwitch(t1);
					}
					else if (tiny[0].equals("POP")) {
						tinyTable.add("pop " + t1);
					}
					else if (tiny[0].equals("PUSH")) {
						tinyTable.add("push " + t1);
					}
				break;
				case 3:
					if (tiny[1].startsWith("$T")) {
						use = true;
						registerCounter = varTable.indexOf(tiny[1]);
						if (varTable.indexOf(tiny[1]) == -1) {
							varTable.add(tiny[1]);
							registerCounter = varTable.size() - 1;
						}
						t1 = "r" + registerCounter;
					}
					else if (tiny[1].startsWith("$L")) {
						use = true;
						registerCounter = localTable.indexOf(tiny[1]);
						if (localTable.indexOf(tiny[1]) == -1) {
							localTable.add(tiny[1]);
							registerCounter = localTable.size();
						}
						else {
							registerCounter++;
						}
						t1 = "$-" + registerCounter;
					}
					else if (tiny[1].startsWith("$P")) {
						t1 = "$" + (5 + 0 + parameter - Integer.parseInt(tiny[1].substring(2)));
						/*if (tiny[1].equals("$P1")) {
							t1 = "$1";
						}
						else if (tiny[1].equals("$P2")) {
							t1 = "$2";
						}*/
					}
					else if (tiny[1].startsWith("$R")) {
						t1 = "$" + (5 + 1 + parameter);
					}
					else {
						t1 = tiny[1];
					}

					if (tiny[2].startsWith("$T")) {
						use = true;
						registerCounter = varTable.indexOf(tiny[2]);
						if (varTable.indexOf(tiny[2]) == -1) {
							varTable.add(tiny[2]);
							registerCounter = varTable.size() - 1;
						}
						t2 = "r" + registerCounter;
					}
					else if (tiny[2].startsWith("$L")) {
						use = true;
						registerCounter = localTable.indexOf(tiny[2]);
						if (localTable.indexOf(tiny[2]) == -1) {
							localTable.add(tiny[2]);
							registerCounter = localTable.size();
						}
						else {
							registerCounter++;
						}
						t2 = "$-" + registerCounter;
					}
					else if (tiny[2].startsWith("$P")) {
						t2 = "$" + (5 + 0 + parameter - Integer.parseInt(tiny[2].substring(2)));
						/*if (tiny[2].equals("$P1")) {
							t2 = "$1";
						}
						else if (tiny[1].equals("$P2")) {
							t2 = "$2";
						}*/
					}
					else if (tiny[2].startsWith("$R")) {
						use = false;
						t2 = "$" + (5 + parameter + 1);
					}
					else {
						t2 = tiny[2];
					}

					if (use) {
						use = false;
						tinyTable.add("move " + t1 + " " + t2);
					}
					else {
						varTable.add(tiny[2]);
						t3 = "r" + (varTable.size() - 1);
						tinyTable.add("move " + t1 + " " + t3);
						tinyTable.add("move " + t3 + " " + t2);
					}
				break;
				case 4:
					if (tiny[1].startsWith("$T")) {
						use = true;
						registerCounter = varTable.indexOf(tiny[1]);
						if (varTable.indexOf(tiny[1]) == -1) {
							varTable.add(tiny[1]);
							registerCounter = varTable.size() - 1;
						}
						t1 = "r" + registerCounter;
					}
					else if (tiny[1].startsWith("$L")) {
						use = true;
						registerCounter = localTable.indexOf(tiny[1]);
						if (localTable.indexOf(tiny[1]) == -1) {
							localTable.add(tiny[1]);
							registerCounter = localTable.size();
						}
						else {
							registerCounter++;
						}
						t1 = "$-" + registerCounter;
					}
					else if (tiny[1].startsWith("$P")) {
						t1 = "$" + (5 + 0 + parameter - Integer.parseInt(tiny[1].substring(2)));
						/*if (tiny[1].equals("$P1")) {
							t1 = "$1";
						}
						else if (tiny[1].equals("$P2")) {
							t1 = "$2";
						}*/
					}
					else if (tiny[1].startsWith("$R")) {
						t1 = "$" + (5 + parameter + 1);
					}
					else {
						t1 = tiny[1];
					}

					if (tiny[2].startsWith("$T")) {
						use = true;
						registerCounter = varTable.indexOf(tiny[2]);
						if (varTable.indexOf(tiny[2]) == -1) {
							varTable.add(tiny[2]);
							registerCounter = varTable.size() - 1;
						}
						t2 = "r" + registerCounter;
					}
					else if (tiny[2].startsWith("$L")) {
						use = true;
						registerCounter = localTable.indexOf(tiny[2]);
						if (localTable.indexOf(tiny[2]) == -1) {
							localTable.add(tiny[2]);
							registerCounter = localTable.size();
						}
						else {
							registerCounter++;
						}
						t2 = "$-" + registerCounter;
					}
					else if (tiny[2].startsWith("$P")) {
						t2 = "$" + (5 + 0 + parameter - Integer.parseInt(tiny[2].substring(2)));
						/*if (tiny[2].equals("$P1")) {
							t2 = "$1";
						}
						else if (tiny[2].equals("$P2")) {
							t2 = "$2";
						}*/
					}
					else if (tiny[2].startsWith("$R")) {
						t2 = "$" + (5 + parameter + 1);
					}
					else {
						t2 = tiny[2];
					}

					if (tiny[3].startsWith("$T")) {
						use = true;
						registerCounter = varTable.indexOf(tiny[3]);
						if (varTable.indexOf(tiny[3]) == -1) {
							varTable.add(tiny[3]);
							registerCounter = varTable.size() - 1;
						}
						t3 = "r" + registerCounter;
					}
					else if (tiny[3].startsWith("$L")) {
						use = true;
						registerCounter = localTable.indexOf(tiny[3]);
						if (localTable.indexOf(tiny[3]) == -1) {
							localTable.add(tiny[3]);
							registerCounter = localTable.size();
						}
						else {
							registerCounter++;
						}
						t3 = "$-" + registerCounter;
					}
					else if (tiny[3].startsWith("$P")) {
						t3 = "$" + (5 + 0 + parameter - Integer.parseInt(tiny[3].substring(2)));
						/*if (tiny[3].equals("$P1")) {
							t3 = "$1";
						}
						else if (tiny[3].equals("$P2")) {
							t3 = "$2";
						}*/
					}
					else if (tiny[3].startsWith("$R")) {
						t3 = "$" + (5 + parameter + 1);
					}
					else {
						t3 = tiny[3];
					}

					if (tiny[1].startsWith("$L") && tiny[2].startsWith("$L")) {
						use = false;
					}

					if (use) {
						use = false;
						t4 = t2;
					}
					else {
						varTable.add(tiny[2]);
						t4 = "r" + (varTable.size() - 1);
						tinyTable.add("move " + t2 + " " + t4);
					}

					if (tiny[0].startsWith("ADD") 
						|| tiny[0].startsWith("SUB")
						|| tiny[0].startsWith("MULT")
						|| tiny[0].startsWith("DIV")) {
						tinyTable.add("move " + t1 + " " + t3);
						char lastc = tiny[0].charAt(tiny[0].length() - 1);
						switch (lastc) {
							case 'I':
							switch (tiny[0].charAt(0)) {
								case 'A': tinyTable.add("addi " + t2 + " " + t3); break;
								case 'S': tinyTable.add("subi " + t2 + " " + t3); break;
								case 'M': tinyTable.add("muli " + t2 + " " + t3); break;
								case 'D': tinyTable.add("divi " + t2 + " " + t3); break;
								default: tinyTable.add("Unhandled integer operation");
							}
							break;
							case 'F':
							switch (tiny[0].charAt(0)) {
								case 'A': tinyTable.add("addr " + t2 + " " + t3); break;
								case 'S': tinyTable.add("subr " + t2 + " " + t3); break;
								case 'M': tinyTable.add("mulr " + t2 + " " + t3); break;
								case 'D': tinyTable.add("divr " + t2 + " " + t3); break;
								default: tinyTable.add("Unhandled float operation");
							}
							break;
							default: tinyTable.add("Unrecognized datatype");
						}
					}
					else if (tiny[0].startsWith("GE")) {
						if (tiny[0].equals("GEF") || tiny[0].equals("GEQF")) {
							tinyTable.add("cmpr " + t1 + " " + t4);
						}
						else {
							tinyTable.add("cmpi " + t1 + " " + t4);
						}

						if (tiny[0].startsWith("GEQ")) {
							tinyTable.add("jge " + t3);
						}
						else {
							tinyTable.add("jgt " + t3);
						}
					}
					else if (tiny[0].startsWith("LE")) {
						if (tiny[0].equals("LEF") || tiny[0].equals("LEQF")) {
							tinyTable.add("cmpr " + t1 + " " + t4);
						}
						else {
							tinyTable.add("cmpi " + t1 + " " + t4);
						}

						if (tiny[0].startsWith("LEQ")) {
							tinyTable.add("jle " + t3);
						}
						else {
							tinyTable.add("jlt " + t3);
						}
					}
					else if (tiny[0].startsWith("NE")) {
						if (tiny[0].equals("NEF")) {
							tinyTable.add("cmpr " + t1 + " " + t4);
						}
						else {
							tinyTable.add("cmpi " + t1 + " " + t4);
						}
						tinyTable.add("jne " + t3);
					}
					else if (tiny[0].startsWith("EQ")) {
						if (tiny[0].equals("EQF")) {
							tinyTable.add("cmpr " + t1 + " " + t4);
						}
						else {
							tinyTable.add("cmpi " + t1 + " " + t4);
						}
						tinyTable.add("jeq " + t3);
					}
				break;
			}
			i += tinyTable.size() - previousSize;
			//System.out.println(previousSize + " -> " + tinyTable.size() + " (" + i + ")");
			for (int j = previousSize; j < tinyTable.size(); j++) {
				assemblerBBIndex.add(currentBB);
			}
		}
		//fin();

		/*if (strDeclaration.size() != 0) {
			tinyTable.addAll(0, strDeclaration);
		}*/
		tinyTable.add("end");
		assemblerBBIndex.add(assemblerBBIndex.get(assemblerBBIndex.size() - 1));
		return tinyTable;
	}
}

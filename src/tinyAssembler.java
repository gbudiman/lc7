import java.util.*;

class tinyAssembler {
	public List<String> instruction;
	public List<Integer> bbIndex;
	public List<Integer> scopeIndex;
	public Vector<Vector<String>> rxm;
	public Vector<String> rUsed;
	public Vector<String> persistentUse;
	//public Vector<Vector<Integer>> distantUse;
	public Vector<tinyRegister> regAlloc;
	public Vector<tinyRegister> debugRegAlloc;
	public Map<String, String> spillRegister;
	public Map<Integer, String> spillAction;
	private int registerSize;

	public tinyAssembler(List<String> _tiny, List<Integer> _bbIndex, List<Integer> _scopeIndex) {
		instruction = _tiny;
		bbIndex = _bbIndex;
		scopeIndex = _scopeIndex;
		persistentUse = new Vector<String>();
		registerSize = 4;
		if (instruction.size() != bbIndex.size()) {
			System.out.println("WARNING: instruction : " 
					+ instruction.size()
					+ " bbIndex: "
					+ bbIndex.size());
		}
		rxm = new Vector<Vector<String>>();
		//distantUse = new Vector<Vector<Integer>>();
		regAlloc = new Vector<tinyRegister>();
		debugRegAlloc = new Vector<tinyRegister>();
		for (int i = 0; i < instruction.size(); i++) {
			rxm.add(new Vector<String>());
			//distantUse.add(new Vector<Integer>());
			regAlloc.add(new tinyRegister(registerSize));
			debugRegAlloc.add(new tinyRegister(registerSize));
		}
		spillRegister = new HashMap<String, String>();
		spillAction = new HashMap<Integer, String>();
	}

	public void process() {
		int indexLatch = -1;
		int scopeLatch = -1;
		int jx = 0;
		rUsed = new Vector<String>();
		for (int i = instruction.size() - 1; i >= 0; i--) {
			if (indexLatch != bbIndex.get(i)) {
				indexLatch = bbIndex.get(i);
				rUsed = new Vector<String>();
			}

			if (!instruction.get(i).startsWith(";")) {
				String[] ks = instruction.get(i).split("\\s");
				if (instruction.get(i).startsWith("cmp")) {
					if (ks[1].matches("x[0-9]+")) rUsed.add(ks[1]);
					//if (rUsed.indexOf(ks[1]) == -1) rUsed.add(ks[1]);
					if (ks[2].matches("x[0-9]+")) rUsed.add(ks[2]);
					//if (rUsed.indexOf(ks[1]) == -1) rUsed.add(ks[2]);
				}
				else if (instruction.get(i).startsWith("add")
					|| instruction.get(i).startsWith("sub")
					|| instruction.get(i).startsWith("mul")
					|| instruction.get(i).startsWith("div")) {
					if (ks[2].matches("x[0-9]+")) rUsed.remove(ks[2]);
					if (ks[1].matches("x[0-9]+")) rUsed.add(ks[1]);
					//if (rUsed.indexOf(ks[1]) == -1) rUsed.add(ks[1]);
				}
				else if (instruction.get(i).startsWith("move")) {
					if (ks[2].matches("x[0-9]+")) rUsed.remove(ks[2]);
					if (ks[1].matches("x[0-9]+")) rUsed.add(ks[1]);
					//if (isInteger(ks[1]) && rUsed.indexOf(ks[1]) == -1) rUsed.add(ks[1]);
				}
				/*else if ((instruction.get(i).startsWith("pop")
								|| instruction.get(i).startsWith("push"))
								&& ks.length == 2) {
					if (Integer.parseInt(ks[1].substring(1)) >= registerSize) {
						if (ks[1].matches("r[0-9]+")) {
							rUsed.add(ks[1].replace("r", "x"));
							instruction.set(i, ks[0] + " " + ks[1].replace("r", "x"));
						}
					}
				}*/
				else if ((instruction.get(i).startsWith("pop")
								|| instruction.get(i).startsWith("push"))
								&& ks.length == 2) {
					if (ks[1].matches("x[0-9]+")) rUsed.add(ks[1]);
					//if (ks[0].equals("pop") && ks[1].matches("x[0-9]+")) persistentUse.add(ks[1]);
				}
			}
			Vector<String> vd = new Vector<String>();
			for (int ci = 0; ci < rUsed.size(); ci++) {
				if (!vd.contains(rUsed.get(ci))) {
					vd.add(rUsed.get(ci));
				}
			}
			rxm.setElementAt(vd, i);
		}

		doAllocate();
	}

	public boolean isInteger(String _c) {
		if (Character.isLetter(_c.charAt(0))) {
			//System.out.println(_c + " -> false " + _c.charAt(0));
			return true;
		}
		return false;
	}

	public void doAllocate() {
		tinyRegister tempR = new tinyRegister(registerSize);
		int indexLatch = -1;
		int scopeLatch = -1;
		for (int i = 0; i < instruction.size(); i++) {
			if (i > 0) {
				tempR = regAlloc.get(i-1);
			}
			//System.out.println(";processing: " + instruction.get(i) + " " + tempR.dataVector.toString());
			if (indexLatch != bbIndex.get(i)) {
				indexLatch = bbIndex.get(i);
				if (scopeLatch == scopeIndex.get(i)) {
					for (int pi = 0; pi < registerSize; pi++) {
						if (!persistentUse.contains(tempR.dataVector.get(pi))) {
							tempR.dataVector.set(pi, null);
							tempR.boolVector.set(pi, false);
						}
					}
				}	
				else {
					tempR = new tinyRegister(registerSize);
				}
			}
			if (scopeLatch != scopeIndex.get(i)) {
				scopeLatch = scopeIndex.get(i);
				persistentUse = new Vector<String>();
			}

			//System.out.println("--> start " + tempR.dataVector.toString());
			String[] ks = instruction.get(i).split("\\s");
			if (ks[0].startsWith("move")) {
				if (ks[2].matches("x[0-9]+")) tempR.ensure(ks[2], i, instruction, spillRegister, spillAction, false);
				debugRegAlloc.setElementAt(tempR.clone(), i);
				if (ks[1].matches("x[0-9]+")) tempR.free(ks[1], rxm.get(i + 1), persistentUse);
				regAlloc.setElementAt(tempR.clone(), i);
			}
			else if (ks[0].startsWith("cmp")) {
				if (ks[1].matches("x[0-9]+")) {
					tempR.ensure(ks[1], i, instruction, spillRegister, spillAction, false);
				}
				if (ks[2].matches("x[0-9]+")) {
					tempR.ensure(ks[2], i, instruction, spillRegister, spillAction, false);
				}
				debugRegAlloc.setElementAt(tempR.clone(), i);
				if (ks[1].matches("x[0-9]+")) {
					tempR.free(ks[1], rxm.get(i + 1), persistentUse);
				}
				if (ks[2].matches("x[0-9]+")) {
					tempR.free(ks[2], rxm.get(i + 1), persistentUse);
				}
				regAlloc.setElementAt(tempR.clone(), i);
			}
			else if (ks[0].startsWith("add")
				|| ks[0].startsWith("sub")		
				|| ks[0].startsWith("mul")		
				|| ks[0].startsWith("div")) {
				if (ks[1].matches("x[0-9]+")) {
					tempR.ensure(ks[1], i, instruction, spillRegister, spillAction, true);
				}
				if (ks[2].matches("x[0-9]+")) {
					tempR.ensure(ks[2], i, instruction, spillRegister, spillAction, true);
				}
				debugRegAlloc.setElementAt(tempR.clone(), i);
				if (ks[1].matches("x[0-9]+")) {
					tempR.free(ks[1], rxm.get(i + 1), persistentUse);
				}
				if (ks[2].matches("x[0-9]+")) {
					tempR.free(ks[2], rxm.get(i + 1), persistentUse);
				}
				regAlloc.setElementAt(tempR.clone(), i);
			}
			else if ((ks[0].startsWith("pop") 
							|| ks[0].startsWith("push"))
							&& ks.length == 2) {
				/*Integer rid = Integer.parseInt(ks[1].substring(1));
				if (rid >= registerSize) {
					System.out.println("Ensuring " + ks[1]);
					tempR.ensure(ks[1], i, instruction, spillRegister, spillAction, false);
					debugRegAlloc.setElementAt(tempR.clone(), i);
					regAlloc.setElementAt(tempR.clone(), i);
				}*/
				if (ks[1].matches("x[0-9]+")) {
					tempR.ensure(ks[1], i, instruction, spillRegister, spillAction, false);
					//debugRegAlloc.setElementAt(tempR.clone(), i);
					//regAlloc.setElementAt(tempR.clone(), i);
					if (ks[0].equals("pop")) {
						persistentUse.add(ks[1]);
						//System.out.println("adding " + ks[1] + " as persistent at " + instruction.get(i) + " (" + i + ")");
					}
				
				}
				debugRegAlloc.setElementAt(tempR.clone(), i);
				regAlloc.setElementAt(tempR.clone(), i);
			}
			else {
				debugRegAlloc.setElementAt(tempR.clone(), i);
				regAlloc.setElementAt(tempR.clone(), i);
			}

			//System.out.println("after " + instruction.get(i) + " reg = " + tempR.dataVector.toString());
			//regAlloc.setElementAt(tempR.clone(), i);
		}
	}

	/*public int getUseDistance(int startPoint, String _target) {
		int found = -1;
		int iteration = startPoint + 1;
		while (found == -1 && iteration < instruction.size()) {
			//System.out.println("Searching at " + instruction.get(iteration));
			String[] ss = instruction.get(iteration++).split("\\s");
			if (ss[0].startsWith("add")
				|| ss[0].startsWith("sub")	
				|| ss[0].startsWith("mul")	
				|| ss[0].startsWith("div")) {
				if (ss[1].equals(_target)) {
					found = 1;
				}
			}
			else if (ss[0].startsWith("cmp")) {
				if (ss[1].equals(_target) || ss[2].equals(_target)) {
					found = 1;
				}
			}
			else if (ss[0].startsWith("move")) {
				if (ss[1].equals(_target)) {
					found = 1;
				}
			}
		}
		
		if (found == -1) {
			//System.out.println(";WARNING! Future use of " + _target + " not found");
			return -1;
		}
		return iteration - startPoint;
	}*/

	public void printOut(int verbosity) {
		int spacer = 20;
		int analysisSpace = 24; 
		int finalSpace = 20;
		int whitespaceCount;
		int registerCount;
		int finalCount;
		for (String h: spillRegister.values()) {
			System.out.println("    var " + h);
		}
		for (int i = 0; i < instruction.size(); i++) {
			if (verbosity == 0 && instruction.get(i).startsWith(";")) {
				continue;
			}
			System.out.print(";");
			whitespaceCount = spacer - instruction.get(i).length();
			System.out.print(instruction.get(i));
			for (int j = 0; j < whitespaceCount; j++) {
				System.out.print(" ");
			}
			registerCount = analysisSpace - rxm.get(i).toString().length();
			System.out.print(";" + bbIndex.get(i) + "/" + scopeIndex.get(i) + " ");
			System.out.print(rxm.get(i).toString());
			for (int j = 0; j < registerCount; j++) {
				System.out.print(" ");
			}/*
			System.out.print(distantUse.get(i).toString());
			finalCount = finalSpace - distantUse.get(i).toString().length();
			for (int j = 0; j < finalCount; j++) {
				System.out.print(" ");
			}*/
			//System.out.print(regAlloc.get(i).dataVector.toString());
			printRegister(i);
			System.out.println();
			if (spillAction.get(i) != null) {
				//System.out.println("   " + spillAction.get(i));
				String[] d = spillAction.get(i).split("\\s");
				System.out.println("    move " + d[1] + " " + d[2]);
			}
			rewriteCode(instruction.get(i), debugRegAlloc.get(i));
		}
	}

	public void rewriteCode(String instruction, tinyRegister reg) {
		String[] d = instruction.split("\\s");
		System.out.print("    ");
		if (d.length >= 2 && d[1].matches("x[0-9]+")) {
			d[1] = reg.getRegisterLocation(d[1]);
		}
		if (d.length == 3 && d[2].matches("x[0-9]+")) {
			d[2] = reg.getRegisterLocation(d[2]);
		}

		if (d.length == 1) {
			System.out.print(d[0]);
		}
		else if (d.length == 2) {
			System.out.print(d[0] + " " + d[1]);
		}
		else if (d.length == 3) {
			System.out.print(d[0] + " " + d[1] + " " + d[2]);
		}
		else {
			System.out.print(instruction);
		}
		System.out.println();
	}

	public void printRegister(int _index) {
		for (int j = 0; j < registerSize; j++) {
			if (debugRegAlloc.get(_index).dataVector.get(j) != null) {
				System.out.print(debugRegAlloc.get(_index).dataVector.get(j));
			}
			else {
				System.out.print("_");
			}
			if (debugRegAlloc.get(_index).boolVector.get(j)) {
				System.out.print("*");
			}
			System.out.print(" ");
		}
	}
}

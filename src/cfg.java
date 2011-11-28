import java.util.*;

class cfg {
	public List<String> instruction = new Vector<String>();
	public Vector<Vector<String>> predecessor = new Vector<Vector<String>>();
	public Vector<Vector<Integer>> pid = new Vector<Vector<Integer>>();
	public Vector<Vector<String>> successor = new Vector<Vector<String>>();
	public Vector<Vector<Integer>> sid = new Vector<Vector<Integer>>();
	public List<Integer> bbIndex = new Vector<Integer>();
	public List<Integer> scopeIndex = new Vector<Integer>();
	public List<Boolean> isLeader = new Vector<Boolean>();

	public cfg() {
	}

	public void loadInstruction(String _instruction) {
		instruction.add( _instruction);
		predecessor.add(new Vector<String>());
		pid.add(new Vector<Integer>());
		successor.add(new Vector<String>());
		sid.add(new Vector<Integer>());
		bbIndex.add(null);
		scopeIndex.add(null);
		isLeader.add(false);
	}

	public void printOut(int verbosity) {
		for (int i = 0; i < instruction.size(); i++) {
			// Basic Block index
			System.out.print(";> ");
			if (bbIndex.get(i) != null) {
				System.out.print(bbIndex.get(i) + "  ");
			}
			else {
				System.out.print("   ");
			}

			if (isLeader.get(i)) {
				System.out.print("{LEADER} ");
			}
			else {
		  		System.out.print("   ");
			}

			System.out.print(instruction.get(i));

			if (verbosity > 1) {
				System.out.print("   [SC: " + scopeIndex.get(i) + "]");
				//System.out.print(" | {PRED: ");
				System.out.print("\n;         | {PRED: ");
				for (int j = 0; j < predecessor.get(i).size(); j++) {
					System.out.print(predecessor.get(i).get(j) + "; ");
				}
				//System.out.print("}{SUCC: ");
				System.out.print("\n;         | {SUCC: ");
				for (int j = 0; j < successor.get(i).size(); j++) {
					System.out.print(successor.get(i).get(j) + "; ");
				}
				System.out.print("}");
			}
			System.out.println();
		}
	}

	public List<String> getIR() {
		return instruction;
	}

	public List<Integer> getBB() {
		return bbIndex;
	}

	public List<Integer> getSI() {
		return scopeIndex;
	}

	public void process() {
		identifyEdge();
		identifyLeader();
		assignBBIndex();
	}

	public void identifyEdge() {
		for (int i = 0; i < instruction.size(); i++) {
			if (!instruction.get(i).startsWith(";")
				&& !isUnconditional(instruction.get(i))
				&& !isBranch(instruction.get(i))
				&& !isContextSwitch(instruction.get(i))) {
				if (i < instruction.size() - 1) {
					successor.get(i).add(instruction.get(i+1));
					sid.get(i).add(i+1);

					predecessor.get(i+1).add(instruction.get(i));
					pid.get(i+1).add(i);
				}
			}
			else if (isUnconditional(instruction.get(i))
				|| isBranch(instruction.get(i))) {
				successor.get(i).add(instruction.get(traceLabel(readLabel(instruction.get(i)))));
				sid.get(i).add(traceLabel(readLabel(instruction.get(i))));

				predecessor.get(traceLabel(readLabel(instruction.get(i)))).add(instruction.get(i));
				pid.get(traceLabel(readLabel(instruction.get(i)))).add(i);

				if (isBranch(instruction.get(i)) && i < (instruction.size() - 1)) {
					successor.get(i).add(instruction.get(i+1));
					sid.get(i).add(i+1);

					predecessor.get(i+1).add(instruction.get(i));
					pid.get(i+1).add(i);
				}
			}
		}
	}

	public void identifyLeader() {
		for (int i = 0; i < instruction.size(); i++) {
			if (instruction.get(i).startsWith(";")) continue;
			if (pid.get(i).isEmpty()) {
				isLeader.set(i, true);
			}
			else {
				Vector<Integer> cPredecessor = pid.get(i);
				Vector<Integer> cSuccessor = sid.get(i);
				cPredecessor.remove((Object) (i-1));
				cSuccessor.remove((Object) (i+1));

				if (cPredecessor.size() != 0) {
					isLeader.set(i, true);
				}
				if (cSuccessor.size() != 0) {
					isLeader.set(i + 1, true);
				}
			}
		}
	}

	public void assignBBIndex() {
		int index = 0;
		int i = 0;
		int si = 0;
		String labelLatch = "nolabel";
		for (i = 0; i < instruction.size() - 1; i++) {
			bbIndex.set(i, index);
			if (isLeader.get(i+1)) {
				index++;
			}

			if (instruction.get(i).startsWith("LABEL")) {
				String[] div = instruction.get(i).split("\\s");
				if (div[1].startsWith("LABEL")) {
					// do nothing
				}
				else {
					if (!div[1].equals(labelLatch)) {
						labelLatch = div[1];
						si++;
					}
				}
			}

			scopeIndex.set(i, si);
		}
		bbIndex.set(i, index);
		scopeIndex.set(i, si);
	}

	public boolean isBranch(String _i) {
		if (_i.startsWith("GE")
			|| _i.startsWith("LE")
			|| _i.startsWith("EQ")
			|| _i.startsWith("NE")) {
			return true;
		}
		return false;
	}

	public boolean isUnconditional(String _i) {
		if (_i.startsWith("JUMP")
			|| _i.startsWith("JSR")) {
			return true;
		}
		return false;
	}

	public boolean isContextSwitch(String _i) {
		if (_i.startsWith("RET")) {
			return true;
		}
		return false;
	}

	public int traceLabel(String _label) {
		return instruction.indexOf("LABEL " + _label);
	}

	public String readLabel(String _i) {
		String[] t = _i.split("\\s");
		return t[t.length - 1];
	}
}

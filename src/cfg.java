import java.util.*;

class cfg {
	public List<String> instruction = new Vector<String>();
	public Vector<Vector<String>> predecessor = new Vector<Vector<String>>();
	public Vector<Vector<Integer>> pid = new Vector<Vector<Integer>>();
	public Vector<Vector<String>> successor = new Vector<Vector<String>>();
	public Vector<Vector<Integer>> sid = new Vector<Vector<Integer>>();
	public List<Integer> bbIndex = new Vector<Integer>();
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
		isLeader.add(false);
	}

	public void printOut() {
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
				System.out.print(" Y ");
			}
			else {
		  		System.out.print("   ");
			}

			System.out.print(instruction.get(i));

			//System.out.print(" | {PRED: ");
			System.out.print("\n         | {PRED: ");
			for (int j = 0; j < predecessor.get(i).size(); j++) {
				System.out.print(predecessor.get(i).get(j) + "; ");
			}
			//System.out.print("}{SUCC: ");
			System.out.print("\n         | {SUCC: ");
			for (int j = 0; j < successor.get(i).size(); j++) {
				System.out.print(successor.get(i).get(j) + "; ");
			}
			System.out.println("}");
		}
	}

	public void process() {
		identifyEdge();
	}

	public void identifyEdge() {
		for (int i = 0; i < instruction.size(); i++) {
			if (!isUnconditional(instruction.get(i))
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

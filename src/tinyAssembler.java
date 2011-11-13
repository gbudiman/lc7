import java.util.*;

class tinyAssembler {
	public List<String> instruction;
	public List<Integer> bbIndex;
	public Vector<Vector<String>> rxm;
	public Vector<String> rUsed;

	public tinyAssembler(List<String> _tiny, List<Integer> _bbIndex) {
		instruction = _tiny;
		bbIndex = _bbIndex;
		if (instruction.size() != bbIndex.size()) {
			System.out.println("WARNING: instruction : " 
					+ instruction.size()
					+ " bbIndex: "
					+ bbIndex.size());
		}
		rxm = new Vector<Vector<String>>();
		for (int i = 0; i < instruction.size(); i++) {
			rxm.add(new Vector<String>());
		}
	}

	public void process() {
		int indexLatch = -1;
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
					if (ks[1].startsWith("r")) rUsed.add(ks[1]);
					if (ks[2].startsWith("r")) rUsed.add(ks[2]);
				}
				else if (instruction.get(i).startsWith("add")
					|| instruction.get(i).startsWith("sub")
					|| instruction.get(i).startsWith("mul")
					|| instruction.get(i).startsWith("div")) {
					if (ks[2].startsWith("r")) rUsed.remove(ks[2]);
					if (ks[1].startsWith("r")) rUsed.add(ks[1]);
				}
				else if (instruction.get(i).startsWith("move")) {
					if (ks[2].startsWith("r")) rUsed.remove(ks[2]);
					if (ks[1].startsWith("r")) rUsed.add(ks[1]);
				}
				
			}
			Vector<String> vd = new Vector<String>();
			for (int ci = 0; ci < rUsed.size(); ci++) {
				vd.add(rUsed.get(ci));
			}
			rxm.setElementAt(vd, i);
		}
	}

	public void printOut(int verbosity) {
		int spacer = 40;
		int whitespaceCount;
		for (int i = 0; i < instruction.size(); i++) {
			if (verbosity == 0 && instruction.get(i).startsWith(";")) {
				continue;
			}
			whitespaceCount = spacer - instruction.get(i).length();
			System.out.print(instruction.get(i));
			for (int j = 0; j < whitespaceCount; j++) {
				System.out.print(" ");
			}
			System.out.print("; " + bbIndex.get(i) + " {");
			System.out.print(rxm.get(i).toString());
			System.out.println("} " + i);
		}
	}
}

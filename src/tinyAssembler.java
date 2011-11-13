import java.util.*;

class tinyAssembler {
	public List<String> instruction;
	public List<Integer> bbIndex;

	public tinyAssembler(List<String> _tiny, List<Integer> _bbIndex) {
		instruction = _tiny;
		bbIndex = _bbIndex;
		if (instruction.size() != bbIndex.size()) {
			System.out.println("WARNING: instruction : " 
					+ instruction.size()
					+ " bbIndex: "
					+ bbIndex.size());
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
			System.out.println("; " + bbIndex.get(i));
		}
	}
}

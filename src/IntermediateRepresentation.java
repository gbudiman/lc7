import java.util.*;

class IntermediateRepresentation {
	public int i = 0;
	public int j = 0;
	public int l = 0;
	public int p = 0;
	public int t = 0;
	public IntermediateRepresentation() {
	}

	public String generate() {
		return ("$T" + (i++));
	}
	
	public String generateLabel() {
		return ("LABEL" + (j++));
	}

	public String generateLabel(String l) {
		return ("LABEL " + l);
	}

	public String generateTemp() {
		return ("$T" + t++);
	}
	public void resetTemp() {
		t = 0;
	}

	public String generateReturn() {
		return "$R";
	}

	public String generateParameter() {
		return ("$P" + p++);
	}
	public void resetParameter() {
		p = 0;
	}

	public String generateLocal() {
		return ("$L" + l++);
	}
	public void resetLocal() {
		l = 0;
	}
	/*public String conditional(String left, String op, String right, String label, String type) {
		String condition = "";
		if (op.equals("<")) {
			condition += "LE";
		}
		else if (op.equals(">")) {
			condition += "GE";
		}
		else if (op.equals("!=")) {
			condition += "NE";
		}

		if (type.equals("INT")) {
			condition += " ";
		}
		else if (type.equals("FLOAT")) {
			condition += "F ";
		}

		return (condition + left + ' ' + right + ' ' + label);
	}*/

	public String arithmetic(String a, String b, String target, char op, String type) {
		String opcode = "";
		switch (op) {
			case '+': opcode += "ADD"; 	break;
			case '-': opcode += "SUB";	break;
			case '*': opcode += "MULT";	break;
			case '/': opcode += "DIV";	break;
			default: opcode += ";UNHANDLED";break;
		}

		if (type.equals("INT")) {
			opcode += "I";
		}
		else if (type.equals("FLOAT")) {
			opcode += "F";
		}

		return (opcode + ' ' + a + ' ' + b + ' ' + target);
	}

	public List<String> store(String a, String result, String type) {
		List<String> sl = new Vector<String>();
		if (a.startsWith("$P") && result.startsWith("$L")) {
			String storeOp = "UNIMPLEMENTED store!";
			String imd;
			String res;
			if (type.equals("INT")) storeOp = "STOREI";
			else if (type.equals("FLOAT")) storeOp = "STOREF";
			sl.add(storeOp + " " + a + " " + (imd = generateTemp()));
			sl.add(storeOp + " " + imd + " " + result);
			return sl;
		}

		if (type.equals("INT")) {
			sl.add("STOREI " + a + ' ' + result);
		}
		else if (type.equals("FLOAT")) {
			sl.add("STOREF " + a + ' ' + result);
		}
		else if (type.equals("STRING")) {
			sl.add("STORES " + a + ' ' + result);
		}
		else {
			sl.add("Unimplemented datatype at store: " + type);
		}

		return sl;
	}

	public String rw(String result, String action, String type) {
		if (type.equals("INT")) {
			return (action + "I " + result);
		}
		else if (type.equals("FLOAT")) {
			return (action + "F " + result);
		}
		else if (type.equals("STRING")) {
			return (action + "S " + result);
		}

		return "Unimplemented datatype at rw: " + type;
	}

	public String comparison(String a, String b, String op, String target, String type) {
		String opcode = "";

		// need to reverse operands if comparing register (comes first) with memory
		if (a.startsWith("$T") && b.startsWith("$P")) {
			String temp = a;
			a = b;
			b = temp;
			if (op.equals("<")) { op = ">"; }
			else if (op.equals(">")) { op = "<"; }
		}
		if (op.equals("<")) { opcode += "GEQ"; }
		else if (op.equals("<=")) { opcode += "GE"; }
		else if (op.equals(">")) { opcode += "LEQ"; }
		else if (op.equals(">=")) { opcode += "LE"; }
		else if (op.equals("!=")) { opcode += "EQ"; }
		else if (op.equals("=")) { opcode += "NE"; }

		if (type.equals("FLOAT")) {
			opcode += "F";
		}
		return (opcode + ' ' + a + ' ' + b + ' ' + target);
	}

	public String jump(String target) {
		return ("JUMP " + target);
	}

	public String label(String l) {
		return ("LABEL " + l);
	}

	public String jsr(String s) {
		return ("JSR " + s);
	}

	public String push() {
		return("PUSH");
	}

	public String push(String s) {
		return ("PUSH " + s);
	}

	public String pop() {
		return ("POP");
	}

	public String pop(String s) {
		return ("POP " + s);
	}
}

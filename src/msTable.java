import java.util.Vector;
import java.util.List;

class msTable {
	public List<mSymbol> symbolTable;
	public String scope;

	public msTable(String s) {
		scope = s;
	}

	public msTable(List<mSymbol> m, String s) {
		symbolTable = m;
		scope = s;
	}

	public void attachTable(List<mSymbol> ns) {
		symbolTable = ns;
	}

	public void listMember() {
		System.out.println(";Scope: " + this.scope);
		for (mSymbol symbolRow: symbolTable) {
			System.out.println(";-- " + symbolRow.getName() + ": " + symbolRow.getType() + " (" + symbolRow.getVarClass() + ") " + symbolRow.getID());
		}
	}

}

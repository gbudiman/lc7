import java.util.Vector;
import java.util.List;

class masterIR {
	public List<String> ir;
	public String scope;
	public int Space;
	public int parameter;

	public masterIR(String s) {
		scope = s;
		Space = 0;
	}

	public masterIR(List<String> _ir, String s) {
		ir = _ir;
		scope = s;
	}

	public void attachTable(List<String> _ir, int _Space, int _parameter) {
		ir = _ir;
		Space = _Space;
		parameter = _parameter;
	}

}

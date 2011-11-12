import java.util.*;

class tinyRegister {
	public Vector<String> dataVector = new Vector<String>();
	public Vector<Boolean> boolVector = new Vector<Boolean>();
	private int registerSize;

	public tinyRegister(int _size) {
		registerSize = _size;
		for (int i = 0; i < _size; i++) {
			dataVector.add(null);
			boolVector.add(false);
		}	
	}

	public int findInRegister(String _t) {
		return dataVector.indexOf(_t);
	}

	public boolean is_inRegister(String _t) {
		if (findInRegister(_t) != -1) {
			return true;
		}
		return false;
	}

	public boolean is_dirty(String _t) {
		int bvi;
		if ((bvi = findInRegister(_t)) != -1) {
			return boolVector.get(bvi);
		}
		System.out.println("WARNING! Searching " + _t + " yields no result in registers");
		return false;
	}

	public int findFree() {
		for (int i = 0; i < registerSize; i++) {
			if (dataVector.get(i) == null) {
				return i;
			}
		}
		return -1;
	}
}

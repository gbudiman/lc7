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

	public tinyRegister clone() {
		tinyRegister _clone = new tinyRegister(registerSize);
		//@SuppressWarnings("unchecked")
		_clone.dataVector = (Vector<String>) this.dataVector.clone();
		//@SuppressWarnings("unchecked")
		_clone.boolVector = (Vector<Boolean>) this.boolVector.clone();
		_clone.registerSize = (int) this.registerSize;

		return _clone;
	}

	private int findInRegister(String _t) {
		return dataVector.indexOf(_t);
	}

	private boolean is_inRegister(String _t) {
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

	public boolean ensure(String _load) {
		if (is_inRegister(_load)) {
			return true;
		}
		else {
			allocate(_load);
			return false;
		}
	}

	private boolean allocate(String _load) {
		int freeReg = -1;
		if ((freeReg = findFree()) != -1) {
			load(freeReg, _load);	
			return true;
		}
		return false;
	}

	private void load(int _target, String _load) {
		dataVector.setElementAt(_load, _target);
		boolVector.setElementAt(true, _target);
	}

	public void free(String _freeTarget, Vector<String> liveVariable) {
		/*for (int i = 0; i < dataVector.size(); i++) {
			if (dataVector.get(i) != null && liveVariable.indexOf(dataVector.get(i)) == -1) {
				System.out.print("Freeing " + dataVector.get(i) + " at " + liveVariable.toString() + " <- " + dataVector.toString());
				dataVector.setElementAt(null, i);
			}
		}*/
		if (liveVariable.indexOf(_freeTarget) == -1 && is_inRegister(_freeTarget)) {
			int removalTarget = dataVector.indexOf(_freeTarget);
			dataVector.setElementAt(null, removalTarget);
			boolVector.setElementAt(false, removalTarget);
		}
	}
}

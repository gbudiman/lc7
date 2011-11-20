import java.util.*;

class tinyRegister {
	public Vector<String> dataVector = new Vector<String>();
	public Vector<Boolean> boolVector = new Vector<Boolean>();
	private int registerSize;
	private int spillCounter;

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
		_clone.spillCounter = this.spillCounter;

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

	public String getRegisterLocation(String _s) {
		if (is_inRegister(_s)) {
			return "r" + findInRegister(_s);
		}
		return null;
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

	public String ensure(String _load, int startPoint, List<String> instruction, Map<String, String> spillRegister, Map<Integer, String> spillAction) {
		if (is_inRegister(_load)) {
			return null;
		}
		else if (findFree() != -1 && spillRegister.get(_load) != null) {
			String r = "r" + findFree();
			load(findFree(), _load);
			spillAction.put(startPoint, "load " + spillRegister.get(_load) + " " + r);
			return null;
		}
		else {
			String r = allocate(_load, startPoint, instruction, spillRegister);
			if (r != null) {
				spillAction.put(startPoint, "store " + r);
			}
			return null;
		}
	}

	private String allocate(String _load, int startPoint, List<String> instruction, Map<String, String> spillRegister) {
		int freeReg = -1;
		if ((freeReg = findFree()) != -1) {
			load(freeReg, _load);	
			return null;
		}
		else {
			String r = "r";
			String spillTarget;
			int farthestUsedRegister = findFarthest(startPoint, instruction);
			/*System.out.println(dataVector.get(farthestUsedRegister) 
												+ " => spill_" + spillCounter);*/
			spillRegister.put(dataVector.get(farthestUsedRegister)
											, (spillTarget = "spill_" + spillCounter++));
			dataVector.setElementAt(null, farthestUsedRegister);
			boolVector.setElementAt(false, farthestUsedRegister);
			r += findFree();
			load(findFree(), _load);
			return r + " " + spillTarget;
		}
	}

	private void load(int _target, String _load) {
		dataVector.setElementAt(_load, _target);
		boolVector.setElementAt(true, _target);
	}

	public void free(String _freeTarget, Vector<String> liveVariable) {
		if (liveVariable.indexOf(_freeTarget) == -1 && is_inRegister(_freeTarget)) {
			int removalTarget = dataVector.indexOf(_freeTarget);
			dataVector.setElementAt(null, removalTarget);
			boolVector.setElementAt(false, removalTarget);
		}
	}

	private int findFarthest(int startPoint, List<String> instruction) {
		int max = -1;
		int location = -1;

		for (int i = 0; i < dataVector.size(); i++) {
			for (int j = startPoint + 1; j < instruction.size(); j++) {
				String[] d = instruction.get(j).split("\\s");
				if (d.length >= 2 && d[1].equals(dataVector.get(i))) {
					if (j > max) {
						max = j;
						location = i;
						break;
					}
				}

				if (d.length >= 3 && d[2].equals(dataVector.get(i))) {
					if (j > max) {
						max = j;
						location = i;
						break;
					}
				}
			}
		}

		return location;
	}

}

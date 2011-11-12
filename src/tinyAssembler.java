import java.util.*;

class tinyAssembler {
	public List<String> insruction;
	public List<int> bbIndex;

	public tinyAssembler(List<String> _tiny, List<int> _bbIndex) {
		instruction = _tiny;
		bbIndex = _bbIndex;
	}
}

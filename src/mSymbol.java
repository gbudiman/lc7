class mSymbol {
	public String variableName;
	public String variableType;
	public String variableValue;
	public String parameterID;
	public String variableClass;

	public mSymbol() {
		variableName = "";
		variableType = "";
		variableValue = "";
	}

	public mSymbol(String vName, String vType) {
		variableName = vName;
		variableType = vType;
		variableValue = "";
	}

	public mSymbol(String vName, String vType, String vValue) {
		variableName = vName;
		variableType = vType;
		variableValue = vValue;
	}

	public mSymbol(String vName, String vType, String vID, String vClass) {
		variableName = vName;
		variableType = vType;
		variableValue = "";
		parameterID = vID;
		variableClass = vClass;
	}

	public String getName() { return variableName; }
	public String getType() { return variableType; }
	public String getValue() { return variableValue; }
	public String getID() { return parameterID; }
	public String getVarClass() { return variableClass; }
}

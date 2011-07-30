package de.odysseus.staxon.json;

class JsonXMLStreamScopeInfo {
	private String arrayName = null;
	private int arraySize = -1;

	void startArray(String arrayName) {
		if (this.arrayName != null) {
			throw new IllegalStateException("Cannot start array: " + arrayName);
		}
		this.arrayName = arrayName;
		this.arraySize = 0;
	}

	void incArraySize() {
		if (this.arrayName == null) {
			throw new IllegalStateException("Not in an array");
		}
		arraySize++;
	}

	String getArrayName() {
		return arrayName;
	}

	int getArraySize() {
		return arraySize;
	}

	void endArray() {
		if (this.arrayName == null) {
			throw new IllegalStateException("Cannot end array: " + arrayName);
		}
		this.arrayName = null;
		this.arraySize = -1;
	}
}

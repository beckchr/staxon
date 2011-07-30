package de.odysseus.staxon.json.io;

public enum JsonStreamToken {
	START_OBJECT,
	END_OBJECT,
	START_ARRAY,
	END_ARRAY,
	NAME,
	VALUE,
	NONE;
}

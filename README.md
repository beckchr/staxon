# StAXON - JSON via StAX

[StAXON](http://beckchr.github.com/staxon/) lets you read and write JSON using the Java Streaming API for XML (`javax.xml.stream`).

More specifically, StAXON provides implementations of the

- StAX Cursor API (`XMLStreamReader` and `XMLStreamWriter`)
- StAX Event API (`XMLEventReader` and `XMLEventWriter`)

for JSON.

The availability of a StAX implementation acts as a door opener for JSON to powerful XML related technologies like

- XSL transformations (XSLT)
- XML binding API (JAXB)
- XML Schema Definition (XSD)

## Main Features

- Supports [Jackson](http://jackson.codehaus.org/) and [Gson](http://code.google.com/p/google-gson/) as JSON streaming backends
- Full XML namespace support
- Support for JAXB and JAX-RS
- Start JSON arrays via XML processing instruction
- Memory efficient, even for very large documents
- It's pretty fast ([benchmark](https://github.com/beckchr/staxon/wiki/Benchmark))…

The XML-to-JSON [Mapping Convention](https://github.com/beckchr/staxon/wiki/Mapping-Convention) used by StAXON is
similar to the [Badgerfish](http://www.sklar.com/badgerfish/) convention but attempts to avoid needless text-only
JSON objects to generate a more compact JSON.

## Basic Usage

If you know StAX, you'll notice that there's little new: just obtain a reader or writer
from StAXON and you're ready to go.

### Writing JSON

Create a JSON-based writer:

	XMLStreamWriter writer = new JsonXMLOutputFactory().createXMLStreamWriter(System.out);

Write your document:

	writer.writeStartDocument();
	writer.writeStartElement("alice");
	writer.writeCharacters("charlie");
	writer.writeEndElement();
	writer.writeEndDocument();
	writer.close();

With an XML-based writer, this would have produced something like

	<?xml version="1.0"?>
	<alice>charlie</alice>

However, with our JSON-based writer, the output is

	{"alice":"charlie"}

### Reading JSON

Create a JSON-based reader:

	StringReader json = new StringReader("{\"alice\":\"charlie\"}");
	XMLStreamReader reader = new JsonXMLInputFactory().createXMLStreamReader(json);

Read your document:

	assert reader.getEventType() == XMLStreamConstants.START_DOCUMENT;
	reader.nextTag(); 
	assert reader.isStartElement() && "alice".equals(reader.getLocalName());
	reader.next();
	assert reader.hasText() && "charlie".equals(reader.getText());
	reader.nextTag();
	assert reader.isEndElement();
	reader.next();
	assert reader.getEventType() == XMLStreamConstants.END_DOCUMENT;
	reader.close();

## Documentation

[StAXON Wiki](https://github.com/beckchr/staxon/wiki/)

## Modules

StAXON consists of the following modules:

- `staxon` - core library, containing a default JSON streaming backend (required)
- `staxon-gson` - streaming backend for [Gson](http://code.google.com/p/google-gson/) JSON processor (optional)
- `staxon-jackson` - streaming backend for [Jackson](http://jackson.codehaus.org/) JSON processor (optional)
- `staxon-jaxrs` - JAX-RS support, produce/consume JSON from/to JAXB-annotated classes (optional)

You _must_ have the StAXON core library on your classpath. Additionally, you _may_ choose to use the Gson _or_ the
Jackson streaming backend. However, unless you want to avoid a dependency to an external JSON processor, it is
recommended to choose one of those as backend.

## Maven

Releases are synced to Maven Central. Add a dependency to StAXON in your POM file like this:

	<dependencies>
		<dependency>
			<groupId>de.odysseus.staxon</groupId>
			<artifactId>staxon</artifactId>
			<version>0.9.2</version>
		</dependency>

		<!-- or, to use the Jackson streaming backend -->
		<dependency>
			<groupId>de.odysseus.staxon</groupId>
			<artifactId>staxon-jackson</artifactId>
			<version>0.9.2</version>
		</dependency>

		<!-- or, to use the Gson streaming backend
		<dependency>
			<groupId>de.odysseus.staxon</groupId>
			<artifactId>staxon-gson</artifactId>
			<version>0.9.2</version>
		</dependency>
		-->		
	</dependencies>

## Downloads

Manually download the latest release: StAXON 0.9.2 (2011/11/18):

Core library:

- [staxon-0.9.2.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon/0.9.2/staxon-0.9.2.jar)
- [staxon-0.9.2-sources.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon/0.9.2//staxon-0.9.2-sources.jar)

Gson backend:

- [staxon-gson-0.9.2.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon-gson/0.9.2/staxon-gson-0.9.2.jar)
- [staxon-gson-0.9.2-sources.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon-gson/0.9.2/staxon-gson-0.9.2-sources.jar)

Jackson backend:

- [staxon-jackson-0.9.2.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon-jackson/0.9.2/staxon-jackson-0.9.2.jar)
- [staxon-jackson-0.9.2-sources.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon-jackson/0.9.2/staxon-jackson-0.9.2-sources.jar)

JAX-RS support:

- [staxon-jaxrs-0.9.2.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon-jaxrs/0.9.2/staxon-jaxrs-0.9.2.jar)
- [staxon-jaxrs-0.9.2-sources.jar](http://repo1.maven.org/maven2/de/odysseus/staxon/staxon-jaxrs/0.9.2/staxon-jaxrs-0.9.2-sources.jar)

## Development

[Github project](http://github.com/beckchr/staxon/)

## License

StAXON is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).


_(c) 2011 Odysseus Software_
# StAXON - JSON via StAX

[StAXON](http://beckchr.github.com/staxon/) lets you read and write JSON using the Java Streaming API for XML.

## Main Features

- Supports [Jackson](http://jackson.codehaus.org/) and [Gson](http://code.google.com/p/google-gson/) as JSON backends
- Full XML namespace support
- Start JSON arrays via XML processing instruction
- Memory efficient, even for very large documents
- It's pretty fast…

StAXON provides implementations of `javax.xml.stream.XMLStreamReader` and  `javax.xml.stream.XMLStreamWriter`.

The XML-to-JSON [Mapping Convention](https://github.com/beckchr/staxon/wiki/Mapping Convention) used by StAXON is
similar to the [Badgerfish](http://www.sklar.com/badgerfish/) convention but attempts to avoid needless text-only
JSON objects to generate a more compact JSON.

## Basic Usage

If you know StAX, you'll notice that there's little new: just obtain an `XMLStreamReader` or `XMLStreamWriter`
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

	<?xml version="1.0" ?>
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

## Download

You can get StAXON artifacts from our [maven repositories](http://beckchr.github.com/staxon/maven/) at github: 

	<repositories>
		<repository>
			<id>staxon</id>
			<url>http://beckchr.github.com/staxon/maven/releases</url>
		</repository>
	</repositories>

	<dependencies>
		<dependency>
			<groupId>de.odysseus.staxon</groupId>
			<artifactId>staxon</artifactId>
			<version>0.4.0</version>
		</dependency>
		<dependency>
			<groupId>org.codehaus.jackson</groupId>
			<artifactId>jackson-core-asl</artifactId>
			<version>1.8.4</version>
		</dependency>
	</dependencies>

Manually download the latest release: StAXON 0.4.0 (2011/08/10):

- [staxon-0.4.0.jar](http://beckchr.github.com/staxon/maven/releases/de/odysseus/staxon/staxon/0.4.0/staxon-0.4.0.jar)
- [staxon-0.4.0-sources.jar](http://beckchr.github.com/staxon/maven/releases/de/odysseus/staxon/staxon/0.4.0/staxon-0.4.0-sources.jar)

Make sure you have the `jackson-core` (and/or `gson`) jars on your classpath ([jackson download](http://wiki.fasterxml.com/JacksonDownload)).

## Development

[Github project](http://github.com/beckchr/staxon/)

## License

StAXON is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).


_(c) 2011 Odysseus Software_
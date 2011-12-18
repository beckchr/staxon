# StAXON - JSON via StAX

[StAXON](http://beckchr.github.com/staxon/) lets you read and write JSON using the Java Streaming API for XML (`javax.xml.stream`).

More specifically, StAXON provides implementations of the

- StAX Cursor API (`XMLStreamReader` and `XMLStreamWriter`)
- StAX Event API (`XMLEventReader` and `XMLEventWriter`)
- StAX Factory API (`XMLInputFactory` and `XMLOutputFactory`)

for JSON.

The availability of a StAX implementation acts as a door opener for JSON to powerful XML related technologies like

- XSL transformations (XSLT)
- XML binding API (JAXB)
- XML Schema Definition (XSD)

## Features

- Supports [Jackson](http://jackson.codehaus.org/) and [Gson](http://code.google.com/p/google-gson/) as JSON streaming backends
- Full XML namespace support
- Support for JAXB and JAX-RS
- Start JSON arrays via XML processing instruction
- Memory efficient, even for very large documents
- It's pretty fast ([benchmark](https://github.com/beckchr/staxon/wiki/Benchmark))…

The XML-to-JSON [Mapping Convention](https://github.com/beckchr/staxon/wiki/Mapping-Convention) used by StAXON is
similar to the [Badgerfish](http://www.sklar.com/badgerfish/) convention but attempts to avoid needless text-only
JSON objects to generate a more compact JSON.

## Documentation

Check the [Getting Started](https://github.com/beckchr/staxon/wiki/Getting-Started) guide first.
More documentation is available from the [StAXON Wiki](https://github.com/beckchr/staxon/wiki/).

## Downloads

The [Downloads](https://github.com/beckchr/staxon/wiki/Downloads) page provides Maven instructions and individual download links.

## Development

Visit the [Github project](http://github.com/beckchr/staxon/).

## License

StAXON is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).


_(c) 2011 Odysseus Software_
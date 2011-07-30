/**
 * <p>This package provides classes to read and write JSON via StAX.</p>
 * 
 * <p>The writer consumes processing instructions
 * <code>&lt;?xml-multiple element-name?&gt;</code> to properly insert JSON array tokens (<code>'['</code>
 * and <code>']'</code>). The client must provide this instruction through the
 * {@link javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(String, String)} method,
 * passing the (possibly prefixed) field name as data e.g.</p>
 * <pre>
 *   ...
 *   writer.writeProcessingInstruction("xml-multiple", "item");
 *   for (Item item : items) {
 *     writer.writeStartElement("item");
 *     ...
 *     writer.writeEndElement();
 *   }
 *   ...
 * </pre>
 * <p>Likewise, the reader produces the processing instruction as XML events.</p>
 * 
 * <p>The purpose of the used mapping convention is to generate a more compact <code>JSON</code>.
 * It borrows the <code>"$"</code> syntax for text elements from the BadgerFish convention but
 * attempts to avoid needless text-only <code>JSON</code> properties. The rules are:</p>
 * <ol>
 *   <li>
 *     <p>Element names become object properties.</p>
 *   </li>
 *   <li>
 *     <p>Text content of elements goes directly in the value of an object.</p>
 *     <pre><code>&lt;alice&gt;bob&lt;/alice&gt;</code></pre>
 *     <p>becomes</p>
 *     <pre><code>{ "alice": "bob" }</code></pre>
 *   </li>
 *   <li>
 *     <p>Nested elements become nested properties.</p>
 *     <pre><code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;david&gt;edgar&lt;/david&gt;&lt;/alice&gt;</code></pre>
 *     <p>becomes</p>
 *     <pre><code>{ "alice": { "bob": "charlie", "david": "edgar" } }</code></pre>
 *   </li>
 *   <li>
 *     <p>Multiple elements with the same name and at the same level become array elements.</p>
 *     <pre><code>&lt;alice&gt;&lt;bob&gt;charlie&lt;/bob&gt;&lt;bob&gt;david&lt;/bob&gt;&lt;/alice&gt;</code></pre>
 *     <p>becomes</p>
 *     <pre><code>{ "alice": { "bob": [ "charlie", "david" ] } }</code></pre>
 *   </li>
 *   <li>
 *     <p>Attributes go in properties whose name begin with <code>"@"</code>.</p>
 *     <pre><code>&lt;alice charlie="david"&gt;bob&lt;/alice&gt;</code></pre>
 *     <p>becomes</p>
 *     <pre><code>{ "alice": { "@charlie": "david", "$": "bob" } }</code></pre>
 *   </li>
 *   <li>
 *     <p>A default namespace declaration goes in the element's <code>"@xmlns"</code> property.
 *     <pre><code>&lt;alice xmlns="http://some-namespace"&gt;bob&lt;/alice&gt;</code></pre>
 *     <p>becomes</p>
 *     <pre><code>{ "alice": { "@xmlns": "http://some-namespace", "$": "bob" } }</code></pre>
 *   </li>
 *   <li>
 *     <p>A prefixed namespace declaration goes in the element's <code>"@xmlns:&lt;prefix&gt;"</code> property.
 *     <pre><code>&lt;alice xmlns:edgar="http://some-other-namespace"&gt;bob&lt;/alice&gt;</code></pre>
 *     <p>becomes</p>
 *     <pre><code>{ "alice": { "@xmlns:edgar": "http://some-other-namespace", "$": "bob" } }</code></pre>
 *   </li>
 * </ol>
 */
package de.odysseus.staxon.json;

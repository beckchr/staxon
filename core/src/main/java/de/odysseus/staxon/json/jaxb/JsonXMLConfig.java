package de.odysseus.staxon.json.jaxb;

/**
 * Json XML configuration insterface.
 */
public interface JsonXMLConfig {

	/**
	 * <p>Trigger arrays automatically?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public boolean isAutoArray();

	/**
	 * <p>Specify array paths. Paths may be absolute or relative (without
	 * leading <code>'/'</code>), where names are separated by <code>'/'</code>
	 * and may be prefixed. The root element is <em>not</em> included in a
	 * multiple path.</p>
	 * <p>E.g. for</p>
	 * <pre>
	 * {
	 *   "alice" : {
	 *     "bob" : [ "edgar", "charlie" ],
	 *     "peter" : null
	 *   }
	 * }
	 * </pre>
	 * <p>we would specify <code>"/bob"</code> or <code>"bob"</code> as
	 * multiple path.</p>
	 */
	public String[] getMultiplePaths();

	/**
	 * <p>Whether to write namespace declarations.</p>
	 * 
	 * <p>The default value is <code>true</code>.</p>
	 */
	public boolean isNamespaceDeclarations();

	/**
	 * <p>Namespace prefix separator.</p>
	 * 
	 * <p>The default value is <code>':'</code>.</p>
	 */
	public char getNamespaceSeparator();

	/**
	 * <p>Format output for better readability?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public boolean isPrettyPrint();

	/**
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property whether to treat
	 * the root as a "virtual" element, which will be removed from the stream
	 * when writing and added to the stream when reading. The root element
	 * name will be determined from an <code>@XmlRootElement</code> or
	 * <code>@XmlType</code> annotation.</p>
	 * 
	 * <p>The default value is <code>false</code> (i.e. no virtual root).</p>
	 */
	public boolean isVirtualRoot();
}
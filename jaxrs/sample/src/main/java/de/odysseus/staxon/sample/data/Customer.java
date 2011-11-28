package de.odysseus.staxon.sample.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.odysseus.staxon.json.jaxb.JsonXML;

@JsonXML(
		multiplePaths = { "/phone" }, // trigger JSON array for "phone" list
		virtualRoot = true,           // JSON will omit "customer" root
		prettyPrint = true)           // produce formatted JSON output
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer {
	@XmlElement(name = "first-name")
	public String firstName;

	@XmlElement(name = "last-name")
	public String lastName;

	@XmlElement
	public Address address;

	@XmlElement(name = "phone")
	public List<PhoneNumber> phoneNumbers;
}

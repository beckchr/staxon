package de.odysseus.staxon.sample.service;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.odysseus.staxon.sample.data.Address;
import de.odysseus.staxon.sample.data.Customer;
import de.odysseus.staxon.sample.data.PhoneNumber;

@Path("customer")
public class CustomerResource {
	private Customer newCustomer(
			String firstName, String lastName,
			String street,
			String... phoneNumbers) {
		Customer customer = new Customer();
		customer.firstName = firstName;
		customer.lastName = lastName;
		customer.address = new Address();
		customer.address.street = street;
		customer.phoneNumbers = new ArrayList<PhoneNumber>();
		for (String phoneNumber : phoneNumbers) {
			customer.phoneNumbers.add(new PhoneNumber());
			customer.phoneNumbers.get(customer.phoneNumbers.size() - 1).number = phoneNumber;
		}
		return customer;
	}
	
	@GET
	@Path("get")
	@Produces("application/json")
	public Customer getCustomer() {
		return newCustomer("David", "Lynch", "Mulholland Drive", "555-555-555");
	}

	@GET
	@Path("get/array")
	@Produces("application/json")
	public Customer[] getCustomerArray() {
		return new Customer[]{
			newCustomer("Jack", "London", "Piccadilly Circus"),
			newCustomer("John", "Lennon", "Abbey Road", "123-456-789", "987-654-321") };
	}
	
	@POST
	@Path("post")
	@Consumes("application/json")
	public Response postCustomer(Customer customer) {
		return Response
			.status(201)
			.entity("Received: " + customer.firstName + " " + customer.lastName)
			.build();
	}
	
	@POST
	@Path("post/array")
	@Consumes("application/json")
	public Response postCustomers(Customer[] customers) {
		return Response.status(201)
			.entity("Received: " + customers.length + " customers")
			.build();
	}
}
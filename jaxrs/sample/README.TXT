JAX-RS Example of using Jersey with StAXON
==========================================

To start the server:

	mvn jetty:run

Open a browser at the following URLs:

	http://localhost:9095/staxon-jaxrs-sample/restful/customer/get
	http://localhost:9095/staxon-jaxrs-sample/restful/customer/get/array

To stop the server:

	mvn jetty:stop

In addition, class CustomerResourceTest demonstrates consuming of POST requests. 

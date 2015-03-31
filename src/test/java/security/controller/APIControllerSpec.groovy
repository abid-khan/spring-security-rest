package security.controller

import groovyx.net.http.RESTClient

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.test.IntegrationTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.web.WebAppConfiguration

import security.SecurityApplication
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@WebAppConfiguration
@Stepwise
@IntegrationTest
class APIControllerSpec extends Specification {

	@Shared
	@AutoCleanup
	ConfigurableApplicationContext context


	@Shared
	@AutoCleanup
	def restClient

	@Shared
	@Value('${local.server.port}')
	int port;

	@Shared
	String authToken;

	def setupSpec(){

		Future future = Executors
				.newSingleThreadExecutor().submit(
				new Callable() {
					@Override
					public ConfigurableApplicationContext call() throws Exception {
						return (ConfigurableApplicationContext) SpringApplication
								.run(SecurityApplication.class)
					}
				})
		context = future.get(60, TimeUnit.SECONDS)


		if(null == restClient){
			restClient =  new RESTClient("http://localhost:8080")
			login(restClient)
		}
	}


	def cleanupSpec(){
		restClient = null;
		authToken=null;
	}


	def setup(){
		//TODO
	}

	def cleanup(){
		//TODO
	}


	def login(RESTClient restClient) {

		def loginResponse
		restClient.post(path: "/login", contentType: "application/x-www-form-urlencoded",
		body: [username:"abid", password:"abid"],
		requestContentType: "application/x-www-form-urlencoded"){ resp ->
			loginResponse = resp
			authToken = resp.headers.'X-AuthToken'
		}
	}

	def "Get user list"(){

		when: "get user "
		def response = restClient.get(path: "/api/users",headers : ["X-AuthToken": authToken],requestContentType: "application/json")

		then: "test user"
		response.status ==200;
	}
}

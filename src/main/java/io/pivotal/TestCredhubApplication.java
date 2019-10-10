package io.pivotal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
/**
 * This simple demo shows how to access values written by the credhub service broker securely into your app's environment. For reference the 
 * VCAP_SERVICES json document that we're operating on looks like:
 * 
 * {
 *   "credhub": [ 
 *     {"binding_name":null,"credentials":{"password":"aoeuaoeu"},"instance_name":"mycred","label":"credhub","name":"mycred","plan":"default","provider":null,"syslog_drain_url":null,"tags":["credhub"],"volume_mounts":[]},
 *     {"binding_name":null,"credentials":{"aoeu":"onetuhnotuh","password":"aoeuaoeu","username":"blah"},"instance_name":"mycredmulti","label":"credhub","name":"mycredmulti","plan":"default","provider":null,"syslog_drain_url":null,"tags":["credhub"],"volume_mounts":[]}
 *   ]
 * }
 * 
 */
public class TestCredhubApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(TestCredhubApplication.class, args);
	}

	@Autowired
	Environment env;


	@Override
	public void run(String... args) {
		System.out.println("starting run");
		ObjectMapper mapper = new ObjectMapper();

		try {
			while (true) {
				System.out.println("vcap: " + env.getProperty("VCAP_SERVICES"));

				// the VCAP_SERVICES environment variable is a JSON document holding info about this app's service dependencies
				JsonNode root = mapper.readTree(env.getProperty("VCAP_SERVICES"));

				// the secure credhub properties live under the "credhub" node of VCAP_SERVICES
				Iterator<JsonNode> credhubBindings = root.get("credhub").elements();

				// this map will hold the set of credentials that have been read out of credhub
				Map<String, String> creds = new HashMap<>();

				// since there can be more than one set of secure credentials bound to a single app, we have to iterate over all
				// of the "credhub" bindings and pull the desired properties out of each. if you know in advance that you only 
				// care about a binding named "blah", then you can simply check that 'binding.get("name").asText().equals("blah")'
				// here, we look at each binding and print the properties
				while (credhubBindings.hasNext()) {
					JsonNode binding = credhubBindings.next();

					// we created one binding called "mycred" and store one credential within
					if (binding.get("name").asText().equalsIgnoreCase("mycred")) {
						// this shows how you can pull out a credential by its name
						System.out.println("password for the mycred binding: " + binding.get("credentials").get("password"));
					}
					// we created another binding called "mycredmulti" with 3 credentials inside
					else if (binding.get("name").asText().equalsIgnoreCase("mycredmulti")) {
						// this shows how to pull out all of the credentials into a map that we can pass elsewhere
						Iterator<String> credsIter = binding.get("credentials").fieldNames();
						while (credsIter.hasNext()) {
							String credName = credsIter.next();
							creds.put(credName, binding.get("credentials").get(credName).asText());
						}
						
						System.out.println("credentials map for mycredmulti: " + creds);
					}
				}

				Thread.sleep(5000);
			}
		}
		catch (InterruptedException ie) {
			return;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}

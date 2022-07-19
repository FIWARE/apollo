package org.fiware.apollo.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * Configuration of general properties
 */
@ConfigurationProperties("general")
@Data
public class GeneralProperties {
	/**
	 * Which tenant should Apollo serve?
	 */
	private String tenant = null;
}

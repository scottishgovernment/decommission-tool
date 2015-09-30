package org.mygovscot.decommissioned.config;

import scot.mygov.authentication.client.CorsFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The Class CorsFilter.
 */
@Component
@Order(10)
public class DecommissionCorsFilter extends CorsFilter {

}
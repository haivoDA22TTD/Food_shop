/**
 * Client components for inter-service communication.
 * 
 * <p>This package contains HTTP clients that enable the Identity Service to communicate
 * with other microservices in the food shop application. These clients handle:
 * <ul>
 *   <li>HTTP request/response serialization</li>
 *   <li>Error handling and retry logic</li>
 *   <li>Service discovery and routing</li>
 * </ul>
 * 
 * <h2>Available Clients:</h2>
 * <ul>
 *   <li>{@link com.example.foodshop.identity.client.OrderServiceClient} - 
 *       Communicates with Order Service for shipper profile management</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.example.foodshop.identity.client;

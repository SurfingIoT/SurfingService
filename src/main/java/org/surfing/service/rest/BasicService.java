/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.surfing.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author vsenger
 */
@Path("/service")
public class BasicService {

    @GET
    @Produces("text/html")
    public String getMessage() {
        System.out.println("sayHello()");
        return "Hello, world!";
    }
}

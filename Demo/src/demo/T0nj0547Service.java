package demo;

import com.fasterxml.jackson.databind.JsonMappingException;

import demo.entity.T0nj0547;

import java.sql.SQLException;

import javax.naming.NamingException;

import javax.ws.rs.core.Response;


interface T0nj0547Service {
    Response addT0nj0547(String payload, String authString) ;

    T0nj0547 getT0nj0547ByBussRfnoRegoFc(String bussrfno, String regofc);
}

package it.polito.dp2.RNS.sol3.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import it.polito.dp2.RNS.sol3.jaxb.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/rns")
@Api(value="/rns")
public class RnsSystemResources {
	public UriInfo uriInfo;
	
	RnsSystemService service = new RnsSystemService();
	
	public RnsSystemResources(@Context UriInfo uriInfo){
		this.uriInfo = uriInfo;
	}
	
	@GET
	@Path("/places")
	@ApiOperation(value = "getPlaces", notes = "get all places in the system")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPlaces(){
		Places p =  service.getPlaces();
		if ( p == null)
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		else return Response.ok(p).build();
	}
	
	@GET
	@Path("/places/{id}")
	@ApiOperation(value = "getPlace", notes = "get place given the id")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPlace(@PathParam("id") String id){
		Place place = service.getPlace(id);
		if (service.getPlace(id) == null)
			return Response.status(Status.NOT_FOUND).build();
		else 	
			return Response.ok(place).build();
	}
	
	@GET
	@Path("/places/{id}/vehicles")
	@ApiOperation(value = "getVehiclesPerPlace", notes = "get vehicles in a given place")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getVehiclesPerPlace(@PathParam("id") String id){
		if (service.getPlace(id) == null){
			return Response.status(Status.NOT_FOUND).build();
		}
		Vehicles vehicles =  service.getVehiclesPerPlace(id); 
		if (vehicles == null)
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		return Response.ok(vehicles).build();
		
	}
	
	@GET
	@Path("/vehicles")
	@ApiOperation(value = "getVehicles", notes = "get all tracked vehicles in the system")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getVehicles(){
		Vehicles vehicles = service.getVehicles(); 
		if (vehicles == null)
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		else return Response.ok(vehicles).build();
		
	}
	
	@GET
	@Path("/vehicles/{id}")
	@ApiOperation(value = "getVehicle", notes = "get vehicle given the id")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getVehicle(@PathParam("id") String id){
		Vehicle vehicle = service.getVehicle(id); 
		if (vehicle == null)
			return Response.status(Status.NOT_FOUND).build();
		else return Response.ok(vehicle).build();
	}
	
	@POST
	@Path("/vehicles")
	@ApiOperation(value = "createNewVehicle", notes = "create a new vehicle that want to enter the system")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "CREATED"),
			@ApiResponse(code = 400, message = "BAD REQUEST"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR"),
	})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createNewVehicle(Vehicle v)
	{
		if ( v == null){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("WRONG_QUERY");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
		
		/* tutti i check sui places e sulla query non hanno bisogno di essere synchronized */
		
		String inGate = v.getComesFrom();
		String type = v.getType().toString();
		String destination = v.getGoesTo();
		String plateId = v.getId();
		if (inGate == null || type == null || plateId == null || destination == null){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("WRONG_QUERY");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
			
		if (service.getPlace(inGate) == null || service.getPlace(destination) == null){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("PLACE_ERROR");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
		if (!service.checkInGate(inGate)){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("NOT_IN");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
		if (!type.equals("CAR") && !type.equals("CARAVAN") && !type.equals("TRUCK") && !type.equals("SHUTTLE")){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("TYPE_ERROR");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
		if (!service.checkExistingPath(inGate,destination)){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("PATH_NOT_EXIST");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
		
		synchronized (service.getMap()) { //lock vehicles map
			
			if (service.getVehicle(plateId) != null){
				ResponseBody rb = new ResponseBody();
				rb.setMessage("ALREADY");
				return Response.status(Status.BAD_REQUEST).entity(rb).build();
			}
			
			Vehicle vehicle = v;
			UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(plateId); 
			URI self = builder.build();
			vehicle.setId(plateId);
			vehicle.setSelf(self.toString());
			vehicle.setComesFrom(inGate);
			vehicle.setGoesTo(destination);
			vehicle.setState(StateType.IN_TRANSIT);
			vehicle.setType(TypeType.fromValue(type));
			GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
			XMLGregorianCalendar xcal;
			try {
				xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			} catch (DatatypeConfigurationException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
			vehicle.setEntryTime(xcal);
			Vehicle created = service.createNewVehicle(vehicle);
			if (created != null){
				return Response.created(self).entity(created).build();
			}
			else{
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		
	}
	
	@DELETE
	@Path("/vehicles/{id}")
	@ApiOperation(value = "deleteVehicle", notes = "an admin that want to delete one single vehicle from the system")
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "NO CONTENT"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR"),
	})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteVehicle(@PathParam("id") String id){
		synchronized (service.getMap()) { //lock vehicles map
			Vehicle vehicle = service.getVehicle(id);
			if (vehicle == null){
				return Response.status(Status.NOT_FOUND).build();
			}
			if(service.deleteVehicle(id))
				return Response.noContent().build();
			else 
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PUT
	@Path("/vehicles/{id}")
	@ApiOperation(value = "putResponse", notes = "perform {typeOfrequest} operation on vehicle")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "CREATED"),
			@ApiResponse(code = 204, message = "NO CONTENT"),
			@ApiResponse(code = 400, message = "BAD REQUEST"),
			@ApiResponse(code = 404, message = "NOT FOUND"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR"),
	})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putResponse(@PathParam("id") String id, PutRequest pr){
		if ( pr == null){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("WRONG_QUERY");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
		if (pr.getTypeOfRequest() == null){
			ResponseBody rb = new ResponseBody();
			rb.setMessage("WRONG_QUERY");
			return Response.status(Status.BAD_REQUEST).entity(rb).build();
		}
		String typeOfRequest = pr.getTypeOfRequest();
		
		synchronized (service.getMap()) { //lock vehicles map
			
			Vehicle vehicle = service.getVehicle(id);
			if (vehicle == null){
				return Response.status(Status.NOT_FOUND).build();
			}
			else{
				
				//change state
				if (typeOfRequest.equals("CHANGE_STATE")){
					if (pr.getValue() == null){
						ResponseBody rb = new ResponseBody();
						rb.setMessage("WRONG_QUERY");
						return Response.status(Status.BAD_REQUEST).entity(rb).build();
					}
					String newState = pr.getValue();
					if (newState == null || (!newState.equals(StateType.IN_TRANSIT.toString()) && !newState.equals(StateType.PARKED.toString()))){
						return Response.status(Status.BAD_REQUEST).build();
					}
					if(service.updateState(vehicle, newState))
						return Response.noContent().build();
					else return Response.status(Status.INTERNAL_SERVER_ERROR).build();
				}
				
				//move
				else if (typeOfRequest.equals("MOVE")){
					if (pr.getValue() == null){
						ResponseBody rb = new ResponseBody();
						rb.setMessage("WRONG_QUERY");
						return Response.status(Status.BAD_REQUEST).entity(rb).build();
					}
					String newPosition = pr.getValue();
					if (service.getPlace(newPosition) == null)
						return Response.status(Status.BAD_REQUEST).entity("NOT_EXIST").build();
					if (!service.checkNextPlaces(vehicle.getIsInPlace(), newPosition))
						return Response.status(Status.BAD_REQUEST).entity("NOT_REACHABLE").build();
					Vehicle updated = service.updatePosition(vehicle, newPosition);
					if(updated == null)
						return Response.status(Status.INTERNAL_SERVER_ERROR).build();
					URI self;
					try {
						self = new URI(updated.getSelf());
						return Response.created(self).entity(updated).build();
					} catch (URISyntaxException e) {
						return Response.status(Status.INTERNAL_SERVER_ERROR).build();
					}
				}
				
				//exit
				else if(typeOfRequest.equals("EXIT")){
					
					if(pr.getValue() == null){
						ResponseBody rb = new ResponseBody();
						rb.setMessage("NOT_EXIST");
						return Response.status(Status.BAD_REQUEST).entity(rb).build();
					}
					String gateId = pr.getValue();	
					if (service.getPlace(gateId) == null){
						ResponseBody rb = new ResponseBody();
						rb.setMessage("NOT_EXIST");
						return Response.status(Status.BAD_REQUEST).entity(rb).build();
					}
					if (!service.checkOutGate(gateId)){
						ResponseBody rb = new ResponseBody();
						rb.setMessage("NOT_OUT");
						return Response.status(Status.BAD_REQUEST).entity(rb).build();
						
					}
					if (!vehicle.getIsInPlace().equals(gateId)){
						ResponseBody rb = new ResponseBody();
						rb.setMessage("NOT_GATE");
						return Response.status(Status.BAD_REQUEST).entity(rb).build();
					}
					if(service.deleteVehicle(id))
						return Response.noContent().build();
					else
						return Response.status(Status.INTERNAL_SERVER_ERROR).build();
				}
			}
		}
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
}
package it.polito.dp2.RNS.sol3.vehClient;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.VehClient;
import it.polito.dp2.RNS.lab3.WrongPlaceException;
import it.polito.dp2.RNS.sol3.jaxb.PutRequest;
import it.polito.dp2.RNS.sol3.jaxb.ResponseBody;
import it.polito.dp2.RNS.sol3.jaxb.StateType;
import it.polito.dp2.RNS.sol3.jaxb.TypeType;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;

public class VehicleClient implements VehClient {

	private WebTarget target;
	private Client client;
	private Vehicle current = new Vehicle();
	
	public VehicleClient(){
		client = ClientBuilder.newClient();
		if (System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
			target = client.target("http://localhost:8080/RnsSystem/rest");
		}
		else {
			target = client.target(System.getProperty("it.polito.dp2.RNS.lab3.URL"));
		}
	}
	
	@Override
	public List<String> enter(String plateId, VehicleType type, String inGate, String destination)
			throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
		// TODO Auto-generated method stub
		
		Vehicle vehicle = new Vehicle();
		vehicle.setComesFrom(inGate);
		vehicle.setGoesTo(destination);
		vehicle.setType(TypeType.fromValue(type.toString()));
		vehicle.setId(plateId);
		
		Response res = target.path("rns").path("vehicles")
				.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.post(Entity.entity(vehicle, MediaType.APPLICATION_XML));
		
		res.bufferEntity();
		if (res.getStatus() == 400){
			ResponseBody rb = res.readEntity(new GenericType<ResponseBody>(){});
			String message = rb.getMessage();
			if (message.equals("ALREADY"))
				throw new EntranceRefusedException();
			else if (message.equals("NOT_IN")) 
				throw new WrongPlaceException();
			else if (message.equals("PLACE_ERROR"))
				throw new UnknownPlaceException();
			else if (message.equals("TYPE_ERROR"))
				throw new EntranceRefusedException();
			else if (message.equals("WRONG_QUERY"))
				throw new EntranceRefusedException(); //controllare se va bene questa exception
			else if (message.equals("PATH_NOT_EXIST"))
				throw new EntranceRefusedException();
		}
		else if (res.getStatus() == 404)
			throw new EntranceRefusedException();
		else if (res.getStatus() == 500)
			throw new ServiceException();
		else if (res.getStatus() == 201){
			Vehicle v = res.readEntity(new GenericType<Vehicle>(){});
			current = v;
			return v.getSuggestedPath().getNode();
		}
		return null;
	}

	@Override
	public List<String> move(String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		// TODO Auto-generated method stub
		PutRequest pr = new PutRequest();
		pr.setTypeOfRequest("MOVE");
		pr.setValue(newPlace);
		
		Response res = target.path("rns").path("vehicles").path(current.getId())
				.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.put(Entity.entity(pr,MediaType.APPLICATION_XML)); 
		
		res.bufferEntity();
		if (res.getStatus() == 400){
			ResponseBody rb = res.readEntity(new GenericType<ResponseBody>(){});
			String message = rb.getMessage();
			if(message.equals("DESTINATION_ERROR"))
				throw new WrongPlaceException();
			else if (message.equals("NOT_REACHABLE"))
				throw new WrongPlaceException();
			else if (message.equals("NOT_EXIST"))
				throw new UnknownPlaceException();
			else if (message.equals("WRONG_QUERY"))
				throw new ServiceException();
		}
		else if (res.getStatus() == 500)
			throw new ServiceException();
		else if (res.getStatus() == 201){
			Vehicle v = res.readEntity(new GenericType<Vehicle>(){});
			current.setIsInPlace(v.getIsInPlace());
			if(v.getSuggestedPath() != null){
				current.setSuggestedPath(v.getSuggestedPath());
				return v.getSuggestedPath().getNode();
			}
			else return null;
		}
		return null;
		
	}

	@Override
	public void changeState(VehicleState newState) throws ServiceException {
		// TODO Auto-generated method stub
		PutRequest pr = new PutRequest();
		pr.setTypeOfRequest("CHANGE_STATE");
		pr.setValue(newState.toString());
		
		Response res = target.path("rns").path("vehicles").path(current.getId())
				.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.put(Entity.entity(pr, MediaType.APPLICATION_XML)); 
		res.bufferEntity();
		if (res.getStatus() == 500 || res.getStatus() == 404)
			throw new ServiceException();
		else 
			current.setState(StateType.fromValue(newState.toString()));
			return;

	}

	@Override
	public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		// TODO Auto-generated method stub
		PutRequest pr = new PutRequest();
		pr.setTypeOfRequest("EXIT");
		pr.setValue(outGate);
		Response res = target.path("rns").path("vehicles").path(current.getId())
				.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.put(Entity.entity(pr, MediaType.APPLICATION_XML)); 
		res.bufferEntity();
		if (res.getStatus() == 400){
			ResponseBody rb = res.readEntity(new GenericType<ResponseBody>(){});
			String message = rb.getMessage();
			if (message.equals("NOT_OUT") || message.equals("NOT_GATE"))
				throw new WrongPlaceException();
			if (message.equals("NOT_EXIST"))
				throw new UnknownPlaceException();
			if (message.equals("WRONG_QUERY"))
				throw new ServiceException();
		}
		else if (res.getStatus() == 500 || res.getStatus() == 404)
			throw new ServiceException();
	}

}

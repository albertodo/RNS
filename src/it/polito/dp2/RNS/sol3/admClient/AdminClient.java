package it.polito.dp2.RNS.sol3.admClient;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.AdmClient;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Places;
import it.polito.dp2.RNS.sol3.jaxb.ServiceType;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.Vehicles;

public class AdminClient implements AdmClient {

	private WebTarget target;
	private Client client;
	private Map<String, PlaceReader> placeMap = new HashMap<>();
	private List<MyGateReader> gateList = new LinkedList<>();
	private List<MyRoadSegmentReader> roadSegmentList = new LinkedList<>();
	private List<MyParkingAreaReader> parkingAreaList = new LinkedList<>();
	private List<MyConnectionReader> connectionList = new LinkedList<>();
	
	public AdminClient(){ 
		client = ClientBuilder.newClient();
		if (System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
			target = client.target("http://localhost:8080/RnsSystem/rest");
		}
		else {
			target = client.target(System.getProperty("it.polito.dp2.RNS.lab3.URL"));
		}
		
		WebTarget wt = target.path("rns").path("places");
		Response response = wt
				.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
				.get();
		
		if (response.getStatus() == 500){
			return;
		}
		response.bufferEntity();
		System.out.println(response.readEntity(String.class));
		Places places = response.readEntity(Places.class);
		
		// creo tutti i places e li metto nella mappa
		for (Place p : places.getPlace()){
			MyPlaceReader pr = new MyPlaceReader();
			pr.setCapacity(p.getCapacity());
			pr.setId(p.getId());
			placeMap.put(pr.getId(), pr);
		}
		//ora gestisco le connessioni di placeMap
		for (Place p : places.getPlace()){
			for (String s : p.getConnections()){
				placeMap.get(p.getId()).getNextPlaces().add(placeMap.get(s));
				MyConnectionReader c = new MyConnectionReader();
				c.setFrom(placeMap.get(p.getId()));
				c.setTo(placeMap.get(s));
				connectionList.add(c);
			}
		}
		
		/* *** */
		for (Place p : places.getPlace()){
			if (p.getGate() != null){
				// è un gate
				MyGateReader gate = new MyGateReader();
				gate.setCapacity(p.getCapacity());
				gate.setId(p.getId());
				gate.setType(GateType.fromValue(p.getGate().toString()));
				for(String s : p.getConnections())
					gate.getNextPlaces().add(placeMap.get(s));
				gateList.add(gate);
			}
			else if(p.getRoadSegment() != null){
				//è un roadSegment
				MyRoadSegmentReader roadSegment = new MyRoadSegmentReader();
				roadSegment.setCapacity(p.getCapacity().intValue());
				roadSegment.setId(p.getId());
				roadSegment.setName(p.getRoadSegment().getName());
				roadSegment.setRoadName(p.getRoadSegment().getRoad());
				for(String s : p.getConnections())
					roadSegment.getNextPlaces().add(placeMap.get(s));
				roadSegmentList.add(roadSegment);
			}
			else if (p.getParkingArea() != null){
				// è un parkingArea
				MyParkingAreaReader parkingArea = new MyParkingAreaReader();
				parkingArea.setId(p.getId());
				parkingArea.setCapacity(p.getCapacity().intValue());
				for (ServiceType s : p.getParkingArea().getService())
					parkingArea.getServices().add(s.getServiceName());
				for(String s : p.getConnections())
					parkingArea.getNextPlaces().add(placeMap.get(s));
				parkingAreaList.add(parkingArea);
			}
		}
		
		
		
	}
	
	@Override
	public Set<ConnectionReader> getConnections() {
		// TODO Auto-generated method stub
		return connectionList.stream().collect(Collectors.toSet());
	}

	@Override
	public Set<GateReader> getGates(GateType arg0) {
		// TODO Auto-generated method stub
		return gateList.stream().collect(Collectors.toSet());
	}

	@Override
	public Set<ParkingAreaReader> getParkingAreas(Set<String> arg0) {
		// TODO Auto-generated method stub
		return parkingAreaList.stream().collect(Collectors.toSet());
	}

	@Override
	public PlaceReader getPlace(String arg0) {
		return placeMap.get(arg0);
	}

	@Override
	public Set<PlaceReader> getPlaces(String arg0) {
		return placeMap.values().stream().collect(Collectors.toSet());
	}

	@Override
	public Set<RoadSegmentReader> getRoadSegments(String arg0) {
		// TODO Auto-generated method stub
		return roadSegmentList.stream().collect(Collectors.toSet());
	}

	@Override
	public VehicleReader getVehicle(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<VehicleReader> getVehicles(Calendar arg0, Set<VehicleType> arg1, VehicleState arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException {
		// TODO Auto-generated method stub
		if (place == null){
			//return all tracked vehicles
			Set<MyVehicleReader> set = new HashSet<>();
			WebTarget wt = target.path("rns").path("vehicles");
			Response response = wt
					.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
					.get();
			response.bufferEntity();
			// fare controlli sulla risposta *******************
			if (response.getStatus() == 500){
				//throw new ServiceException();
				return null;//*************************************TEST
			}
			
			System.out.println(response.readEntity(String.class));
			Vehicles vehicles = response.readEntity(new GenericType<Vehicles>(){});
			for (Vehicle v : vehicles.getVehicle()){
				MyVehicleReader tmp = new MyVehicleReader();
				tmp.setCalendar(v.getEntryTime().toGregorianCalendar());
				tmp.setDestination(placeMap.get(v.getGoesTo()));
				tmp.setOrigin(placeMap.get(v.getComesFrom()));
				tmp.setPosition(placeMap.get(v.getIsInPlace()));
				tmp.setId(v.getId());
				tmp.setState(VehicleState.fromValue(v.getState().toString()));
				tmp.setType(VehicleType.fromValue(v.getType().toString()));
				set.add(tmp);
			}
			return set.stream().collect(Collectors.toSet());
		}
		else {
			//return vehicles in that place
			Set<MyVehicleReader> set = new HashSet<>();
			WebTarget wt = target.path("rns").path("places").path(place).path("vehicles");
			Response response = wt
					.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
					.get();
			response.bufferEntity();
			// fare controlli sulla risposta *******************
			if (response.getStatus() == 500 || response.getStatus() == 404) //teoricamente dovrei lanciare not found exception ma non posso quindi boh
				throw new ServiceException();
			
			System.out.println(response.readEntity(String.class));
			Vehicles vehicles = response.readEntity(new GenericType<Vehicles>(){});
			for (Vehicle v : vehicles.getVehicle()){
				MyVehicleReader tmp = new MyVehicleReader();
				tmp.setCalendar(v.getEntryTime().toGregorianCalendar());
				tmp.setDestination(placeMap.get(v.getGoesTo()));
				tmp.setOrigin(placeMap.get(v.getComesFrom()));
				tmp.setPosition(placeMap.get(v.getIsInPlace()));
				tmp.setId(v.getId());
				tmp.setState(VehicleState.fromValue(v.getState().toString()));
				tmp.setType(VehicleType.fromValue(v.getType().toString()));
				set.add(tmp);
			}
			return set.stream().collect(Collectors.toSet());
		}
	}

	@Override
	public VehicleReader getUpdatedVehicle(String id) throws ServiceException {
		// TODO Auto-generated method stub
		WebTarget wt = target.path("rns").path("vehicles").path(id);
		Response response = wt
				.request(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
				.get();
		response.bufferEntity();
		if (response.getStatus() == 500){
			//throw new ServiceException();
			return null;//*************************************TEST
		}
		if (response.getStatus() == 404){
			return null;
		}
		
		//System.out.println(response.readEntity(String.class));
		Vehicle v = (Vehicle) response.readEntity(new GenericType<Vehicle>(){});
		MyVehicleReader tmp = new MyVehicleReader();
		tmp.setCalendar(v.getEntryTime().toGregorianCalendar());
		tmp.setDestination(placeMap.get(v.getGoesTo()));
		tmp.setOrigin(placeMap.get(v.getComesFrom()));
		tmp.setPosition(placeMap.get(v.getIsInPlace()));
		tmp.setId(v.getId());
		tmp.setState(VehicleState.fromValue(v.getState().toString()));
		tmp.setType(VehicleType.fromValue(v.getType().toString()));
		return tmp;
	}

}

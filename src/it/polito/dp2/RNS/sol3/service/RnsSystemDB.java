package it.polito.dp2.RNS.sol3.service;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RnsReaderException;
import it.polito.dp2.RNS.RnsReaderFactory;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.sol3.jaxb.GateType;
import it.polito.dp2.RNS.sol3.jaxb.ParkingAreaType;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.RoadSegmentType;
import it.polito.dp2.RNS.sol3.jaxb.ServiceType;
import it.polito.dp2.RNS.sol3.jaxb.StateType;
import it.polito.dp2.RNS.sol3.jaxb.SuggestedPath;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.neo4j.Paths;
import it.polito.dp2.RNS.sol3.jaxb.neo4j.Relationships;
import it.polito.dp2.RNS.sol3.jaxb.neo4j.Request;

public class RnsSystemDB {
	static private RnsSystemDB db = new RnsSystemDB();
	private List<Place> places = new LinkedList<>();
	private Map<String, URI> uriPlacesMap = new HashMap<>();
	private Map<URI,String> uriPlacesMapReverse = new HashMap<>();
	private RnsReader monitor;
	private Map<String, Vehicle> vehiclesMap = new ConcurrentHashMap<>(); 
	private WebTarget target;
	private Client client;
	
	//costruttore private per singletone il quale carica i dati dall' RnsReader
	private RnsSystemDB(){
		try {
			
			//prepare neo4j
			client = ClientBuilder.newClient();
			if (System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL") == null) {
				target = client.target("http://localhost:7474/db").path("data").path("node");
			}
			else {
				target = client.target(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL")).path("data").path("node");
			}
			
			RnsReaderFactory factory = RnsReaderFactory.newInstance();
			monitor = factory.newRnsReader();
			
			populateNeo4j(monitor);
			populatePlaces(monitor);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (RnsReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	
	public static RnsSystemDB getRnsSystemDB(){
		return db;
	}
	
	public void populateNeo4j(RnsReader monitor) throws MalformedURLException{
		
		
		/* ***********************take places and create nodes on neo4j********************** */
		Set<PlaceReader> places = monitor.getPlaces(null);
		
		for (PlaceReader it : places){
			it.polito.dp2.RNS.sol3.jaxb.neo4j.Node node = new it.polito.dp2.RNS.sol3.jaxb.neo4j.Node(); 
			node.setId(it.getId());
			
			javax.ws.rs.core.Response res = target.request(MediaType.APPLICATION_JSON)
								.post(Entity.entity(node, MediaType.APPLICATION_JSON));
			
			if (res.getStatus() == 201){
				uriPlacesMap.put(it.getId(), res.getLocation());
				uriPlacesMapReverse.put(res.getLocation(), it.getId());
			}
			else {
			}
		}
		
		/* ***********************take connections and create connections on neo4j********************** */
		for (ConnectionReader it : monitor.getConnections()){
			//connections.put(it.getFrom(), it.getTo()); 
			URI nodeb = uriPlacesMap.get(it.getTo().getId());
			URI urlFrom = uriPlacesMap.get(it.getFrom().getId());
			WebTarget from = client.target(urlFrom.toString()).path("relationships");
			
			it.polito.dp2.RNS.sol3.jaxb.neo4j.Connection connection = new it.polito.dp2.RNS.sol3.jaxb.neo4j.Connection();
			connection.setType("ConnectedTo");
			connection.setTo(nodeb.toString());
			
			javax.ws.rs.core.Response res = from.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(connection, MediaType.APPLICATION_JSON));
			
			if (res.getStatus() != 201){
				return;
			}
		}
	}
	
	public void populatePlaces(RnsReader monitor){
		//popolo places con parkingAreas
		Set<ParkingAreaReader> parkingAreas = monitor.getParkingAreas(null);
		for (ParkingAreaReader p :parkingAreas){
			Place place = new Place();
			place.setId(p.getId());
			place.setCapacity(BigInteger.valueOf(p.getCapacity()));
			ParkingAreaType pat = new ParkingAreaType();
			for (String s : p.getServices()){
				ServiceType service = new ServiceType();
				service.setServiceName(s);
				pat.getService().add(service);
			}
			place.setParkingArea(pat);
			List<String> conn = new LinkedList<>();
			for (PlaceReader pr : p.getNextPlaces()){
				conn.add(pr.getId());
			}
			place.getConnections().addAll(conn);
			places.add(place);
		}
		
		//popolo places con Gates
		Set<GateReader> gates = monitor.getGates(null);
		for (GateReader g : gates){
			Place place = new Place();
			place.setId(g.getId());
			place.setCapacity(BigInteger.valueOf(g.getCapacity()));
			List<String> conn = new LinkedList<>();
			for (PlaceReader pr : g.getNextPlaces()){
				conn.add(pr.getId());
			}
			place.getConnections().addAll(conn);	
			GateType type;
			type = GateType.fromValue(g.getType().value());
			place.setGate(type);
			places.add(place);
		}
		
		//popolo places con RoadSegments
		Set<RoadSegmentReader> roadSegments = monitor.getRoadSegments(null);
		for (RoadSegmentReader r : roadSegments){
			Place place = new Place();
			place.setId(r.getId());
			place.setCapacity(BigInteger.valueOf(r.getCapacity()));
			List<String> conn = new LinkedList<>();
			for (PlaceReader pr : r.getNextPlaces()){
				conn.add(pr.getId());
			}
			place.getConnections().addAll(conn);
			RoadSegmentType type = new RoadSegmentType();
			type.setName(r.getName());
			type.setRoad(r.getRoadName());
			place.setRoadSegment(type);
			places.add(place);
		}
	}
	
	public List<Place> getPlaces(){
		return places;
	}
	
	public Place getPlace(String id){
		for (Place p : places){
			if (p.getId().equals(id))
				return p;
		}
		return null;
	}
	
	public Vehicle getVehicle(String id){
		return vehiclesMap.get(id);
	}
	
	public synchronized List<Vehicle> getVehicles(){
		return vehiclesMap.values().stream()
				.collect(Collectors.toList());
	}
	
	public synchronized List<Vehicle> getVehiclesPerPlace(String id){
		return vehiclesMap.values().stream()
				.filter(p -> p.getIsInPlace().equals(id))
				.collect(Collectors.toList()); 
	}
	
	public boolean checkInGate(String id){
		for (Place p : places){
			if ( p.getId().equals(id) && p.getGate() != null && ( p.getGate().toString().equals("IN") || p.getGate().toString().equals("INOUT")))
				return true;
		}
		return false;
	}
	
	public boolean checkOutGate(String id){
		for (Place p : places){
			if ( p.getId().equals(id) && p.getGate() != null && ( p.getGate().toString().equals("OUT") || p.getGate().toString().equals("INOUT")))
				return true;
		}
		return false;
	}
	
	public synchronized Vehicle createNewVehicle(Vehicle vehicle) {
		
		Vehicle v = vehicle;
		String source = v.getComesFrom();
		String destination = v.getGoesTo();
		
		//calcolo shortest path
		try {
		URI uriFrom = uriPlacesMap.get(source);
		URI uriTo = uriPlacesMap.get(destination);
		WebTarget t = client.target(uriFrom.toString()).path("paths");
		Relationships rel = new Relationships();
		rel.setDirection("out");
		rel.setType("ConnectedTo");
		Request req = new Request();
		req.setTo(uriTo.toString());
		req.setMaxDepth(BigInteger.valueOf(places.size()));
		req.setRelationships(rel);
		req.setAlgorithm("shortestPath");
		
		javax.ws.rs.core.Response res = t.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(req, MediaType.APPLICATION_JSON));
		
		res.bufferEntity();

		if (res.getStatus() != 200){
			return null;
		}
		List<Paths> lp = res.readEntity(new GenericType<List<Paths>>(){});
		if (lp.isEmpty()){
			return null;
		}
		List<String> path = new LinkedList<>();
		for (Paths p : lp){
			for(String s : p.getNodes()){
				URI uri = new URI(s);
				path.add(uriPlacesMapReverse.get(uri));
			}
			break;
		}
		SuggestedPath sp = new SuggestedPath();
		sp.getNode().addAll(path);
		v.setSuggestedPath(sp);
		v.setIsInPlace(v.getComesFrom());
		}
		catch(URISyntaxException e){
			e.printStackTrace();
		}
		vehiclesMap.put(v.getId(), v);
		return v;
	}
	
	public synchronized boolean deleteVehicle(String id){
		vehiclesMap.remove(id);
		return true;
	}
	
	public synchronized boolean updateState(Vehicle v, String newState){ 
		vehiclesMap.get(v.getId()).setState(StateType.fromValue(newState));
		return true;

	}
	
	public synchronized Vehicle updatePosition(Vehicle v, String newPosition){

		Vehicle vehicle = new Vehicle();
		vehicle.setId(v.getId());
		vehicle.setComesFrom(v.getComesFrom());
		vehicle.setEntryTime(v.getEntryTime());
		vehicle.setGoesTo(v.getGoesTo());
		vehicle.setSelf(v.getSelf());
		vehicle.setState(v.getState());
		vehicle.setType(v.getType());
		String source = newPosition;
		String destination = v.getGoesTo();
		
		if (v.getSuggestedPath().getNode().contains(newPosition)){
			vehicle.setIsInPlace(newPosition);
			vehiclesMap.get(v.getId()).setIsInPlace(newPosition);
			return vehicle; //è una mossa lecita che non richiede calcolo shortestPath
		}
		
		else {
		//calcolo nuovo shortest path
			try {
				URI uriFrom = uriPlacesMap.get(source);
				URI uriTo = uriPlacesMap.get(destination);
				WebTarget t = client.target(uriFrom.toString()).path("paths");
				Relationships rel = new Relationships();
				rel.setDirection("out");
				rel.setType("ConnectedTo");
				Request req = new Request();
				req.setTo(uriTo.toString());
				req.setMaxDepth(BigInteger.valueOf(places.size()));
				req.setRelationships(rel);
				req.setAlgorithm("shortestPath");
				
				javax.ws.rs.core.Response res = t.request(MediaType.APPLICATION_JSON)
						.post(Entity.entity(req, MediaType.APPLICATION_JSON));
				
				res.bufferEntity();

				if (res.getStatus() != 200){
					return null;
				}
				List<Paths> lp = res.readEntity(new GenericType<List<Paths>>(){});
				List<String> path = new LinkedList<>();
				for (Paths p : lp){
					for(String s : p.getNodes()){
						URI uri = new URI(s);
						path.add(uriPlacesMapReverse.get(uri));
					}
					break;
				}
				SuggestedPath sp = new SuggestedPath();
				sp.getNode().addAll(path);
				vehicle.setSuggestedPath(sp);
				vehicle.setIsInPlace(newPosition);
				vehiclesMap.remove(v.getId());
				vehiclesMap.put(v.getId(), vehicle);
				return vehicle;
				}
				catch(URISyntaxException e){
					e.printStackTrace();
				}
			return null;
		}
	}
	
	public boolean checkNextPlaces(String src, String dst){
		Place place = new Place();
		for (Place p : places)
			if (p.getId().equals(src)){
				place = p;
				break;
			}
		for (String s : place.getConnections())
			if (s.equals(dst))
				return true;
		return false;
	}

	public boolean checkExistingPath(String src, String dst){
	
		URI uriFrom = uriPlacesMap.get(src);
		URI uriTo = uriPlacesMap.get(dst);
		WebTarget t = client.target(uriFrom.toString()).path("paths");
		Relationships rel = new Relationships();
		rel.setDirection("out");
		rel.setType("ConnectedTo");
		Request req = new Request();
		req.setTo(uriTo.toString());
		req.setMaxDepth(BigInteger.valueOf(places.size()));
		req.setRelationships(rel);
		req.setAlgorithm("shortestPath");
		
		javax.ws.rs.core.Response res = t.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(req, MediaType.APPLICATION_JSON));
		
		res.bufferEntity();
	
		if (res.getStatus() != 200){ 
			return false;
		}
		List<Paths> lp = res.readEntity(new GenericType<List<Paths>>(){});
		if (lp.isEmpty())
			return false;
		else return true;
		
	}
	
	public Map<String, Vehicle> getMap(){
		return vehiclesMap;
	}
}

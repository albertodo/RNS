package it.polito.dp2.RNS.sol3.service;

import java.util.List;
import java.util.Map;

import it.polito.dp2.RNS.sol3.jaxb.*;

public class RnsSystemService {
	
	private static RnsSystemDB db = RnsSystemDB.getRnsSystemDB();
	
	public Places getPlaces(){
		Places places = new Places();
		List<Place> list = places.getPlace();
		list.addAll(db.getPlaces());
		return places;
	}
	
	public Place getPlace(String id){
		return db.getPlace(id);
	}
	
	public Vehicles getVehicles(){
		Vehicles vehicles = new Vehicles();
		List<Vehicle> list = vehicles.getVehicle();
		list.addAll(db.getVehicles());
		return vehicles;
	}
	
	public Vehicles getVehiclesPerPlace(String id){
		Vehicles vehicles = new Vehicles();
		List<Vehicle> list = vehicles.getVehicle();
		list.addAll(db.getVehiclesPerPlace(id));
		return vehicles;
	}
	
	public Vehicle getVehicle(String id){
		return db.getVehicle(id);
	}
	
	public boolean checkInGate(String inGate){
		return db.checkInGate(inGate);
	}
	
	public boolean checkOutGate(String outGate){
		return db.checkOutGate(outGate);
	}
	
	public Vehicle createNewVehicle(Vehicle vehicle) {
		return db.createNewVehicle(vehicle);
	}
	
	public boolean deleteVehicle(String id){
		return db.deleteVehicle(id);
	}
	
	public boolean updateState(Vehicle v, String newState){
		return db.updateState(v, newState);
	}
	
	public Vehicle updatePosition(Vehicle v, String newPosition){
		return db.updatePosition(v, newPosition);
	}
	
	public boolean checkNextPlaces(String src, String dst){
		return db.checkNextPlaces(src,dst);
	}
	public boolean checkExistingPath(String src, String dst){
		return db.checkExistingPath(src,dst);
	}
	
	public Map<String, Vehicle> getMap(){
		return db.getMap();
	}

}

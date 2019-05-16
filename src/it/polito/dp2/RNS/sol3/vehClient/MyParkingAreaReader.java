package it.polito.dp2.RNS.sol3.vehClient;

import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;

public class MyParkingAreaReader implements ParkingAreaReader {

	int capacity;
	String id;
	Set<String> services = new HashSet<>();
	Set<PlaceReader> connections = new HashSet<>();
	
	@Override
	public int getCapacity() {
		// TODO Auto-generated method stub
		return capacity;
	}

	@Override
	public Set<PlaceReader> getNextPlaces() {
		// TODO Auto-generated method stub
		return connections;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public Set<String> getServices() {
		// TODO Auto-generated method stub
		return services;
	}
	
	public void setCapacity(int c){
		capacity = c;
	}
	
	public void setId(String s){
		id = s;
	}
	
	public void addConnection(PlaceReader p){
		connections.add(p);
	}
	
	
}
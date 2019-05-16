package it.polito.dp2.RNS.sol3.admClient;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.RNS.PlaceReader;

public class MyPlaceReader implements PlaceReader {

	String id;
	int capacity;
	Set<PlaceReader> connections = new HashSet<>();
	
	//Set<myPlaceReader> connections = new HashSet<>();
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

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
	
	public void setId(String s){
		id=s;
	}
	
	public void setCapacity(BigInteger c){
		capacity = c.intValue();
	}
	
	public void addConnection(PlaceReader p){
		connections.add(p);
	}

}
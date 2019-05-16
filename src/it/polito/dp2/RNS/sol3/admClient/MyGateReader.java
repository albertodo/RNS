package it.polito.dp2.RNS.sol3.admClient;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.PlaceReader;

public class MyGateReader implements GateReader {

	int capacity;
	String id;
	GateType type;
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
	public GateType getType() {
		// TODO Auto-generated method stub
		return type;
	}
	
	public void setId(String s){
		id = s;
	}
	
	public void setCapacity(BigInteger c){
		capacity = c.intValue();
	}
	
	public void setType(GateType g){
		type = g;
	}
	
	public void addConnection(PlaceReader p){
		connections.add(p);
	}

}
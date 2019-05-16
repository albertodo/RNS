package it.polito.dp2.RNS.sol3.vehClient;

import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;

public class MyRoadSegmentReader implements RoadSegmentReader {

	int capacity;
	String id, name, roadName;
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
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public String getRoadName() {
		// TODO Auto-generated method stub
		return roadName;
	}
	
	public void setCapacity(int c){
		capacity=c;
	}

	public void setId(String s){
		id = s;
	}
	
	public void setName(String s){
		name = s;
	}
	
	public void setRoadName(String s){
		roadName = s;
	}
	
	public void addConnection(PlaceReader p){
		connections.add(p);
	}
	
	
}
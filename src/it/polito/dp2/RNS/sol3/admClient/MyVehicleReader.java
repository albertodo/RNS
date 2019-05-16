package it.polito.dp2.RNS.sol3.admClient;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;

public class MyVehicleReader implements VehicleReader {

	String id;
	PlaceReader destination = new MyPlaceReader();
	PlaceReader origin = new MyPlaceReader();
	PlaceReader position = new MyPlaceReader();
	VehicleState state;
	VehicleType type;
	Calendar calendar = new GregorianCalendar();
	
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public PlaceReader getDestination() {
		// TODO Auto-generated method stub
		return destination;
	}

	@Override
	public Calendar getEntryTime() {
		// TODO Auto-generated method stub
		return calendar;
	}

	@Override
	public PlaceReader getOrigin() {
		// TODO Auto-generated method stub
		return origin;
	}

	@Override
	public PlaceReader getPosition() {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public VehicleState getState() {
		// TODO Auto-generated method stub
		return state;
	}

	@Override
	public VehicleType getType() {
		// TODO Auto-generated method stub
		return type;
	}
	
	public void setId(String s){
		id = s;
	}

	public void setOrigin(PlaceReader p){
		origin = p;
	}
	
	public void setDestination(PlaceReader p){
		destination = p;
	}
	
	public void setPosition(PlaceReader p){
		position = p;
	}
	
	public void setCalendar(Calendar c){
		calendar = c;
	}
	
	public void setType(VehicleType vt){
		type = vt;
	}
	
	public void setState(VehicleState vs){
		state = vs;
	}
}
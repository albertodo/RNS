package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.PlaceReader;

public class MyConnectionReader implements ConnectionReader {

	PlaceReader from = new MyPlaceReader();
	PlaceReader to = new MyPlaceReader();
	
	@Override
	public PlaceReader getFrom() {
		// TODO Auto-generated method stub
		return from;
	}

	@Override
	public PlaceReader getTo() {
		// TODO Auto-generated method stub
		return to;
	}
	
	public void setFrom(PlaceReader p){
		from = p;
	}
	
	public void setTo(PlaceReader p){
		to = p;
	}

}
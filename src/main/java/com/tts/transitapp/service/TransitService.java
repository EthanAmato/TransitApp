package com.tts.transitapp.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tts.transitapp.model.Bus;
import com.tts.transitapp.model.BusComparator;
import com.tts.transitapp.model.BusRequest;
import com.tts.transitapp.model.DistanceResponse;
import com.tts.transitapp.model.GeocodingResponse;
import com.tts.transitapp.model.Location;


@Service
public class TransitService {
	
	//Conversion factor that translates 1 meter into its equivalent in miles 
	double METERS_TO_MILES = 0.000621371;
	
	
	@Value("${transit_url}")
	public String transitUrl;
	
	@Value("${geocoding_url}")
    public String geocodingUrl;
	
    @Value("${distance_url}")
    public String distanceUrl;
	
    @Value("${google_api_key}")
    public String googleApiKey;
    
    
    private List<Bus> getBuses() { //returns a list of all buses on API
    	RestTemplate restTemplate = new RestTemplate(); 
    	Bus[] buses = restTemplate.getForObject(transitUrl, Bus[].class);
    	return Arrays.asList(buses);
    }
    
    private Location getCoordinates(String description){ //converts address + city -> coords
    	try {
			description = URLEncoder.encode(description, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println("error urlencoding");
			System.exit(1); //0 means exit successfully, anything other is an error
		} //url encoding
    	//turns any chars in description into something url can understand
    	//" " -> "+", unsafe chars replaced properly
    	String url = geocodingUrl + description + "GA&key="+googleApiKey; //construct url. Better alternative is to use url building module
    	RestTemplate restTemplate = new RestTemplate();
    	GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class); //store results of url into GeocodingResponse object
    	return response.results.get(0).geometry.location; //return the Json navigation equivalent of lat + long (location)
    } //return coords from a description of a location

    private double getDistance(Location origin, Location destination) {
    	String url = distanceUrl + "origins=" + origin.lat + "," +
    				 origin.lng + "&destinations=" + destination.lat + "," + 
    				 destination.lng + "&key=" + googleApiKey;
    	//construct api request asking google distance matrix api for dist btw 2 locations. Again, using url constructor is better practice
    	RestTemplate restTemplate = new RestTemplate();
    	DistanceResponse response = restTemplate.getForObject(url, DistanceResponse.class);
    	return response.rows.get(0).elements.get(0).distance.value * METERS_TO_MILES; 
    }
    
    public List<Bus> getNearbyBuses(BusRequest request) { //comprised of an address and city 
		List<Bus> allBuses = this.getBuses();
		Location personLocation = this.getCoordinates(request.address + " " + request.city); //translates this desc into coords
		List<Bus> nearbyBuses = new ArrayList<>();
		
		for(Bus bus: allBuses) {
			Location busLocation = new Location();
			busLocation.lat = bus.LATITUDE;
			busLocation.lng = bus.LONGITUDE;
			
			//
			double latDistance = Double.parseDouble(busLocation.lat) - Double.parseDouble(personLocation.lat);
			double lngDistance = Double.parseDouble(busLocation.lng) - Double.parseDouble(personLocation.lng);
			
			//This makes sure that we don't call getDistance (and subsequently use quotas from GoogleAPI)
			//on buses that are far away / a ton of buses at once
			if (Math.abs(latDistance) <= 0.02 && Math.abs(lngDistance) <= 0.02) { //if diff btw coords is < .02 lat,long
				double distance = getDistance(busLocation, personLocation); //calc the dist btw person and address in miles
				if (distance <= 1) { //if it's less than 1 mile
				    bus.distance = (double) Math.round(distance * 100) / 100; //save # of miles as property of bus
				    nearbyBuses.add(bus); //add to list of nearby buses
				}
			}
		}
		Collections.sort(nearbyBuses, new BusComparator());
		return nearbyBuses;    	
    }
    
    public Location getUserLocation(BusRequest request) {
		return this.getCoordinates(request.address + " " + request.city);
    }
    








}

package com.tts.transitapp.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.tts.transitapp.model.Bus;
import com.tts.transitapp.model.BusRequest;
import com.tts.transitapp.model.Location;
import com.tts.transitapp.service.TransitService;

@Controller
public class TransitController {
	@Autowired
	private TransitService transitservice;
	
	@GetMapping(path="/")
	public String redirectRoot() {
		return "redirect:/buses";
	}
	
	
	@GetMapping(path = "/buses")
    public String getBusesPage(Model model){
		BusRequest request = new BusRequest();
        model.addAttribute("request", request);
        return "index";
    }
	
    @PostMapping(path = "/buses")
    public String getNearbyBuses(BusRequest request, Model model) {
    	try {
    		List<Bus> buses = transitservice.getNearbyBuses(request);
    		Location userLocation = transitservice.getUserLocation(request);
    		model.addAttribute("buses", buses);
    		model.addAttribute("userLocation",userLocation);
    	} catch(Exception e) {
    		List<Bus> buses = new ArrayList();
    		model.addAttribute("buses", buses);
    	}
        model.addAttribute("request", request);    
        return "index";
    }
}

package com.safetynet.controller;

import com.safetynet.exception.NotFoundException;
import com.safetynet.model.*;
import com.safetynet.service.URLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@RestController
public class URLController {

    @Autowired
    private URLService service;

    @RequestMapping("/communityEmail")
    public TreeSet<String> getEmailsByCity(@RequestParam String city) throws NotFoundException {
        return service.getEmailsByCity(city);
    }

    @RequestMapping("/phoneAlert")
    public TreeSet<String> getPhonesByFireStation(@RequestParam String firestation) throws NotFoundException {
        return service.getPhonesByFireStation(firestation);
    }

    @RequestMapping("/personInfo")
    public InfosByPerson getInfosByPerson(@RequestParam String firstName, @RequestParam String lastName)
            throws NotFoundException {
        return service.getInfosByPerson(firstName, lastName);
    }

    @RequestMapping("/flood/stations")
    public ArrayList<HomesByFireStation> getHomesByFireStationsList(@RequestParam List<String> stations) throws NotFoundException {
        return service.getHomesByFireStationsList(stations);
    }

    @RequestMapping("/fire")
    public HomePersonsByAddress getHomePersonsByAddress(@RequestParam String address) throws NotFoundException {
        return service.getHomePersonsByAddress(address);
    }

    @RequestMapping("/firestation")
    public PersonsByFireStation getPersonsByFireStation(@RequestParam String stationNumber) throws NotFoundException {
        return service.getPersonsByFireStation(stationNumber);
    }

    @RequestMapping("/childAlert")
    public ChildrenByAddress getChildrenByAddress(@RequestParam String address) throws NotFoundException {
        return service.getChildrenByAddress(address);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleNotFoundException(NotFoundException notFoundException) {
        return notFoundException.getMessage();
    }

}

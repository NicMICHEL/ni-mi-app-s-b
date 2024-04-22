package com.safetynet.service;


import com.safetynet.exception.NotFoundException;
import com.safetynet.model.*;
import com.safetynet.repository.FireStationRepository;
import com.safetynet.repository.MedicalRecordRepository;
import com.safetynet.repository.PersonRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


@org.springframework.stereotype.Service
public class URLService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private FireStationRepository fireStationRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    private static final Logger logger = LogManager.getLogger(URLService.class);

    public TreeSet<String> getEmailsByCity(String city) throws NotFoundException {
        return personRepository.getEmailsByCity(city);
    }

    public TreeSet<String> getPhonesByFireStation(String firestation) throws NotFoundException {
        logger.debug("Getting phones corresponding to firestation : {}", firestation);
        logger.debug("Getting addresses corresponding to firestation : {}", firestation);
        TreeSet<String> addresses = fireStationRepository.getAddressesByFireStation(firestation);
        TreeSet<String> phones = new TreeSet<>();
        for (String address : addresses) {
            phones.addAll(personRepository.getPhonesByAddress(address));
        }
        logger.info("Phones corresponding to firestation : {} :", firestation);
        for (String phone : phones) {
            logger.info(phone);
        }
        return phones;
    }

    public InfosByPerson getInfosByPerson(String firstName, String lastName) throws NotFoundException {
        logger.debug("Getting Infos corresponding to firstName : {} and lastName : {}", firstName, lastName);
        InfosByPerson infosByPerson = new InfosByPerson(
                firstName,
                lastName,
                personRepository.findPersonByFirstNameLastName(firstName, lastName).address(),
                Integer.toString(medicalRecordRepository.getAgeByLastNameFirstName(firstName, lastName)),
                personRepository.findPersonByFirstNameLastName(firstName, lastName).email(),
                medicalRecordRepository.getMedicationsByLastNameFirstName(firstName, lastName),
                medicalRecordRepository.getAllergiesByLastNameFirstName(firstName, lastName));
        logger.info(infosByPerson.toString());
        return infosByPerson;
    }

    public ArrayList<HomesByFireStation> getHomesByFireStationsList(List<String> stations) throws NotFoundException {
        logger.debug("Getting Homes corresponding to firestations list : {}", stations);
        TreeSet<String> orderedStations = new TreeSet<>(stations);
        ArrayList<HomesByFireStation> homesByFireStationsList = new ArrayList<>();
        for (String station : orderedStations) {
            logger.debug("Getting Homes corresponding to firestation : {}", station);
            TreeSet<String> addresses = fireStationRepository.getAddressesByFireStation(station);
            List<HomeByAddress> homesByAddresses = new ArrayList<>();
            for (String address : addresses) {
                HomeByAddress homeByAddress = getHomeByAddress(address);
                homesByAddresses.add(homeByAddress);
            }
            HomesByFireStation homesByFireStation = new HomesByFireStation(station, homesByAddresses);
            homesByFireStationsList.add(homesByFireStation);
        }
        logger.info(homesByFireStationsList.toString());
        return homesByFireStationsList;
    }

    public HomeByAddress getHomeByAddress(String address) throws NotFoundException {
        logger.debug("Getting Home corresponding to address : {}", address);
        Map<String, Person> personsByAddress = personRepository.getPersonsByAddress(address);
        List<HomePerson> homePersons = getHomePerson(personsByAddress);
        HomeByAddress homeByAddress = new HomeByAddress(address, homePersons);
        return homeByAddress;
    }

    public HomePersonsByAddress getHomePersonsByAddress(String address) throws NotFoundException {
        logger.debug("Getting Home Persons corresponding to address : {}", address);
        HomePersonsByAddress homePersonsByAddress = new HomePersonsByAddress(address,
                fireStationRepository.findByAddress(address).station(),
                getHomePerson(personRepository.getPersonsByAddress(address)));
        logger.info(homePersonsByAddress.toString());
        return homePersonsByAddress;
    }

    public List<HomePerson> getHomePerson(Map<String, Person> personsByAddress) throws NotFoundException {
        logger.debug("Getting HomePersons from Map<String, Person> personsByAddress : {}", personsByAddress);
        List<HomePerson> homePersons = new ArrayList<>();
        for (Map.Entry<String, Person> entry : personsByAddress.entrySet()) {
            String firstName = entry.getValue().firstName();
            String lastName = entry.getValue().lastName();
            HomePerson homePerson = new HomePerson(
                    firstName,
                    lastName,
                    entry.getValue().phone(),
                    Integer.toString(medicalRecordRepository
                            .getAgeByLastNameFirstName(firstName, lastName)),
                    medicalRecordRepository
                            .getMedicationsByLastNameFirstName(firstName, lastName),
                    medicalRecordRepository
                            .getAllergiesByLastNameFirstName(firstName, lastName));
            homePersons.add(homePerson);
        }
        return homePersons;
    }

    public PersonsByFireStation getPersonsByFireStation(String stationNumber) throws NotFoundException {
        logger.debug("Getting Persons corresponding to firestation : {}", stationNumber);
        TreeSet<String> addresses = fireStationRepository.getAddressesByFireStation(stationNumber);
        List<LastNameFirstNamePhoneByAddress> lastNameFirstNamePhoneByAddresses = new ArrayList<>();
        int childrenCount = 0;
        int adultsCount = 0;
        for (String address : addresses) {
            Map<String, Person> personsByAddress = personRepository.getPersonsByAddress(address);
            List<LastNameFirstNamePhone> lastNameFirstNamePhones = new ArrayList<>();
            for (Map.Entry<String, Person> entry : personsByAddress.entrySet()) {
                String lastName = entry.getValue().lastName();
                String firstName = entry.getValue().firstName();
                LastNameFirstNamePhone lastNameFirstNamePhone =
                        new LastNameFirstNamePhone(lastName, firstName, entry.getValue().phone());
                int age = medicalRecordRepository
                        .getAgeByLastNameFirstName(firstName, lastName);
                if (age <= 18) childrenCount += 1;
                else adultsCount += 1;
                lastNameFirstNamePhones.add(lastNameFirstNamePhone);
            }
            LastNameFirstNamePhoneByAddress lastNameFirstNamePhoneByAddress =
                    new LastNameFirstNamePhoneByAddress(address, lastNameFirstNamePhones);
            lastNameFirstNamePhoneByAddresses.add(lastNameFirstNamePhoneByAddress);
        }
        PersonsByFireStation personsByFireStation = new PersonsByFireStation(
                stationNumber,
                Integer.toString(childrenCount),
                Integer.toString(adultsCount),
                lastNameFirstNamePhoneByAddresses);
        logger.info(personsByFireStation.toString());
        return personsByFireStation;
    }

    public ChildrenByAddress getChildrenByAddress(String address) throws NotFoundException {
        logger.debug("Getting Children corresponding to address : {}", address);
        Map<String, Person> personsByAddress = personRepository.getPersonsByAddress(address);
        List<Child> children = new ArrayList<>();
        List<LastNameFirstName> adults = new ArrayList<>();
        for (Map.Entry<String, Person> entry : personsByAddress.entrySet()) {
            String firstName = entry.getValue().firstName();
            String lastName = entry.getValue().lastName();
            int age = medicalRecordRepository
                    .getAgeByLastNameFirstName(firstName, lastName);
            if (age <= 18) {
                Child child = new Child(lastName, firstName, Integer.toString(age));
                children.add(child);
            } else {
                LastNameFirstName lastNameFirstName = new LastNameFirstName(lastName, firstName);
                adults.add(lastNameFirstName);
            }
        }
        ChildrenByAddress childrenByAddress = new ChildrenByAddress(address, children, adults);
        logger.info(childrenByAddress.toString());
        return childrenByAddress;
    }

}

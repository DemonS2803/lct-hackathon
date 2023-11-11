package com.example.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dto.Way;
import com.example.entities.FromToDTO;
import com.example.entities.Task;
import com.example.entities.User;
import com.example.repositories.FromToDTORepository;

import jakarta.annotation.PostConstruct;

@Service
public class MapsService {

    @Autowired
    private FromToDTORepository cacheRepository;

    private Map<Way, Double> waysCache;
    private HashSet<FromToDTO> waysRequestSet;

    @PostConstruct
    public void initMapsService() {
        waysCache = new HashMap<>();
        ArrayList<FromToDTO> dbCache = (ArrayList<FromToDTO>) cacheRepository.findAll();
        for (var fromTo: dbCache) {
            waysCache.put(new Way(fromTo.getFromPoint(), fromTo.getToPoint()), fromTo.getMinutes() / 60);
        }

        waysRequestSet = new HashSet<>();
    }

    public void fillWaysRequest(ArrayList<Task> tasks, ArrayList<User> workers) {
        waysRequestSet = new HashSet<>();
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = i + 1; j < tasks.size(); j++) {
                if (waysCache.containsKey(new Way(tasks.get(i).getAddress(), tasks.get(j).getAddress())) ||
                    waysCache.containsKey(new Way(tasks.get(j).getAddress(), tasks.get(i).getAddress())) || 
                    tasks.get(i).getAddress().equals(tasks.get(j).getAddress())) {
                    continue;
                }
                waysRequestSet.add(new FromToDTO(tasks.get(i).getAddress(), tasks.get(j).getAddress()));
            }
        }
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < workers.size(); j++) {
                if (waysCache.containsKey(new Way(tasks.get(i).getAddress(), workers.get(j).getAddress())) ||
                    waysCache.containsKey(new Way(workers.get(j).getAddress(), tasks.get(i).getAddress())) || 
                    tasks.get(i).getAddress().equals(workers.get(j).getAddress())) {
                    continue;
                }
                waysRequestSet.add(new FromToDTO(tasks.get(i).getAddress(), workers.get(j).getAddress()));
            }
        }
    }

    public HashSet<FromToDTO> getWaysRequestSet() {
        return waysRequestSet;
    }

    public Map<Way, Double> getCache() {
        return waysCache;
    }

    public void fillCache(ArrayList<FromToDTO> gotWays) {
        for (var gotWay: gotWays) {
            if (waysCache.containsKey(new Way(gotWay.getFromPoint(), gotWay.getToPoint())) || 
                waysCache.containsKey(new Way(gotWay.getToPoint(), gotWay.getFromPoint()))) {
                continue;
            }
            waysCache.put(new Way(gotWay.getFromPoint(), gotWay.getToPoint()), gotWay.getMinutes() / 60d);
        }
        cacheRepository.saveAll(gotWays);

        waysRequestSet.clear();
    }

    public Double getRoadTime(String from, String to) {
        if (from.equals(to)) {
            return 0d;
        }
        if (waysCache.containsKey(new Way(from, to))) {
            return waysCache.get(new Way(from, to));
        }
        return waysCache.getOrDefault(new Way(to, from), Double.MAX_VALUE);
    }
    
}

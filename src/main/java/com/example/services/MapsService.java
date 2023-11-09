package com.example.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dto.FromToDTO;
import com.example.dto.Way;
import com.example.entities.Task;
import com.example.entities.User;

@Service
public class MapsService {


    private Map<Way, Double> waysCache;
    private HashSet<FromToDTO> waysRequestSet;

    public void fillWaysCashe(ArrayList<Task> tasks, ArrayList<User> workers) {
        waysRequestSet = new HashSet<>();
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = i + 1; j < tasks.size(); j++) {
                if (waysRequestSet.contains(new FromToDTO(tasks.get(i).getAddress(), tasks.get(j).getAddress())) ||
                    waysRequestSet.contains(new FromToDTO(tasks.get(j).getAddress(), tasks.get(i).getAddress()))) {
                    continue;
                }
                waysRequestSet.add(new FromToDTO(tasks.get(i).getAddress(), tasks.get(j).getAddress()));
            }
        }
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = i + 1; j < workers.size(); j++) {
                if (waysRequestSet.contains(new FromToDTO(tasks.get(i).getAddress(), workers.get(j).getAddress())) ||
                    waysRequestSet.contains(new FromToDTO(workers.get(j).getAddress(), tasks.get(i).getAddress()))) {
                    continue;
                }
                waysRequestSet.add(new FromToDTO(tasks.get(i).getAddress(), workers.get(j).getAddress()));
            }
        }
    }

    public HashSet<FromToDTO> getWaysRequestSet() {
        return waysRequestSet;
    }

    public void fillCache(ArrayList<FromToDTO> gotWays) {
        for (var gotWay: gotWays) {
            if (waysCache.containsKey(new Way(gotWay.getFrom(), gotWay.getTo())) || 
                waysCache.containsKey(new Way(gotWay.getTo(), gotWay.getFrom()))) {
                continue;
            }
            waysCache.put(new Way(gotWay.getFrom(), gotWay.getTo()), gotWay.getMinutes());
        }
    }

    public Double gotTime(String from, String to) {
        if (waysCache.containsKey(new Way(from, to))) {
            return waysCache.get(new Way(from, to));
        }
        return waysCache.getOrDefault(new Way(to, from), Double.MAX_VALUE);
    }
    
}

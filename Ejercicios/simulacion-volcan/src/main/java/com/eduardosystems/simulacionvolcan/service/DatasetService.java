package com.eduardosystems.simulacionvolcan.service;

import com.eduardosystems.simulacionvolcan.entity.DatasetPoblacion;
import com.eduardosystems.simulacionvolcan.repository.DatasetPoblacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetService implements CommandLineRunner {

    @Autowired
    private DatasetPoblacionRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    new DatasetPoblacion(85, 120, 'C'),
                    new DatasetPoblacion(85, 125, 'C'),
                    new DatasetPoblacion(86, 122, 'T'),
                    new DatasetPoblacion(90, 100, 'E'),
                    new DatasetPoblacion(95, 110, 'P'),
                    new DatasetPoblacion(96, 112, 'A'),
                    new DatasetPoblacion(100, 80, 'F')
            ));
        }
    }

    public List<DatasetPoblacion> obtenerDataset() {
        return repository.findAll();
    }
}
package com.mangaon.recycleers.service;

import com.mangaon.recycleers.model.Client;
import com.mangaon.recycleers.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public Client updateClient(Long id, Client details) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        client.setClientName(details.getClientName());
        client.setIndividualName(details.getIndividualName());
        client.setAddress(details.getAddress());
        client.setGstNo(details.getGstNo());
        client.setGstApplicable(details.getGstApplicable());   // ✅ FIXED
        client.setClientType(details.getClientType());
        client.setEntityStatus(details.getEntityStatus());     // ✅ FIXED
        client.setMobileNo(details.getMobileNo());
        client.setEmail(details.getEmail());

        return clientRepository.save(client);
    }

    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
        clientRepository.delete(client);
    }

    public List<Client> searchClientsByName(String name) {
        return clientRepository.findByClientNameContainingIgnoreCase(name);
    }

    public long getClientCount() {
        return clientRepository.count();
    }
}
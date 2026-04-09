package com.mangaon.recycleers.service;

import com.mangaon.recycleers.model.Supplier;
import com.mangaon.recycleers.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));

        supplier.setSupplierName(supplierDetails.getSupplierName());
        supplier.setIndividualName(supplierDetails.getIndividualName());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setGstNo(supplierDetails.getGstNo());
        supplier.setGstApplicable(supplierDetails.getGstApplicable());   // ✅ FIXED
        supplier.setVendorType(supplierDetails.getVendorType());
        supplier.setEntityStatus(supplierDetails.getEntityStatus());     // ✅ FIXED
        supplier.setMobileNo(supplierDetails.getMobileNo());
        supplier.setEmail(supplierDetails.getEmail());

        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        supplierRepository.delete(supplier);
    }

    public List<Supplier> searchSuppliersByName(String name) {
        return supplierRepository.findBySupplierNameContainingIgnoreCase(name);
    }

    public long getSupplierCount() {
        return supplierRepository.count();
    }
}
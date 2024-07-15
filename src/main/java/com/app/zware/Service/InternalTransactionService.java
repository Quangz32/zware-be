package com.app.zware.Service;

import com.app.zware.Entities.InternalTransaction;
import com.app.zware.Repositories.InternalTransactionRepository;
import java.util.List;

import com.app.zware.Validation.InternalTransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InternalTransactionService {
  @Autowired
  InternalTransactionRepository internalTransactionRepository;

  public List<InternalTransaction> getAll(){
    return internalTransactionRepository.findAll();
  }

  public InternalTransaction save(InternalTransaction transaction){
    return internalTransactionRepository.save(transaction);
  }

  public InternalTransaction findById(Integer id){
    return internalTransactionRepository.findById(id).orElse(null);
  }

  public boolean existByTransactionId(Integer id){
    return internalTransactionRepository.existByIdAndIsDeletedFalse(id);
  }

    public List<InternalTransaction> getByWarehouse(Integer warehouseId) {
      return internalTransactionRepository.findByWarehouse(warehouseId);
    }

  public List<InternalTransaction> getByDestinationId(Integer warehouseId) {
    return internalTransactionRepository.findByDestinationId(warehouseId);
  }

  public List<InternalTransaction> getAllInboundInternal() {
    return internalTransactionRepository.findAllInboundInternal();
  }

  public InternalTransaction getTransactionById(Integer id){
    return internalTransactionRepository.getTransactionById(id);
  }
}

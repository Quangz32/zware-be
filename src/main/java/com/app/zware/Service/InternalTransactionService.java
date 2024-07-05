package com.app.zware.Service;

import com.app.zware.Entities.InternalTransaction;
import com.app.zware.Repositories.InternalTransactionRepository;
import java.util.List;
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
}

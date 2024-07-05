package com.app.zware.Service;

import com.app.zware.Entities.InternalTransaction;
import com.app.zware.Entities.InternalTransactionDetail;
import com.app.zware.Repositories.InternalTransactionDetailRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InternalTransactionDetailService {
  @Autowired
  InternalTransactionDetailRepository internalTransactionDetailRepository;

  public List<InternalTransactionDetail> getAll(){
    return internalTransactionDetailRepository.findAll();
  }

  public InternalTransactionDetail save(InternalTransactionDetail detail){
    return internalTransactionDetailRepository.save(detail);
  }

}

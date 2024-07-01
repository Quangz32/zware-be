package com.app.zware.Service;

import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Repositories.OutboundTransactionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutboundTransactionService {

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  public List<OutboundTransaction> getAllOutboundTransaction() {
    return outboundTransactionRepository.findAll();
  }

  public OutboundTransaction getOutboundTransactionById(int id) {
    return outboundTransactionRepository.findById(id).orElse(null);

  }

  public OutboundTransaction createOutboundTransaction(OutboundTransaction request) {
    request.setStatus("Pending");
    request.setIsdeleted(false);
    return outboundTransactionRepository.save(request);
  }

  public OutboundTransaction save(OutboundTransaction transaction){
    return outboundTransactionRepository.save(transaction);
  }

  public void deleteOutboundTransaction(Integer id) {
    OutboundTransaction outboundTransaction = getOutboundTransactionById(id);
    outboundTransaction.setIsdeleted(true);
    outboundTransactionRepository.save(outboundTransaction);

//        outboundTransactionRepository.deleteById(id);
  }

  public OutboundTransaction update(OutboundTransaction outboundTransaction) {
    return outboundTransactionRepository.save(outboundTransaction);
  }

  public OutboundTransaction merge(Integer id, OutboundTransaction request) {

    OutboundTransaction outboundTransaction = getOutboundTransactionById(id);
    if (outboundTransaction == null) {
      return null;
    }
    Optional.ofNullable(request.getDate()).ifPresent(outboundTransaction::setDate);
    Optional.ofNullable(request.getMaker_id()).ifPresent(outboundTransaction::setMaker_id);
    Optional.ofNullable(request.getStatus()).ifPresent(outboundTransaction::setStatus);
    Optional.ofNullable(request.getDestination()).ifPresent(outboundTransaction::setDestination);
    Optional.ofNullable(request.getExternal_destination())
        .ifPresent(outboundTransaction::setExternal_destination);

    outboundTransaction.setIsdeleted(false);
    return outboundTransaction;
  }

  public boolean existById(Integer id){
    return outboundTransactionRepository.existsByIdAndIsDeletedFalse(id);
  }

}

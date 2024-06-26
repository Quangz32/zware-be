package com.app.zware.Service;

import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.OutboundTransactionDTO;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutboundTransactionService {

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  @Autowired
  OutboundTransactionDetailRepository detailRepository;

  @Autowired
  UserService userService;

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

  //create outboundstransaction and list details
  public OutboundTransactionDTO createOutboundTransactionDTO(OutboundTransactionDTO outboundTransactionDTO, HttpServletRequest request) {
    //get information maker transaction
    User requestUser = userService.getRequestMaker(request);

    OutboundTransaction transaction = new OutboundTransaction();

    transaction.setDate(outboundTransactionDTO.getDate());
    transaction.setMaker_id(requestUser.getId());
    transaction.setStatus("pending");
    transaction.setIsdeleted(false);

    if(outboundTransactionDTO.getDestination() != null) {
      transaction.setDestination(outboundTransactionDTO.getDestination());
    } else {
      transaction.setExternal_destination(outboundTransactionDTO.getExternal_destination());
    }

    //save outboundTransaction
    OutboundTransaction savedTransaction = outboundTransactionRepository.save(transaction);

    //save list details of transaction
    List<OutboundTransactionDetail> details = outboundTransactionDTO.getDetails();
    for (OutboundTransactionDetail detail : details){
      detail.setTransaction_id(savedTransaction.getId());
      detailRepository.save(detail);
    }

    //return transaction
    OutboundTransactionDTO resultDTO = new OutboundTransactionDTO();
    resultDTO.setDate(savedTransaction.getDate());
    resultDTO.setMaker_id(savedTransaction.getMaker_id());
    resultDTO.setStatus(savedTransaction.getStatus());
    resultDTO.setDestination(savedTransaction.getDestination());
    resultDTO.setExternal_destination(savedTransaction.getExternal_destination());
    resultDTO.setIsdeleted(savedTransaction.getIsdeleted());
    resultDTO.setDetails(details);

    return resultDTO;
  }

}

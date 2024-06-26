package com.app.zware.Validation;


import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import com.app.zware.Repositories.UserRepository;
import com.app.zware.Repositories.WarehouseRespository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutBoundTransactionValidator {

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  @Autowired
  WarehouseRespository warehouseRespository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  OutboundTransactionDetailRepository outboundTransactionDetailRepository;

  public String checkPost(OutboundTransaction outboundTransaction) {
    if (outboundTransaction.getDate() == null) {
      return "Date is invalid";
    }

    Integer makerId = outboundTransaction.getMaker_id();
    if (makerId == null || !userRepository.existByIdAndIsDeletedFalse(
        outboundTransaction.getMaker_id())) {
      return "Not found Maker";
    }
//        if( outboundTransaction.getStatus()== null || outboundTransaction.getStatus().isEmpty()){
//            return "Status is invalid";
//        }

//        List<String> statusList = Arrays.asList("pending", "processing", "done", "cancel");
//        if (!statusList.contains(outboundTransaction.getStatus())) {
//            return "Status is not valid";
//        }

    Integer destination = outboundTransaction.getDestination();
    if (destination == null && outboundTransaction.getExternal_destination() == null) {
      return "Destination and External Destination are invalid !";
    } else {
      if (!warehouseRespository.existByIdAndIsDeletedFalse(outboundTransaction.getDestination())) {
        return "Not found ID warehouse for destination";
      }

      if (outboundTransaction.getExternal_destination().isEmpty()) {
        return "External destination is invalid";
      }
    }
    return "";
  }

  public String checkPut(Integer id, OutboundTransaction outboundTransaction) {
    if (id == null || !outboundTransactionRepository.existsById(id)) {
      return "Not found OutboundTransactionID";
    }
    List<String> statusList = Arrays.asList("pending", "processing", "done", "cancel");
    if (!statusList.contains(outboundTransaction.getStatus())) {
      return "Status is not valid";
    }
    return checkPost(outboundTransaction);

  }

  public String checkGet(Integer id) {
    if (!checkId(id)) {
      return "Not found ID";
    } else {
      return "";
    }
  }

  public String checkDelete(Integer id) {
    return checkGet(id);
  }

  public boolean checkId(Integer id) {
    return outboundTransactionRepository.existsByIdAndIsDeletedFalse(id);
  }

  public String checkGetDetail(Integer id) {
    if (!checkId(id)) {
      return "OutboundID not found or OutboundID was deleted !";
    }
    return "";
  }
}

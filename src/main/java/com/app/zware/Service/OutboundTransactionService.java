package com.app.zware.Service;

import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import java.util.List;
import java.util.Optional;

import com.app.zware.Repositories.WarehouseItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutboundTransactionService {

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  @Autowired
  WarehouseItemsRepository warehouseItemsRepository;

  @Autowired
  OutboundTransactionDetailRepository detailRepository;

  public List<OutboundTransaction> getAllOutboundTransaction() {
    return outboundTransactionRepository.findAll();
  }

  public OutboundTransaction getOutboundTransactionById(Integer id) {
    return outboundTransactionRepository.findByOutboundId(id);

  }

  public OutboundTransaction createOutboundTransaction(OutboundTransaction request) {
    request.setStatus("pending");
    request.setIsdeleted(false);
    return outboundTransactionRepository.save(request);
  }

  public OutboundTransaction save(OutboundTransaction transaction) {
    return outboundTransactionRepository.save(transaction);
  }

  public void deleteOutboundTransaction(Integer id) {
    OutboundTransaction outboundTransaction = getOutboundTransactionById(id);
    outboundTransaction.setIsdeleted(true);
    outboundTransactionRepository.save(outboundTransaction);

//        outboundTransactionRepository.deleteById(id);
  }

  public String update(Integer id, OutboundTransaction outboundTransaction) {
    if (outboundTransaction.getStatus().equals("completed")) {
      //set quantity in warehouseItem
      this.updateWarehouseItemsQuantities(id);
      return "The outbound transaction was completed(The quantity of product in warehouse has been updated)";
    }
    //status pending -> processing
    if(outboundTransaction.getStatus().equals("processing")){
      outboundTransactionRepository.save(outboundTransaction);
      return "Status has been updated( Processing ). Products are being prepared";
    }
    // status " cancel "
    outboundTransactionRepository.save(outboundTransaction);
    return "Transaction has been cancelled";
  }

  public OutboundTransaction merge(Integer id, OutboundTransaction request) {

    OutboundTransaction outboundTransaction = getOutboundTransactionById(id);
    if (outboundTransaction == null) {
      return null;
    }
//    Optional.ofNullable(request.getDate()).ifPresent(outboundTransaction::setDate);
//    Optional.ofNullable(request.getMaker_id()).ifPresent(outboundTransaction::setMaker_id);
    Optional.ofNullable(request.getStatus()).ifPresent(outboundTransaction::setStatus);
//    Optional.ofNullable(request.getDestination()).ifPresent(outboundTransaction::setDestination);
//    Optional.ofNullable(request.getExternal_destination())
//        .ifPresent(outboundTransaction::setExternal_destination);

    outboundTransaction.setIsdeleted(false);
    return outboundTransaction;
  }

  public boolean existById(Integer id) {
    return outboundTransactionRepository.existsByIdAndIsDeletedFalse(id);
  }

  public List<OutboundTransaction> getByWarehouse(Integer warehouseId) {
    return outboundTransactionRepository.findByWarehouse(warehouseId);
  }

  public void updateWarehouseItemsQuantities(Integer id) {
    System.out.println(id);
    List<OutboundTransactionDetail> transactionDetails = detailRepository.findByOutboundTransaction(id);
    System.out.println(transactionDetails);
    for (OutboundTransactionDetail detail : transactionDetails) {
      WarehouseItems warehouseItem = warehouseItemsRepository.findByZoneIdAndItemId(detail.getZone_id(), detail.getItem_id());
      if (warehouseItem != null) {
        int newQuantity = warehouseItem.getQuantity() - detail.getQuantity();
        warehouseItem.setQuantity(newQuantity);
        warehouseItemsRepository.save(warehouseItem);
      }
    }
  }
}

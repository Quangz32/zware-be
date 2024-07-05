package com.app.zware.Service;

import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.Repositories.OutboundTransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutboundTransactionService {

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  @Autowired
  WarehouseItemsService warehouseItemsService;

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
//    Optional.ofNullable(request.getExternal_destination())
//        .ifPresent(outboundTransaction::setExternal_destination);

    outboundTransaction.setIsdeleted(false);
    return outboundTransaction;
  }

  public boolean existById(Integer id){
    return outboundTransactionRepository.existsByIdAndIsDeletedFalse(id);
  }

  public List<OutboundTransaction> getByWarehouse(Integer warehouseId){
    return outboundTransactionRepository.findByWarehouse(warehouseId);
  }

  public List<OutboundTransactionDetail> generateOutboundDetail(
      Integer productId, Integer quantity, Integer warehouseId
  ) {

    //this list is sorted by expire_date and quantity
    List<WarehouseItems> warehouseItemList = warehouseItemsService.getByProductAndWarehouse(productId, warehouseId);
//    System.out.println(warehouseItems.toString());

    List<OutboundTransactionDetail> detailList = new ArrayList<>();

    int leftQuantity = quantity;  //Số lg còn lại cần phải lấy

    for (WarehouseItems warehouseItem : warehouseItemList) {
      OutboundTransactionDetail newDetail = new OutboundTransactionDetail();
      newDetail.setItem_id(warehouseItem.getItem_id());
      newDetail.setZone_id(warehouseItem.getZone_id());

      if (warehouseItem.getQuantity() >= leftQuantity) {
        newDetail.setQuantity(leftQuantity);
        detailList.add(newDetail);
        break;
      }

      //if current warehouseItem not enough, get all
      newDetail.setQuantity(warehouseItem.getQuantity()); //Get all
      leftQuantity -= warehouseItem.getQuantity();

      detailList.add(newDetail);
    }

    System.out.println(detailList);

    return detailList;
  }
}

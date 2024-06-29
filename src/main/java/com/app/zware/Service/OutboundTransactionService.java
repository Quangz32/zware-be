package com.app.zware.Service;

import com.app.zware.Entities.*;
import com.app.zware.HttpEntities.OutboundTransactionDTO;
import com.app.zware.Repositories.ItemRepository;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import java.util.List;
import java.util.Optional;

import com.app.zware.Repositories.WarehouseItemsRepository;
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

  @Autowired
  ItemRepository itemRepository;

  @Autowired
  WarehouseItemsRepository warehouseItemsRepository;

  public List<OutboundTransaction> getAllOutboundTransaction() {
    return outboundTransactionRepository.findAll();
  }

  public OutboundTransaction getOutboundTransactionById(Integer id) {
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
    Optional.ofNullable(request.getWarehouse_id()).ifPresent(outboundTransaction::setWarehouse_id);
    outboundTransaction.setIsdeleted(false);
    return outboundTransaction;
  }

  public OutboundTransactionDTO createOutboundTransactionDTO(OutboundTransactionDTO outboundTransactionDTO, HttpServletRequest request) {
    // Get information about the maker of the transaction
    User requestUser = userService.getRequestMaker(request);

    // Create a new OutboundTransaction
    OutboundTransaction transaction = new OutboundTransaction();
    transaction.setDate(outboundTransactionDTO.getDate());
    transaction.setMaker_id(requestUser.getId());
    transaction.setStatus("pending");
    transaction.setIsdeleted(false);
    //get warehouse to find product
    //if manager  create
    if(requestUser.getWarehouse_id() != null){
      transaction.setWarehouse_id(requestUser.getWarehouse_id());
    } else {
      //if admin create
      transaction.setWarehouse_id(outboundTransactionDTO.getWarehouse_id());
    }

    // Set destination based on the DTO
    if (outboundTransactionDTO.getDestination() != null) {
      transaction.setDestination(outboundTransactionDTO.getDestination());
    } else {
      transaction.setExternal_destination(outboundTransactionDTO.getExternal_destination());
    }

    // Save the outbound transaction
    OutboundTransaction savedTransaction = outboundTransactionRepository.save(transaction);
    Integer warehouseId = savedTransaction.getWarehouse_id();
    System.out.println("Id" + warehouseId);
    // Process each transaction item
    List<OutboundTransactionDTO.TransactionItem> transactionItems = outboundTransactionDTO.getTransactionItems();
    for (OutboundTransactionDTO.TransactionItem detail : transactionItems) {
      System.out.println(detail);
      // Retrieve warehouse items prioritizing by expiry date and quantity in zone

      List<WarehouseItems> warehouseItemsList = warehouseItemsRepository.findNearestExpiryItems(detail.getProductId(), savedTransaction.getWarehouse_id());

      // Calculate required quantities
      int remainingQuantity = detail.getQuantityProduct();

      // Iterate through warehouse items to deduct quantities
      for (WarehouseItems warehouseItem : warehouseItemsList) {
        if (remainingQuantity <= 0) {
          break;
        }

        int quantityAvailable = warehouseItem.getQuantity();
        if (quantityAvailable > 0) {
          int quantityToDeduct = Math.min(quantityAvailable, remainingQuantity);

          // Update the warehouse item quantity
//          warehouseItem.setQuantity(quantityAvailable - quantityToDeduct);
//          warehouseItemsRepository.save(warehouseItem);

          // Create and save the outbound transaction detail
          OutboundTransactionDetail transactionDetail = new OutboundTransactionDetail();
          transactionDetail.setTransaction_id(savedTransaction.getId());
          transactionDetail.setZone_id(warehouseItem.getZone_id());
          transactionDetail.setItem_id(warehouseItem.getItem_id());
          transactionDetail.setQuantity(quantityToDeduct);
          detailRepository.save(transactionDetail);

          // Logging the saved detail
          System.out.println("Saved transaction detail: " + transactionDetail);

          // Update remaining quantity needed
          remainingQuantity -= quantityToDeduct;
        }
        }
      }

      // Prepare the result DTO
      OutboundTransactionDTO resultDTO = new OutboundTransactionDTO();
      resultDTO.setId(savedTransaction.getId());
      resultDTO.setDate(savedTransaction.getDate());
      resultDTO.setMaker_id(savedTransaction.getMaker_id());
      resultDTO.setStatus(savedTransaction.getStatus());
      resultDTO.setDestination(savedTransaction.getDestination());
      resultDTO.setExternal_destination(savedTransaction.getExternal_destination());
      resultDTO.setIsdeleted(savedTransaction.getIsdeleted());
      resultDTO.setWarehouse_id(savedTransaction.getWarehouse_id());
      resultDTO.setTransactionItems(outboundTransactionDTO.getTransactionItems()); // Include the transaction items in the result DTO

      return resultDTO;
    }

  public void updateWarehouseItemsQuantities(Integer id, OutboundTransaction outboundTransaction) {
    System.out.println(id);
    List<OutboundTransactionDetail> transactionDetails = detailRepository.findDetailsAndIsDeletedFalse(id);
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

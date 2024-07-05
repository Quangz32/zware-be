package com.app.zware.Controllers;

import com.app.zware.Entities.*;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.InternalDetailDTO;
import com.app.zware.HttpEntities.InternalTransactionDTO;
import com.app.zware.HttpEntities.OutboundDetailDTO;
import com.app.zware.Service.*;
import com.app.zware.Validation.InternalTransactionValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal_transactions")
public class InternalTransactionController {
  @Autowired
  InternalTransactionService internalTransactionService;

  @Autowired
  UserService userService;

  @Autowired
  InternalTransactionValidator internalTransactionValidator;

  @Autowired
  OutboundTransactionService outboundTransactionService;

  @Autowired
  InternalTransactionDetailService internalTransactionDetailService;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  ItemService itemService;


  @GetMapping()
  public ResponseEntity<?> index(){
    CustomResponse customResponse = new CustomResponse();
    customResponse.setAll(true,"Get all internal transaction success" ,
        internalTransactionService.getAll());

    return ResponseEntity.ok(customResponse);
  }

  @PostMapping("create")
  public  ResponseEntity<?> create(
      @RequestBody InternalTransactionDTO transactionDTO,
      HttpServletRequest request){
    CustomResponse customResponse = new CustomResponse();

    //Authorization
    User requestMaker = userService.getRequestMaker(request);
    if (!requestMaker.getRole().equals("admin")){
      //
      if (transactionDTO.getType().equals("inbound") &&
          !requestMaker.getWarehouse_id().equals(transactionDTO.getDestination_warehouse()) ){
        customResponse.setAll(false, "You are not allowed", null);
        return ResponseEntity.ok(customResponse);
      }

      if (transactionDTO.getType().equals("outbound") &&
      !requestMaker.getWarehouse_id().equals(transactionDTO.getSource_warehouse())){
        customResponse.setAll(false, "You are not allowed", null);
        return ResponseEntity.ok(customResponse);
      }
    }

    //Validation
    String checkMessage = internalTransactionValidator.checkCreate(transactionDTO);
    if (!checkMessage.isEmpty()){
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //passed validation
    InternalTransaction newTransaction = new InternalTransaction();
    newTransaction.setType(transactionDTO.getType());
    newTransaction.setSource_warehouse(transactionDTO.getSource_warehouse());
    newTransaction.setDestination_warehouse(transactionDTO.getDestination_warehouse());
    newTransaction.setDate(LocalDate.now());
    newTransaction.setMaker_id(requestMaker.getId());
    newTransaction.setStatus("pending");

    InternalTransaction savedTransaction = internalTransactionService.save(newTransaction);

    for (InternalDetailDTO detail : transactionDTO.getDetails()) {
      List<OutboundTransactionDetail> generatedDetailList =
          outboundTransactionService.generateOutboundDetail(
              detail.getProduct_id(), detail.getQuantity(), transactionDTO.getSource_warehouse()
          );

      for (OutboundTransactionDetail generatedDetail : generatedDetailList) {

        InternalTransactionDetail newDetail = new InternalTransactionDetail();
        newDetail.setTransaction_id(savedTransaction.getId());
        if (savedTransaction.getType().equals("inbound")){
          newDetail.setDestination_zone(detail.getDestination_zone());
        }
        newDetail.setSource_zone(generatedDetail.getZone_id());
        newDetail.setItem_id(generatedDetail.getItem_id());
        newDetail.setQuantity(generatedDetail.getQuantity());

        internalTransactionDetailService.save(newDetail);

      }
      System.out.println(detail);
    }

    customResponse.setAll(true, "Create internal transaction success", savedTransaction);
    return ResponseEntity.ok(customResponse);
  }

  @PutMapping("{id}/change_status")
  public ResponseEntity<?> changeStatus (
          @PathVariable Integer id,
          @RequestBody InternalTransaction updatedTransaction,
          HttpServletRequest request){

    // response
    CustomResponse customResponse = new CustomResponse();

    InternalTransaction transaction = internalTransactionService.findById(id);
    if(transaction==null){
      customResponse.setAll(false,"Transaction not found",null);
      return new ResponseEntity<>(customResponse,HttpStatus.NOT_FOUND);
    }

    //Authorization
    User requestMaker = userService.getRequestMaker(request);
    boolean isAdmin = requestMaker.getRole().equals("admin");
    boolean isSourceOwner = requestMaker.getWarehouse_id().equals(transaction.getSource_warehouse());
    boolean isDestinationOwner = requestMaker.getWarehouse_id().equals(transaction.getDestination_warehouse());

    String currentStatus = transaction.getStatus();
    String newStatus = updatedTransaction.getStatus();

    if (currentStatus.equals("pending")) {
      if (!newStatus.equals("shipping") && !newStatus.equals("canceled")) {
        customResponse.setAll(false, "Invalid status transition", null);
        return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
      }
      if (newStatus.equals("shipping") && !isAdmin && !isSourceOwner) {
        customResponse.setAll(false, "You are not allowed to change status to shipping", null);
        return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
      }
    } else if (currentStatus.equals("shipping")) {
      if (!newStatus.equals("completed") && !newStatus.equals("canceled")) {
        customResponse.setAll(false, "Invalid status transition", null);
        return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
      }
      if ((newStatus.equals("canceled") || newStatus.equals("completed")) &&
              !isAdmin && !isSourceOwner && !isDestinationOwner) {
        customResponse.setAll(false, "You are not allowed to change status to canceled or completed", null);
        return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
      }
    } else {
      customResponse.setAll(false, "Invalid current status", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    List<InternalTransactionDetail> details = internalTransactionDetailService.findByTransactionId(transaction.getId());

    if(newStatus.equals("shipping")){
      for(InternalTransactionDetail detail : details){

        Item item = itemService.getItemById(detail.getItem_id());
        Integer productId = item.getProduct_id();
        LocalDate expireDate =  item.getExpire_date();
        warehouseItemsService.removeFromZone(detail.getSource_zone(),productId,expireDate,detail.getQuantity());
      }
    } else if (newStatus.equals("canceled")) {
       if(currentStatus.equals("shipping")){
         for (InternalTransactionDetail detail : details){
             Item item = itemService.getItemById(detail.getItem_id());
             Integer productId = item.getProduct_id();
             LocalDate expireDate =  item.getExpire_date();
             warehouseItemsService.addToZone(detail.getSource_zone(),productId,expireDate,detail.getQuantity());

         }
       }

    } else if (newStatus.equals("completed")) {
        if (currentStatus.equals("shipping")){
          for (InternalTransactionDetail detail : details){
            Item item = itemService.getItemById(detail.getItem_id());
            Integer productId = item.getProduct_id();
            LocalDate expireDate =  item.getExpire_date();
            warehouseItemsService.addToZone(detail.getDestination_zone(),productId,expireDate,detail.getQuantity());
          }
        }

    }
    // Update status
    transaction.setStatus(newStatus);
    internalTransactionService.save(transaction);

    customResponse.setAll(true, "Status changed successfully", transaction);
    return ResponseEntity.ok(customResponse);
  }






}

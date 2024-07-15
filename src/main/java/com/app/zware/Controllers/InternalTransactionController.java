package com.app.zware.Controllers;

import com.app.zware.Entities.*;
import com.app.zware.HttpEntities.*;
import com.app.zware.Service.*;
import com.app.zware.Validation.InternalTransactionValidator;
import com.app.zware.Validation.WarehouseValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

  @Autowired
  WarehouseValidator warehouseValidator;


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
          @RequestBody Map<String,String> requestBody,
          HttpServletRequest request){

    // response
    CustomResponse customResponse = new CustomResponse();

    InternalTransaction transaction = internalTransactionService.findById(id);
    if(transaction==null){
      customResponse.setAll(false,"Transaction not found",null);
      return new ResponseEntity<>(customResponse,HttpStatus.NOT_FOUND);
    }

    //Authorization
//    User requestMaker = userService.getRequestMaker(request);
//    boolean isAdmin = requestMaker.getRole().equals("admin");
//    boolean isSourceOwner = requestMaker.getWarehouse_id().equals(transaction.getSource_warehouse());
//    boolean isDestinationOwner = requestMaker.getWarehouse_id().equals(transaction.getDestination_warehouse());

    User requestMaker = userService.getRequestMaker(request);
    boolean isAdmin = "admin".equals(requestMaker.getRole());

    boolean isSourceOwner = !isAdmin && requestMaker.getWarehouse_id() != null &&
            requestMaker.getWarehouse_id().equals(transaction.getSource_warehouse());

    boolean isDestinationOwner = !isAdmin && requestMaker.getWarehouse_id() != null &&
            requestMaker.getWarehouse_id().equals(transaction.getDestination_warehouse());

    String currentStatus = transaction.getStatus();
    String newStatus = requestBody.get("status");

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
        WarehouseItems updatedWi = warehouseItemsService.removeFromZone(detail.getSource_zone(),productId,expireDate,detail.getQuantity());
        if (updatedWi == null) {
          customResponse.setAll(false, "Source does not have enough quantity", null);
          return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
        }
      }
    } else if (newStatus.equals("canceled")) {
       if(currentStatus.equals("shipping")){
         for (InternalTransactionDetail detail : details){
             Item item = itemService.getItemById(detail.getItem_id());
             Integer productId = item.getProduct_id();
             LocalDate expireDate =  item.getExpire_date();
             WarehouseItems updateWi = warehouseItemsService.addToZone(detail.getSource_zone(),productId,expireDate,detail.getQuantity());



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

  @GetMapping(params = "warehouse_id")
  public ResponseEntity<?> getByWarehouse(
          @RequestParam("warehouse_id") Integer warehouseId) {

    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
      //finally
    }
    //Get all transaction when source or destination = warehouseID
      customResponse.setAll(true, "Get Internal Transaction success",
              internalTransactionService.getByWarehouse(warehouseId));

      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    @GetMapping("/outbound")
    public ResponseEntity<?> getOutboundByDestinationId(@RequestParam("warehouse_id") Integer destinationId){
      //Response
      CustomResponse customResponse = new CustomResponse();
      //Validation
      String checkMessage = warehouseValidator.checkGet(destinationId);
      if (!checkMessage.isEmpty()) {
        customResponse.setAll(false, checkMessage, null);
        return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
        //finally
      }
      //Get all transaction when source or destination = warehouseID
      customResponse.setAll(true, "Get Outbound Internal Transaction success",
              internalTransactionService.getOutboundByDestinationId(destinationId));

      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

  @GetMapping("/inbound")
  public ResponseEntity<?> getInboundByDestinationId(@RequestParam("warehouse_id") Integer sourceId){
    //Response
    CustomResponse customResponse = new CustomResponse();
    //Validation
    String checkMessage = warehouseValidator.checkGet(sourceId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
      //finally
    }
    customResponse.setAll(true, "Get Inbound Internal Transaction success",
            internalTransactionService.getInboundByDestinationId(sourceId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping("/all_outbound")
  public ResponseEntity<?> getAllOutboundInternal(){
    //Response
    CustomResponse customResponse = new CustomResponse();
    //Validation
    customResponse.setAll(true, "Get All Outbound Internal Transaction success",
            internalTransactionService.getAllOutboundInternal());

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping("/all_inbound")
  public ResponseEntity<?> getAllInboundInternal(){
    //Response
    CustomResponse customResponse = new CustomResponse();
    //Validation
    customResponse.setAll(true, "Get All Inbound Internal Transaction success",
            internalTransactionService.getAllInboundInternal());

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  //Get transaction by id
    @GetMapping("/{id}")
    public ResponseEntity<?> show(@PathVariable Integer id) {
      //response
      CustomResponse response = new CustomResponse();

      //Authorization : ALL

      //validate
      String checkMessage = internalTransactionValidator.checkGet(id);
      if (!checkMessage.isEmpty()) {
        response.setAll(false, checkMessage, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        //finally
      }

      response.setAll(true, "Get transaction by id : " + id + " successfully.", internalTransactionService.getTransactionById(id));
      return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //update destination_zone
    }






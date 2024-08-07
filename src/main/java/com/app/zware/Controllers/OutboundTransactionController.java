package com.app.zware.Controllers;

import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.OutboundDetailDTO;
import com.app.zware.HttpEntities.OutboundTransactionDTO;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Service.OutboundTransactionDetailService;
import com.app.zware.Service.OutboundTransactionService;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Validation.OutBoundTransactionValidator;
import com.app.zware.Validation.WarehouseValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/outbound_transactions")
public class OutboundTransactionController {

  @Autowired
  OutboundTransactionService outboundTransactionService;

  @Autowired
  OutBoundTransactionValidator outBoundTransactionValidator;

  @Autowired
  UserService userService;

  @Autowired
  OutboundTransactionDetailService transactionDetailService;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  OutboundTransactionDetailService outboundTransactionDetailService;

  @Autowired
  WarehouseValidator warehouseValidator;

  @PostMapping("/create")
  public ResponseEntity<?> createOutboundTransaction(
      @RequestBody OutboundTransactionDTO transactionDTO,
      HttpServletRequest request
  ) {
    CustomResponse customResponse = new CustomResponse();

    //authorization
    User requestMaker = userService.getRequestMaker(request);
    if (!requestMaker.getRole().equals("admin") &&
        !requestMaker.getWarehouse_id().equals(transactionDTO.getWarehouse_id())
    ) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //validation
    String message = outBoundTransactionValidator.checkCreate(transactionDTO);
    if (!message.isEmpty()) {
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Save to db
    //NEW TRANSACTION
    OutboundTransaction newTransaction = new OutboundTransaction();
    newTransaction.setWarehouse_id(transactionDTO.getWarehouse_id());
    newTransaction.setDate(LocalDate.now());
    newTransaction.setMaker_id(requestMaker.getId());
    newTransaction.setStatus("pending");  //default when create
    newTransaction.setDestination(transactionDTO.getDestination());

    OutboundTransaction savedTransaction = outboundTransactionService.save(newTransaction);

    for (OutboundDetailDTO detail : transactionDTO.getDetails()) {
      List<OutboundTransactionDetail> generatedDetailList =
          warehouseItemsService.generateOutboundDetail(
              detail.getProduct_id(), detail.getQuantity(), transactionDTO.getWarehouse_id()
          );

      for (OutboundTransactionDetail generatedDetail : generatedDetailList) {
        generatedDetail.setTransaction_id(savedTransaction.getId());
        outboundTransactionDetailService.save(generatedDetail);
      }
      System.out.println(detail);
    }

    customResponse.setAll(true, "Create outbound transaction success", savedTransaction);
    return ResponseEntity.ok(customResponse);
  }

  @GetMapping("")
  public ResponseEntity<?> index() {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: any authenticated user
    List<OutboundTransaction> outboundTransactionList = outboundTransactionService.getAllOutboundTransaction();
    if (outboundTransactionList.isEmpty()) {
      customResponse.setAll(false, "List are empty", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      customResponse.setAll(true, "Get data of all outboundTransaction success",
          outboundTransactionList);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> show(@PathVariable("id") Integer id) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: any authenticated user
    //check validate
    String message = outBoundTransactionValidator.checkGet(id);

    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //get outbound transaction
      customResponse.setAll(true, "Get data of outboundTransaction with id: " + id + " success",
          outboundTransactionService.getOutboundTransactionById(id));
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody OutboundTransaction request) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: any authenticated user
    //check validate
    String message = outBoundTransactionValidator.checkPost(request);

    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //create new outbound transaction
      customResponse.setAll(true, "OutboundTransaction has been created",
          outboundTransactionService.createOutboundTransaction(request));
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> destroy(@PathVariable("id") Integer id, HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Validation: Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //check validate
    String message = outBoundTransactionValidator.checkDelete(id);

    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve delete
      outboundTransactionService.deleteOutboundTransaction(id);
      customResponse.setAll(true, "OutboundTransaction with id: " + id + " has been deleted", null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @PutMapping("/{id}/change_status")
  public ResponseEntity<?> update(@PathVariable("id") Integer id,
                                  @RequestBody Map<String, String> statusUpdated,
                                  HttpServletRequest userRequest) {
    // response
    CustomResponse customResponse = new CustomResponse();

    // validate checkGet
    String checkedMessage = outBoundTransactionValidator.checkGet(id);
    if (!checkedMessage.isEmpty()) {
      customResponse.setAll(false, checkedMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    Integer warehouseId = outboundTransactionService.getOutboundTransactionById(id).getWarehouse_id();

    // validation: admin or manager warehouse
    User user = userService.getRequestMaker(userRequest);
    if (!"admin".equals(user.getRole()) && !warehouseId.equals(user.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    // extract and validate status
    String status = statusUpdated.get("status");
    if (status == null || status.isEmpty()) {
      customResponse.setAll(false, "Status is required", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    System.out.println("Status: " + status);
    String checkedStatus = outBoundTransactionValidator.checkStatus(id, status);
    if (!checkedStatus.isEmpty()) {
      customResponse.setAll(false, checkedStatus, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    // update new status
    OutboundTransaction transaction = outboundTransactionService.getOutboundTransactionById(id);
    String messageResponse = "";

    if ("pending".equals(transaction.getStatus()) && "shipping".equals(status)) {
      // get list details of transaction
      List<OutboundTransactionDetail> detailList = transactionDetailService.getByOutboundTransaction(id);

      // loop check quantity
      for (OutboundTransactionDetail detail : detailList) {
        // check validate quantity
        String checkedQuantity = outBoundTransactionValidator.checkQuantity(detail.getZone_id(), detail.getItem_id(), detail.getQuantity());
        if (!checkedQuantity.isEmpty()) {
          // if any quantity check fails, set status to canceled and return
          transaction.setStatus("canceled");
          outboundTransactionService.save(transaction);
          customResponse.setAll(false, "Insufficient quantity, status changed to canceled", null);
          return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
        }
      }

      // if all quantity checks pass, proceed with shipping
      for (OutboundTransactionDetail detail : detailList) {
        // remove item
        warehouseItemsService.removeItemToZone(detail.getZone_id(), detail.getItem_id(), detail.getQuantity());
      }
      transaction.setStatus("shipping");
      outboundTransactionService.save(transaction);
      messageResponse = "Products are being shipped";

    } else if ("pending".equals(transaction.getStatus()) && "canceled".equals(status)) {
      // Do nothing if status changes from pending to canceled
      messageResponse = "Transaction has been canceled!";
      transaction.setStatus("canceled");
      outboundTransactionService.save(transaction);

    } else if ("shipping".equals(transaction.getStatus()) && "canceled".equals(status)) {
      // get list details of transaction
      List<OutboundTransactionDetail> detailList = transactionDetailService.getByOutboundTransaction(id);

      // loop to return items to warehouse
      for (OutboundTransactionDetail detail : detailList) {
        // add item back
        warehouseItemsService.addItemToZone(detail.getZone_id(), detail.getItem_id(), detail.getQuantity());
      }
      transaction.setStatus("canceled");
      outboundTransactionService.save(transaction);
      messageResponse = "Transaction has been canceled. The product will be returned to the warehouse";
    } else {
      transaction.setStatus(status);
      outboundTransactionService.save(transaction);
      messageResponse = "Transaction has been " + status + "!";
    }

    customResponse.setAll(true, messageResponse, null);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping(params = "warehouse_id")
  public ResponseEntity<?> getByWarehouse(@RequestParam("warehouse_id") Integer warehouseId) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: any authenticated user
    //check validate
    String message = warehouseValidator.checkGet(warehouseId);
    if (!message.isEmpty()) {
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //get outbound transaction
    customResponse.setAll(true, "Get outbound transactions of warehouse " + warehouseId + " success",
        outboundTransactionService.getByWarehouse(warehouseId));
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

//  @GetMapping("/{id}/details")
//  public ResponseEntity<?> getOutboundDetailsByOutboundId(@PathVariable("id") Integer id) {
//    //response
//    CustomResponse customResponse = new CustomResponse();
//
//    //Authorization : ALL
//
//    //validate
//    String message = outBoundTransactionValidator.checkGetDetail(id);
//    if (!message.isEmpty()) {
//      customResponse.setAll(false, message, null);
//      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
//    } else {
//      List<OutboundTransactionDetail> outboundTransactionDetail =
//          outboundTransactionDetailRepository.findAllDetailsByOutboundTransactionIdAndIsDeletedFalse(
//              id);
//      customResponse.setAll(true, "Get all data details of OutboundTransaction with id : " +
//          id + " has been success", outboundTransactionDetail);
//      return new ResponseEntity<>(customResponse, HttpStatus.OK);
//    }
//  }
//

}

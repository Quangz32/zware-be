package com.app.zware.Controllers;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.Entities.Item;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.User;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.InboundDetailDTO;
import com.app.zware.HttpEntities.InboundTransactionDTO;
import com.app.zware.Service.InboundTransactionDetailService;
import com.app.zware.Service.InboundTransactionService;
import com.app.zware.Service.ItemService;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Validation.InboundTransactionValidator;
import com.app.zware.Validation.WarehouseValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inbound_transactions")
public class InboundTransactionController {

  @Autowired
  InboundTransactionService service;

  @Autowired
  InboundTransactionValidator validator;

  @Autowired
  UserService userService;

  @Autowired
  ItemService itemService;

  @Autowired
  InboundTransactionDetailService inboundTransactionDetailService;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  WarehouseValidator warehouseValidator;

  @PostMapping("/create")
  public ResponseEntity<?> createInboundTransaction(
      @RequestBody InboundTransactionDTO inboundDto,
      HttpServletRequest request)
  {
    CustomResponse customResponse = new CustomResponse();

    //authorization
    User requestMaker = userService.getRequestMaker(request);
    if (!requestMaker.getRole().equals("admin") &&
        !requestMaker.getWarehouse_id().equals(inboundDto.getWarehouse_id())
    ){
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }



    //validation
    String message = validator.checkCreate(inboundDto);
    if (!message.isEmpty()){
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //after validation, create and save to DB

    //NEW TRANSACTIOn
    InboundTransaction newTransaction = new InboundTransaction();
    newTransaction.setWarehouse_id(inboundDto.getWarehouse_id());
    newTransaction.setDate(LocalDate.now());
    newTransaction.setMaker_id(requestMaker.getId());
    newTransaction.setStatus("pending");  //default when create
    if (inboundDto.getSource() == null){
      newTransaction.setExternal_source(inboundDto.getExternal_source());
    } else{
      newTransaction.setSource(inboundDto.getSource());
    }

    InboundTransaction savedTransaction = service.save(newTransaction);



    //NEW TRANSACTION'S DETAILS
    //If source is external
    if (inboundDto.getSource() == null){
      for (InboundDetailDTO detail : inboundDto.getDetails()){
        Item itemToSave =
            itemService.getOrCreateByProductAndDate(detail.getProduct_id(), detail.getExpire_date());

        InboundTransactionDetail detailToSave = new InboundTransactionDetail();
        detailToSave.setTransaction_id(savedTransaction.getId());
        detailToSave.setItem_id(itemToSave.getId());
        detailToSave.setZone_id(detail.getZone_id());
        detailToSave.setQuantity(detail.getQuantity());
        inboundTransactionDetailService.save(detailToSave);
      }
    } else{
//      detail: cần lấy sản phầm gì, số lượng bao nhiêu, để vào zone nào
      for (InboundDetailDTO detail : inboundDto.getDetails()){

        //Tự động lấy hàng cho đủ số lượng, theo đúng thứ tự ưu tiên
        List<OutboundTransactionDetail> generatedDetailList =
            warehouseItemsService.createTransactionDetailsByProductAndQuantityAndWarehouse(
                detail.getProduct_id(), detail.getQuantity(), inboundDto.getSource()
            );
        for (OutboundTransactionDetail generatedDetail : generatedDetailList){
          InboundTransactionDetail detailToSave = new InboundTransactionDetail();
          detailToSave.setTransaction_id(savedTransaction.getId());
          detailToSave.setItem_id(generatedDetail.getItem_id());
          detailToSave.setZone_id(generatedDetail.getZone_id());
          detailToSave.setQuantity(generatedDetail.getQuantity());
          inboundTransactionDetailService.save(detailToSave);
        }
      }
    }


    customResponse.setAll(true, "Create inbound transaction success", null);
    return ResponseEntity.ok(customResponse);
  }

  @GetMapping("")
  public ResponseEntity<?> index() {
    //Validation: All authenticated user

    //response
    CustomResponse customResponse = new CustomResponse();

    //GET
    customResponse.setAll(true, "Get data of all inbound transaction success", service.getAll());
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> show(@PathVariable Integer id) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization: any authenticated user
    //Validate
    String checkMessage = validator.checkGet(id);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Get
    customResponse.setAll(true, "get data of inbound transaction with id " + id + " success",
        service.getById(id));
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody InboundTransaction transaction) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization: any authenticated user
    //Validate
    String checkMessage = validator.checkPost(transaction);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Save

    InboundTransaction created = service.save(transaction);
    customResponse.setAll(true, "InboundTransaction created", created);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable Integer id,
      @RequestBody InboundTransaction transaction,
      HttpServletRequest request
  ) {

    //response
    CustomResponse customResponse = new CustomResponse();
    //Validation: Admin or Transaction's maker
    User requestMaker = userService.getRequestMaker(request);
    if (!requestMaker.getRole().equals("admin") && !requestMaker.getId()
        .equals(transaction.getId())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //Merge info
    InboundTransaction mergedTransaction = service.merge(id, transaction);

    //Validate
    String checkMessage = validator.checkPut(id, mergedTransaction);
    if (!checkMessage.isEmpty()) {

      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Update
    InboundTransaction updatedTransaction = service.update(mergedTransaction);
    customResponse.setAll(true, "Inbound Transaction update successful", updatedTransaction);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @DeleteMapping("{id}")
  public ResponseEntity<?> destroy(
      @PathVariable Integer id,
      HttpServletRequest request
  ) {

    //Response
    CustomResponse customResponse = new CustomResponse();
    //Validation: Admin only
    User requestMaker = userService.getRequestMaker(request);
    if (!requestMaker.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //Validate
    String checkMessage = validator.checkDelete(id);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Delete
    service.delete(id);
    customResponse.setAll(true, "Inbound Transaction with id " + id + " has been deleted", null);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

//  @GetMapping("/{id}/details")
//  public ResponseEntity<?> getInboundTransactionDetails(@PathVariable("id") Integer id) {
//
//    //Response
//    CustomResponse customResponse = new CustomResponse();
//
//    //Authorization : ALL
//
//    //Validation
//    String checkMessage = validator.checkGet(id);
//    if (!checkMessage.isEmpty()) {
//      customResponse.setAll(false, checkMessage, null);
//      return new ResponseEntity<>(customResponse, HttpStatus.OK);
//    }
//
//    //finally
//    customResponse.setAll(true, "Get Inbound Transaction Details success",
//        service.getInboundDetailsByTransactionId(id));
//    return new ResponseEntity<>(customResponse, HttpStatus.OK);
//
//  }

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
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    //finally
    customResponse.setAll(true, "Get Inbound Transaction success",
            service.getInboundByWarehouseId(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

}

package com.app.zware.Controllers;


import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Service.OutboundTransactionDetailService;
import com.app.zware.Service.UserService;
import com.app.zware.Validation.OutBoundTransactionValidator;
import com.app.zware.Validation.OutboundTransactionDetailValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
@RequestMapping("api/outbound_transaction_details")
public class OutboundTransactionDetailController {

  @Autowired
  OutboundTransactionDetailService outboundTransactionDetailService;

  @Autowired
  OutboundTransactionDetailValidator outboundTransactionDetailValidator;

  @Autowired
  UserService userService;

  @Autowired
  OutBoundTransactionValidator outBoundTransactionValidator;

  @GetMapping("")
  public ResponseEntity<?> index() {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Validation: All
    List<OutboundTransactionDetail> transactionDetailList = outboundTransactionDetailService.getAll();
    if (transactionDetailList.isEmpty()) {
      customResponse.setAll(false, "List are empty!", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      customResponse.setAll(true, "Get data of all OutboundTransactionDetail success",
          transactionDetailList);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> show(@PathVariable("id") Integer id) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Validation: All
    //check validate
    String message = outboundTransactionDetailValidator.checkGet(id);

    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve get
      customResponse.setAll(true,
          "Get data of outboundTransactionDetail with id: " + id + " success"
          , outboundTransactionDetailService.getById(id));
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @GetMapping(params = "transaction_id")
  public ResponseEntity<?> getByOutboundTransaction(
      @RequestParam("transaction_id") Integer transactionId) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Validation: All
    //check validate
    String message = outBoundTransactionValidator.checkGet(transactionId);
    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }
    //approve get
    customResponse.setAll(true,
        "Get detail of outbound transaction [" + transactionId + "] success"
        , outboundTransactionDetailService.getByOutboundTransaction(transactionId));
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody OutboundTransactionDetail request,
      HttpServletRequest userRequest) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: Admin or transaction maker
    User user = userService.getRequestMaker(userRequest);
    OutboundTransaction outboundTransaction = outboundTransactionDetailService.getTransaction(
        request);
    if (!user.getRole().equals("admin") && !user.getId()
        .equals(outboundTransaction.getMaker_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }
    //check validate
    String message = outboundTransactionDetailValidator.checkPost(request);

    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //create new outbound transactions details
      customResponse.setAll(true, "OutboundTransactionDetail has been created",
          outboundTransactionDetailService.create(request));
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> destroy(@PathVariable("id") Integer id, HttpServletRequest userRequest) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: Admin
    User user = userService.getRequestMaker(userRequest);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //check validate
    String message = outboundTransactionDetailValidator.checkDelete(id);

    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve delete
      outboundTransactionDetailService.delete(id);
      customResponse.setAll(false, "OutboundTransactionDetail with id: " + id + " has been deleted",
          null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Integer id,
      @RequestBody OutboundTransactionDetail request,
      HttpServletRequest userRequest) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: Admin or transaction maker
    User user = userService.getRequestMaker(userRequest);
    OutboundTransaction outboundTransaction = outboundTransactionDetailService.getTransaction(
        request);
    if (!user.getRole().equals("admin") && !user.getId()
        .equals(outboundTransaction.getMaker_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //merge info
    OutboundTransactionDetail updatedOutboundDetails = outboundTransactionDetailService.merge(id,
        request);

    //check validate
    String message = outboundTransactionDetailValidator.checkPut(id, updatedOutboundDetails);
    if (!message.isEmpty()) {
      //error
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve updated
      outboundTransactionDetailService.update(updatedOutboundDetails);
      customResponse.setAll(true, "OutboundTransactionDetail has been updated",
          updatedOutboundDetails);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }


}

package com.app.zware.Controllers;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Service.InboundTransactionService;
import com.app.zware.Service.UserService;
import com.app.zware.Validation.InboundTransactionValidator;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbound_transactions")
public class InboundTransactionController {

  @Autowired
  InboundTransactionService service;

  @Autowired
  InboundTransactionValidator validator;

  @Autowired
  UserService userService;

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

  @GetMapping("/{id}/details")
  public ResponseEntity<?> getInboundTransactionDetails(@PathVariable("id") Integer id) {

    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = validator.checkGet(id);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    //finally
    customResponse.setAll(true, "Get Inbound Transaction Details success",
        service.getInboundDetailsByTransactionId(id));
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }
}

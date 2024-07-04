package com.app.zware.Controllers;

import com.app.zware.Entities.InternalTransaction;
import com.app.zware.Entities.InternalTransactionDetail;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.InternalDetailDTO;
import com.app.zware.HttpEntities.InternalTransactionDTO;
import com.app.zware.HttpEntities.OutboundDetailDTO;
import com.app.zware.Service.InternalTransactionDetailService;
import com.app.zware.Service.InternalTransactionService;
import com.app.zware.Service.OutboundTransactionService;
import com.app.zware.Service.UserService;
import com.app.zware.Validation.InternalTransactionValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

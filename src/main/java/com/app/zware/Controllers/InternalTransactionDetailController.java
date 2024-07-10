package com.app.zware.Controllers;

import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Service.InternalTransactionDetailService;
import com.app.zware.Validation.InternalTransactionDetailValidator;
import com.app.zware.Validation.InternalTransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal_transaction_details")
public class InternalTransactionDetailController {

    @Autowired
    InternalTransactionValidator validator;

    @Autowired
    InternalTransactionDetailService service;

    @GetMapping(params = "transaction_id")
    public ResponseEntity<?> getByDisposal(
            @RequestParam("transaction_id") Integer transactionId) {

        //Response
        CustomResponse customResponse = new CustomResponse();

        //Authorization : ALL

        //Validation
        String checkMessage = validator.checkGet(transactionId);
        if (!checkMessage.isEmpty()) {
            customResponse.setAll(false, checkMessage, null);
            return new ResponseEntity<>(customResponse, HttpStatus.OK);
        }

        //finally
        customResponse.setAll(true, "Get Internal Transaction Details success",
                service.findByTransactionId(transactionId));

        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
}

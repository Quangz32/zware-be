package com.app.zware.Controllers;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.InternalTransaction;
import com.app.zware.Entities.InternalTransactionDetail;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.DestinationZoneInternalUpdateDTO;
import com.app.zware.Service.InternalTransactionDetailService;
import com.app.zware.Service.InternalTransactionService;
import com.app.zware.Service.UserService;
import com.app.zware.Validation.InternalTransactionDetailValidator;
import com.app.zware.Validation.InternalTransactionValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal_transaction_details")
public class InternalTransactionDetailController {

    @Autowired
    InternalTransactionValidator validator;

    @Autowired
    InternalTransactionDetailService service;

    @Autowired
    InternalTransactionService internalTransactionService;

    @Autowired
    UserService userService;

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

    @PutMapping("/{transactionId}/destination_zones")
    public ResponseEntity<?> updateDestinationZones(
            @PathVariable Integer transactionId,
            @RequestBody List<DestinationZoneInternalUpdateDTO> updates,
            HttpServletRequest request) {
        // Response
        CustomResponse customResponse = new CustomResponse();

        // Get transaction
        InternalTransaction transaction = internalTransactionService.findById(transactionId);
        if (transaction == null) {
            customResponse.setAll(false, "Transaction not found", null);
            return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
        }

        // Authorization: Admin or transaction maker
        User user = userService.getRequestMaker(request);
        if (!user.getRole().equals("admin") && !user.getWarehouse_id().equals(transaction.getDestination_warehouse())) {
            customResponse.setAll(false, "You are not allowed", null);
            return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
        }

        // Update destination zones sequentially
        for (DestinationZoneInternalUpdateDTO update : updates) {
            List<InternalTransactionDetail> details = service.findByTransactionId(update.getDetailId());
            if (details == null || details.isEmpty()) {
                customResponse.setAll(false, "Detail not found or does not belong to the transaction", null);
                return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
            }

            List<Integer> destinationZones = update.getDestinationZones();
            if (destinationZones == null || destinationZones.isEmpty()) {
                customResponse.setAll(false, "Destination zones list is empty", null);
                return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
            }

            // Kiểm tra nếu số lượng chi tiết không khớp với số lượng destination zones
            if (details.size() != destinationZones.size()) {
                customResponse.setAll(false, "Number of details does not match number of destination zones", null);
                return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
            }

            // Cập nhật từng chi tiết với destination zone tương ứng
            for (int i = 0; i < details.size(); i++) {
                InternalTransactionDetail detail = details.get(i);
                detail.setDestination_zone(destinationZones.get(i));
                service.save(detail);
            }
        }

        customResponse.setAll(true, "Destination zones updated successfully", null);
        return ResponseEntity.ok(customResponse);
    }
}

package com.app.zware.Controllers;

import com.app.zware.Entities.History;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Service.HistoryService;
import com.app.zware.Service.ProductService;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")

public class HistoryController {

  @Autowired
  UserService userService;

  @Autowired
  HistoryService historyService;

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  ProductService productService;

  @GetMapping()
  public ResponseEntity<?> getByWarehouseIdAndProductAndDateInterval(
      @RequestParam("warehouse_id") Integer warehouseId,
      @RequestParam("product_id") Integer productId,
      @RequestParam("start_date") LocalDate startDate,
      @RequestParam("end_date") LocalDate endDate,
      HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization:
    User requestMaker = userService.getRequestMaker(request);
    if (!requestMaker.getRole().equals("admin") &&
        !requestMaker.getWarehouse_id().equals(warehouseId)) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //validate
    if (!warehouseService.existById(warehouseId)) {
      customResponse.setAll(false, "Warehouse Id is not valid", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    if (!productService.existById(productId)) {
      customResponse.setAll(false, "Product Id is not valid", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //passed validation
    List<History> histories = historyService.getByWarehouseAndProductAndDateInterval(warehouseId,
        productId, startDate, endDate);
    customResponse.setAll(true, "Get history success!", histories);
    return ResponseEntity.ok(customResponse);
  }
}

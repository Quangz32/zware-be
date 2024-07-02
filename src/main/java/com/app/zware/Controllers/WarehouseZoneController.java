package com.app.zware.Controllers;

import com.app.zware.Entities.User;
import com.app.zware.Entities.WarehouseZone;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseService;
import com.app.zware.Service.WarehouseZoneService;
import com.app.zware.Validation.WarehouseValidator;
import com.app.zware.Validation.WarehouseZoneValidator;
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
@RequestMapping("/api/zones")
public class WarehouseZoneController {

  @Autowired
  WarehouseZoneService warehouseZoneService;

  @Autowired
  WarehouseZoneValidator warehouseZoneValidator;

  @Autowired
  UserService userService;

  @Autowired
  WarehouseValidator warehouseValidator;

  @GetMapping("")
  public ResponseEntity<?> index() {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //finally
    List<WarehouseZone> warehouseZones = warehouseZoneService.getAll();
    customResponse.setAll(true, "Get warehouse zones success", warehouseZones);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody WarehouseZone warehouseZone,
      HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //finally
    String checkMessage = warehouseZoneValidator.checkPost(warehouseZone);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      WarehouseZone newZone = warehouseZoneService.createWarehouseZone(warehouseZone);
      customResponse.setAll(true, "Create warehouse zone success", newZone);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }


  @GetMapping("/{warehouseZoneId}")
  public ResponseEntity<?> show(@PathVariable("warehouseZoneId") Integer warehouseZoneId) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //validation
    String checkMessage = warehouseZoneValidator.checkGet(warehouseZoneId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally
    WarehouseZone foundZone = warehouseZoneService.getWarehouseZoneById(warehouseZoneId);
    customResponse.setAll(true, "Get warehouse zone success", foundZone);

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  @DeleteMapping("/{warehouseZoneId}")
  public ResponseEntity<?> destroy(@PathVariable("warehouseZoneId") Integer warehouseZoneId,
      HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //Validation
    String checkMessage = warehouseZoneValidator.checkDelete(warehouseZoneId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally
    warehouseZoneService.deleteWarehouseZoneById(warehouseZoneId);
    customResponse.setAll(true, "Delete warehouse zone success", null);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);


  }

  @PutMapping("/{warehouseZoneId}")
  public ResponseEntity<?> update(@PathVariable Integer warehouseZoneId,
      @RequestBody WarehouseZone requestWarehouseZone, HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //Validation
    WarehouseZone mergedWarehouseZone = warehouseZoneService.merger(warehouseZoneId,
        requestWarehouseZone);

    String checkMessage = warehouseZoneValidator.checkPut(warehouseZoneId, mergedWarehouseZone);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Update
    WarehouseZone updated = warehouseZoneService.update(mergedWarehouseZone);
    customResponse.setAll(true, "Update warehouse zone success", updated);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping(params = "warehouse_id")
  public ResponseEntity<?> getByWarehouse(@RequestParam("warehouse_id") Integer warehouseId){
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : All

    //Validation


    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Update
    List<WarehouseZone> zones = warehouseZoneService.getByWarehouse(warehouseId);
    customResponse.setAll(true, "Update warehouse zone success", zones);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }
}
